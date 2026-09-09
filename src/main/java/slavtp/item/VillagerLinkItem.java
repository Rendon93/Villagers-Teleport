package slavtp.item;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import slavtp.block.AltarBlock;
import slavtp.block.TransportAnchorBlock;
import slavtp.data.AltarSavedData;
import slavtp.data.AnchorLocationData;
import slavtp.network.ModPackets;

import java.util.List;
import java.util.Map;

public class VillagerLinkItem extends Item {

    public VillagerLinkItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (world.isClient() || player == null) return ActionResult.SUCCESS;

        ServerWorld serverWorld = (ServerWorld) world;

        // =========================================================================
        // DIRECCIÓN 2: ALTAR -> ANCLA (Abre la GUI para seleccionar destino)
        // =========================================================================
        if (world.getBlockState(pos).getBlock() instanceof AltarBlock) {

            // Caja de búsqueda 5x5 en la superficie del Altar
            Box searchBox = new Box(
                    pos.getX() - 2, pos.getY(), pos.getZ() - 2,
                    pos.getX() + 3, pos.getY() + 2.5, pos.getZ() + 3
            );
            List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, entity -> true);

            if (villagers.isEmpty()) {
                player.sendMessage(
                        Text.literal("No hay aldeanos sobre el Altar para transportar.").formatted(Formatting.YELLOW),
                        true
                );
                return ActionResult.FAIL;
            }

            if (player.experienceLevel < 1 && !player.isCreative()) {
                player.sendMessage(
                        Text.literal("Necesitas al menos 1 nivel de experiencia para activar el Altar.").formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            AnchorLocationData anchorData = AnchorLocationData.get(serverWorld);
            Map<BlockPos, Text> anchors = anchorData.getAnchors();

            if (anchors.isEmpty()) {
                player.sendMessage(
                        Text.literal("No hay Anclas de Transporte registradas a las cuales enviar los aldeanos.").formatted(Formatting.YELLOW),
                        true
                );
                return ActionResult.FAIL;
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(anchors.size());
            for (Map.Entry<BlockPos, Text> entry : anchors.entrySet()) {
                buf.writeBlockPos(entry.getKey());
                buf.writeText(entry.getValue());
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, ModPackets.OPEN_ANCHOR_GUI_PACKET, buf);
            }

            return ActionResult.SUCCESS;
        }

        // =========================================================================
        // DIRECCIÓN 1: ANCLA -> ALTAR (Transporte directo de aldeanos)
        // =========================================================================
        if (world.getBlockState(pos).getBlock() instanceof TransportAnchorBlock) {

            if (player.experienceLevel < 1 && !player.isCreative()) {
                player.sendMessage(
                        Text.literal("Necesitas al menos 1 nivel de experiencia para activar el ritual.").formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            // Buscar Altar activo vinculado al jugador o el más cercano válido con obsidianas
            AltarSavedData savedData = AltarSavedData.get(serverWorld);
            BlockPos targetAltarPos = savedData.getAltar(player.getUuid());

            if (targetAltarPos == null || !(world.getBlockState(targetAltarPos).getBlock() instanceof AltarBlock) || !AltarBlock.checkObsidianStructure(world, targetAltarPos)) {
                targetAltarPos = savedData.getValidNearestAltar(serverWorld, pos);
            }

            if (targetAltarPos == null) {
                player.sendMessage(
                        Text.literal("No existe ningún Altar activo guardado al cual enviar los aldeanos.").formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            // Cobertura exacta 5x5 en la superficie del Ancla
            Box searchBox = new Box(
                    pos.getX() - 2, pos.getY(), pos.getZ() - 2,
                    pos.getX() + 3, pos.getY() + 2.5, pos.getZ() + 3
            );
            List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, entity -> true);

            if (villagers.isEmpty()) {
                player.sendMessage(
                        Text.literal("No hay aldeanos sobre la plataforma 5x5 de anclaje.").formatted(Formatting.YELLOW),
                        true
                );
                return ActionResult.FAIL;
            }

            // Consumir XP
            if (!player.isCreative()) {
                player.addExperienceLevels(-1);
            }

            // Teletransportar aldeanos a la parte superior del Altar
            BlockPos destination = targetAltarPos.up();
            for (VillagerEntity villager : villagers) {
                villager.teleport(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, true);
            }

            // Enviar paquete a los clientes cercanos para renderizar los rayos rojos en el Altar
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(destination);

            for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(serverWorld, destination)) {
                ServerPlayNetworking.send(trackingPlayer, ModPackets.RED_LIGHTNING_EFFECT_PACKET, buf);
            }

            // Efectos de sonido
            world.playSound(null, destination, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, destination, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

            player.sendMessage(
                    Text.literal("¡Transporte completado! Se han trasladado " + villagers.size() + " aldeano(s) al Altar.")
                            .formatted(Formatting.GREEN),
                    true
            );

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}