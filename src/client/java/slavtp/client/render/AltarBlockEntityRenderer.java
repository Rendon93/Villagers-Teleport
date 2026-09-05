package slavtp.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import slavtp.block.entity.AltarBlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class AltarBlockEntityRenderer implements BlockEntityRenderer<AltarBlockEntity> {
    private static final Identifier CIRCLE_TEXTURE = new Identifier("slave-tp", "textures/entity/magic_circle.png");

    public AltarBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(AltarBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float progress = entity.getRenderProgress();

        if (progress <= 0.0f) return;

        matrices.push();

        // Posición fija sobre el altar
        matrices.translate(0.5, 1.01, 0.5);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(CIRCLE_TEXTURE));
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int maxLight = 0xF000F0; // Brillo mágico

        // El tamaño del marco siempre es 5x5 completo (2.5 de radio)
        float totalSize = 2.5f;

        // El radio visible va aumentando, pero la textura NO se encoge ni se deforma;
        // se recorta para mostrar solo el área del círculo que ya ha sido "trazada"
        float currentRadius = totalSize * progress;
        float uMin = 0.5f - (0.5f * progress);
        float uMax = 0.5f + (0.5f * progress);
        float vMin = 0.5f - (0.5f * progress);
        float vMax = 0.5f + (0.5f * progress);

        // Opacidad de las líneas que van apareciendo
        int alpha = (int) (255 * Math.min(1.0f, progress * 1.2f));

        // Dibujamos la porción revelada con las coordenadas UV mapeadas al tamaño real
        buffer.vertex(matrix, -currentRadius, 0, -currentRadius).color(255, 255, 255, alpha).texture(uMin, vMin).overlay(overlay).light(maxLight).normal(0, 1, 0).next();
        buffer.vertex(matrix, -currentRadius, 0,  currentRadius).color(255, 255, 255, alpha).texture(uMin, vMax).overlay(overlay).light(maxLight).normal(0, 1, 0).next();
        buffer.vertex(matrix,  currentRadius, 0,  currentRadius).color(255, 255, 255, alpha).texture(uMax, vMax).overlay(overlay).light(maxLight).normal(0, 1, 0).next();
        buffer.vertex(matrix,  currentRadius, 0, -currentRadius).color(255, 255, 255, alpha).texture(uMax, vMin).overlay(overlay).light(maxLight).normal(0, 1, 0).next();


        matrices.pop();

        World world = entity.getWorld();
        if (world != null && world.isClient()) {
            Random random = world.getRandom();

            // Aparecen con más frecuencia conforme el circuito se completa
            if (random.nextFloat() < (0.4f * progress)) {
                double offsetX = (random.nextDouble() - 0.5) * 4.8;
                double offsetZ = (random.nextDouble() - 0.5) * 4.8;

                double x = entity.getPos().getX() + 0.5 + offsetX;
                double y = entity.getPos().getY() + 1.05;
                double z = entity.getPos().getZ() + 0.5 + offsetZ;

                // Color personalizado (ejemplo: Rojo Carmesí)
                double red = 1.0;
                double green = 0.1;
                double blue = 0.1;

                world.addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        x, y, z,
                        red, green, blue
                );
            }
        }


    }
}