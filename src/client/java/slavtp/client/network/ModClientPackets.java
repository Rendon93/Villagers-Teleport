package slavtp.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import slavtp.client.gui.AnchorSelectionScreen;
import slavtp.client.render.RedLightningRenderer;
import slavtp.network.ModPackets;

import java.util.HashMap;
import java.util.Map;

public class ModClientPackets {

    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OPEN_ANCHOR_GUI_PACKET, (client, handler, buf, responseSender) -> {
            int size = buf.readInt();
            Map<BlockPos, Text> anchors = new HashMap<>();

            for (int i = 0; i < size; i++) {
                BlockPos pos = buf.readBlockPos();
                Text name = buf.readText();
                anchors.put(pos, name);
            }

            client.execute(() -> {
                MinecraftClient.getInstance().setScreen(new AnchorSelectionScreen(anchors));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.RED_LIGHTNING_EFFECT_PACKET, (client, handler, buf, responseSender) -> {
            BlockPos targetPos = buf.readBlockPos();

            client.execute(() -> {
                if (client.world != null) {
                    // Render de los rayos rojos
                    RedLightningRenderer.spawnRedLightningCluster(targetPos);

                    // Reproducción usando coordenadas double directamente
                    client.world.playSound(
                            targetPos.getX() + 0.5,
                            targetPos.getY() + 0.5,
                            targetPos.getZ() + 0.5,
                            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                            SoundCategory.WEATHER,
                            3.0f,
                            1.0f,
                            false
                    );
                }
            });
        });
    }
}