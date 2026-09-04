package slavtp.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import slavtp.block.entity.TransportAnchorBlockEntity;

public class TransportAnchorBlockEntityRenderer implements BlockEntityRenderer<TransportAnchorBlockEntity> {

    // Textura del círculo mágico de invocación/ancla
    private static final Identifier CIRCLE_TEXTURE = new Identifier("slave-tp", "textures/entity/transport_circle.png");

    public TransportAnchorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(TransportAnchorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // Obtenemos los ticks acumulados para la animación
        float ticks = entity.getRenderTicks() + tickDelta;

        // --- LÓGICA DEL DIBUJO LENTO ---
        // Definimos cuánto tarda en dibujarse (por ejemplo, 100 ticks = 5 segundos)
        float drawingDuration = 100.0f;

        // Calculamos el progreso (de 0.0 a 1.0)
        float progress = MathHelper.clamp(ticks / drawingDuration, 0.0f, 1.0f);

        // Si el progreso es 0, no renderizamos nada
        if (progress <= 0.0f) return;

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

        // Definimos el color del círculo (Cyan traslúcido)
        int r = 180;
        int g = 240;
        int b = 255;
        // La opacidad (alpha) también puede aumentar con el progreso (de 0 a 200)
        int alpha = (int)(200 * progress);

        // Renderizado del plano 2D plano sobre el piso
        drawProgressiveQuad(buffer, matrix, radius, r, g, b, alpha, progress); // Color cian/claro traslúcido

        matrices.pop();
    }

    private void drawProgressiveQuad(VertexConsumer buffer, Matrix4f matrix, float r, int red, int green, int blue, int alpha, float progress) {
        // Usamos luz máxima para que el círculo brille (15728880)
        int light = 15728880;

        // El progreso afecta a cómo se mapea la textura para dar el efecto de "dibujado"
        // Este efecto hace que la textura "aparezca" radialmente o de un lado a otro.
        // Aquí lo haremos aparecer de un lado al otro (X) para simplificar:
        float endU = progress;

        buffer.vertex(matrix, -r, 0, -r).color(red, green, blue, alpha).texture(0, 0).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, -r, 0, r).color(red, green, blue, alpha).texture(0, 1).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, r, 0, r).color(red, green, blue, alpha).texture(1, 1).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
        buffer.vertex(matrix, r, 0, -r).color(red, green, blue, alpha).texture(1, 0).overlay(0, 10).light(15728880).normal(0, 1, 0).next();
    }
}
