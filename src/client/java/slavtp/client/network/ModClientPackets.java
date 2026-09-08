package slavtp.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import slavtp.client.gui.AnchorSelectionScreen;
import slavtp.client.render.RedLightningRenderer;
import slavtp.network.ModPackets;

import java.util.HashMap;
import java.util.Map;

public class ModClientPackets {

    public static void registerClientPackets() {
        // 1. Paquete para abrir el menú de selección de Anclas
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

        // 2. Paquete para invocar la ráfaga de rayos rojos
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.RED_LIGHTNING_EFFECT_PACKET, (client, handler, buf, responseSender) -> {
            BlockPos targetPos = buf.readBlockPos();
            client.execute(() -> {
                RedLightningRenderer.spawnRedLightningCluster(targetPos);
            });
        });
    }
}