package slavtp.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RedLightningRenderer {

    private static class LightningBoltData {
        List<Vec3d> points = new ArrayList<>();
        int maxTicks = 8; // Duración del destello (8 ticks ~0.4s)
        int currentTicks = 0;
    }

    private static final List<LightningBoltData> ACTIVE_BOLTS = new ArrayList<>();

    public static void spawnRedLightningCluster(BlockPos targetPos) {
        Random random = new Random();
        int boltCount = 4 + random.nextInt(3); // Genera entre 4 y 6 rayos simultáneos

        for (int b = 0; b < boltCount; b++) {
            LightningBoltData bolt = new LightningBoltData();

            // Punto de inicio en el cielo con un desfase aleatorio
            double startX = targetPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 6.0;
            double startY = targetPos.getY() + 14.0 + random.nextDouble() * 6.0;
            double startZ = targetPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 6.0;

            double targetX = targetPos.getX() + 0.5;
            double targetY = targetPos.getY() + 0.1;
            double targetZ = targetPos.getZ() + 0.5;

            int segments = 12;
            Vec3d current = new Vec3d(startX, startY, startZ);
            bolt.points.add(current);

            for (int i = 1; i < segments; i++) {
                double progress = (double) i / segments;
                // Interpolación hacia el punto del Altar con un desfase en zigzag
                double nextX = startX + (targetX - startX) * progress + (random.nextDouble() - 0.5) * 1.2;
                double nextY = startY + (targetY - startY) * progress;
                double nextZ = startZ + (targetZ - startZ) * progress + (random.nextDouble() - 0.5) * 1.2;

                current = new Vec3d(nextX, nextY, nextZ);
                bolt.points.add(current);
            }
            // Punto final exacto en el Altar
            bolt.points.add(new Vec3d(targetX, targetY, targetZ));
            ACTIVE_BOLTS.add(bolt);
        }
    }

    public static void render(WorldRenderContext context) {
        if (ACTIVE_BOLTS.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        for (int i = ACTIVE_BOLTS.size() - 1; i >= 0; i--) {
            LightningBoltData bolt = ACTIVE_BOLTS.get(i);

            for (int p = 0; p < bolt.points.size() - 1; p++) {
                Vec3d start = bolt.points.get(p);
                Vec3d end = bolt.points.get(p + 1);

                // Dibujar línea con color ROJO (R: 255, G: 20, B: 20, Alpha: 255)
                buffer.vertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
                        .color(255, 20, 20, 255)
                        .normal(0, 1, 0)
                        .next();

                buffer.vertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
                        .color(255, 20, 20, 255)
                        .normal(0, 1, 0)
                        .next();
            }

            bolt.currentTicks++;
            if (bolt.currentTicks >= bolt.maxTicks) {
                ACTIVE_BOLTS.remove(i);
            }
        }

        matrices.pop();
    }
}