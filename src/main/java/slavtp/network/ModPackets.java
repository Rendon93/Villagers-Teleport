package slavtp.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ModPackets {
    public static final Identifier TELEPORT_REQUEST_PACKET = new Identifier("slave-tp", "teleport_request");

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(TELEPORT_REQUEST_PACKET, (server, player, handler, buf, responseSender) -> {
            BlockPos targetPos = buf.readBlockPos();

            server.execute(() -> {
                // Verificar XP (Maná) en el servidor antes de transportar
                if (player.experienceLevel < 1 && !player.isCreative()) {
                    player.sendMessage(Text.literal("Necesitas 1 nivel de XP.").formatted(Formatting.RED), true);
                    return;
                }

                // Consumir 1 nivel de XP
                if (!player.isCreative()) {
                    player.addExperienceLevels(-1);
                }

                // Efecto Origen
                player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

                // Teletransportar 1 bloque arriba del Ancla
                BlockPos dest = targetPos.up();
                player.teleport(player.getServerWorld(), dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, java.util.Collections.emptySet(), player.getYaw(), player.getPitch());

                // Efecto Destino
                player.getServerWorld().playSound(null, dest, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            });
        });
    }
}
