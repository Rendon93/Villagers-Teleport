package slavtp.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

            // Abrir la pantalla de selección en el hilo principal del cliente
            client.execute(() -> {
                MinecraftClient.getInstance().setScreen(new AnchorSelectionScreen(anchors));
            });
        });

        // En el método registerClientPackets():
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.RED_LIGHTNING_EFFECT_PACKET, (client, handler, buf, responseSender) -> {
            BlockPos targetPos = buf.readBlockPos();
            client.execute(() -> {
                RedLightningRenderer.spawnRedLightningCluster(targetPos);
            });
        });

// En el onInitializeClient() de tu inicializador de cliente:
        WorldRenderEvents.LAST.register(RedLightningRenderer::render);
    }

}