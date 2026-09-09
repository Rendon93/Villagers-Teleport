package slavtp.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import slavtp.data.AltarSavedData;

import java.util.List;

public class ModPackets {
    public static final Identifier RED_LIGHTNING_EFFECT_PACKET = new Identifier("slavtp", "red_lightning_effect");
    public static final Identifier TELEPORT_REQUEST_PACKET = new Identifier("slavtp", "teleport_request");
    public static final Identifier OPEN_ANCHOR_GUI_PACKET = new Identifier("slavtp", "open_anchor_gui");

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(TELEPORT_REQUEST_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos targetAnchorPos = buf.readBlockPos();

            server.execute(() -> {
                ServerWorld serverWorld = player.getServerWorld();
                AltarSavedData savedData = AltarSavedData.get(serverWorld);

                BlockPos altarPos = savedData.getAltar(player.getUuid());

                if (altarPos == null) {
                    altarPos = savedData.getValidNearestAltar(serverWorld, player.getBlockPos());
                }

                if (altarPos == null) {
                    player.sendMessage(Text.literal("No se encontró un Altar activo.").formatted(Formatting.RED), true);
                    return;
                }

                Box searchBox = new Box(
                        altarPos.getX() - 2, altarPos.getY(), altarPos.getZ() - 2,
                        altarPos.getX() + 3, altarPos.getY() + 2.5, altarPos.getZ() + 3
                );
                List<VillagerEntity> villagers = serverWorld.getEntitiesByClass(VillagerEntity.class, searchBox, entity -> true);

                if (villagers.isEmpty()) {
                    player.sendMessage(Text.literal("No hay aldeanos sobre el Altar.").formatted(Formatting.YELLOW), true);
                    return;
                }

                if (player.experienceLevel < 1 && !player.isCreative()) {
                    player.sendMessage(Text.literal("Necesitas 1 nivel de XP para realizar el transporte.").formatted(Formatting.RED), true);
                    return;
                }

                if (!player.isCreative()) {
                    player.addExperienceLevels(-1);
                }

                // 1. Sonido sutil en el Altar de origen (solo desvanecimiento tipo Enderman)
                serverWorld.playSound(null, altarPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 0.8f);

                // 2. Mover aldeanos al Ancla destino
                BlockPos destination = targetAnchorPos.up();
                for (VillagerEntity villager : villagers) {
                    villager.teleport(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, true);
                }

                // 3. Enviar paquete al cliente para disparar los Rayos Rojos y el Trueno ÚNICAMENTE en el Ancla destino
                PacketByteBuf effectBuf = PacketByteBufs.create();
                effectBuf.writeBlockPos(destination);

                ServerPlayNetworking.send(player, RED_LIGHTNING_EFFECT_PACKET, effectBuf);

                for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(serverWorld, destination)) {
                    if (!trackingPlayer.equals(player)) {
                        ServerPlayNetworking.send(trackingPlayer, RED_LIGHTNING_EFFECT_PACKET, effectBuf);
                    }
                }

                player.sendMessage(
                        Text.literal("¡Se han trasladado " + villagers.size() + " aldeano(s) al Ancla de destino!")
                                .formatted(Formatting.GREEN),
                        true
                );
            });
        });
    }
}