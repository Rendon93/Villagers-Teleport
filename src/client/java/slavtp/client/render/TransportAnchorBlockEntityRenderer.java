package slavtp.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import slavtp.block.entity.TransportAnchorBlockEntity;

public class TransportAnchorBlockEntityRenderer implements BlockEntityRenderer<TransportAnchorBlockEntity> {

    // Textura del círculo mágico de invocación/ancla
    private static final Identifier CIRCLE_TEXTURE = new Identifier("slave-tp", "textures/entity/transport_circle.png");

    public TransportAnchorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(TransportAnchorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        // Elevar levemente sobre la superficie del bloque de calcita
        matrices.translate(0.5, 1.01, 0.5);

        // Rotación continua lenta del círculo
        float angle = (entity.getRenderTicks() + tickDelta) * 0.5f;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix = entry.getPositionMatrix();

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(CIRCLE_TEXTURE));

        // Escala del círculo para cubrir la plataforma de 5x5 (radio 2.5)
        float radius = 2.5f;

        // Renderizado del plano 2D plano sobre el piso
        drawQuad(buffer, matrix, radius, 180, 220, 255, 200); // Color cian/claro traslúcido

        matrices.pop();
    }

    private void drawQuad(VertexConsumer buffer, Matrix4f matrix, float r, int red, int green, int blue, int alpha) {
        buffer.vertex(matrix, -r, 0, -r).color(red, green, blue, alpha).texture(0, 0).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, -r, 0, r).color(red, green, blue, alpha).texture(0, 1).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, r, 0, r).color(red, green, blue, alpha).texture(1, 1).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, r, 0, -r).color(red, green, blue, alpha).texture(1, 0).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
    }
}
