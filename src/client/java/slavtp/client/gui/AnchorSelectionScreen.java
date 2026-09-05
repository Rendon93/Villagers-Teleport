package slavtp.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import slavtp.network.ModPackets;

import java.util.Map;

public class AnchorSelectionScreen extends Screen {

    private final Map<BlockPos, Text> anchors;

    public AnchorSelectionScreen(Map<BlockPos, Text> anchors) {
        super(Text.literal("Seleccionar Ancla de Destino"));
        this.anchors = anchors;
    }

    @Override
    protected void init() {
        int y = 40;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startX = (this.width - buttonWidth) / 2;

        if (anchors.isEmpty()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("No hay anclas registradas"), b -> this.close())
                    .dimensions(startX, y, buttonWidth, buttonHeight).build());
            return;
        }

        for (Map.Entry<BlockPos, Text> entry : anchors.entrySet()) {
            BlockPos pos = entry.getKey();
            Text name = entry.getValue();

            Text buttonText = Text.literal("").append(name).append(" (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");

            this.addDrawableChild(ButtonWidget.builder(buttonText, button -> {
                // Enviar paquete al servidor con la posición elegida
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                ClientPlayNetworking.send(ModPackets.TELEPORT_REQUEST_PACKET, buf);

                this.close();
            }).dimensions(startX, y, buttonWidth, buttonHeight).build());

            y += 24;
            if (y > this.height - 40) break; // Límite simple de botones en pantalla
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}