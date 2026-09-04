package slavtp.item;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slavtp.block.TransportAnchorBlock;
import slavtp.data.AltarSavedData;

import java.util.List;

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

        // Solo actuamos si el jugador hace clic sobre el TransportAnchorBlock
        if (world.getBlockState(pos).getBlock() instanceof TransportAnchorBlock) {

            // 1. Verificar experiencia del jugador
            if (player.experienceLevel < 1 && !player.isCreative()) {
                player.sendMessage(
                        Text.literal("Necesitas al menos 1 nivel de experiencia para activar el ritual.")
                                .formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            // 2. Obtener la ubicación del Altar vinculada al jugador
            AltarSavedData savedData = AltarSavedData.get((ServerWorld) world);
            BlockPos targetAltarPos = savedData.getAltar(player.getUuid());

            if (targetAltarPos == null) {
                player.sendMessage(
                        Text.literal("No tienes ningún Altar vinculado. Coloca y activa un Altar primero.")
                                .formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            // 3. Buscar aldeanos en el área de 5x5 sobre la plataforma (radio de 2.5 bloques, altura de 2)
            Box searchBox = new Box(pos).expand(2.5, 1.0, 2.5);
            List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, searchBox, entity -> true);

            if (villagers.isEmpty()) {
                player.sendMessage(
                        Text.literal("No hay aldeanos sobre la plataforma de anclaje.")
                                .formatted(Formatting.YELLOW),
                        true
                );
                return ActionResult.FAIL;
            }

            // 4. Cobrar la experiencia
            if (!player.isCreative()) {
                player.addExperienceLevels(-1);
            }

            // 5. Teletransportar a cada aldeano sobre el Altar
            BlockPos destination = targetAltarPos.up(); // Posición justo encima del Altar
            for (VillagerEntity villager : villagers) {
                villager.teleport(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, true);
            }

            // 6. Efectos de sonido
            world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, destination, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

            player.sendMessage(
                    Text.literal("¡Transporte completado! Se han trasladado " + villagers.size() + " aldeano(s).")
                            .formatted(Formatting.GREEN),
                    true
            );

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}