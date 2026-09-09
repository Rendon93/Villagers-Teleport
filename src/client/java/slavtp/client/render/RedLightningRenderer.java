package slavtp.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RedLightningRenderer {

    private static class HakiBolt {
        Vec3d origin;
        Vec3d target;
        long creationTime;
        long delayMs;
        long lifetimeMs;
        int segments;
        boolean isResidual; // Define si es un rayo principal o un residuo

        public HakiBolt(Vec3d origin, Vec3d target, long creationTime, long delayMs, long lifetimeMs, int segments, boolean isResidual) {
            this.origin = origin;
            this.target = target;
            this.creationTime = creationTime;
            this.delayMs = delayMs;
            this.lifetimeMs = lifetimeMs;
            this.segments = segments;
            this.isResidual = isResidual;
        }
    }

    private static final List<HakiBolt> ACTIVE_BOLTS = new ArrayList<>();

    public static void spawnRedLightningCluster(BlockPos targetPos) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) return;

        Random random = new Random();
        long now = System.currentTimeMillis();

        // --- FASE 1: RAYOS PRINCIPALES (Pocos, limpios y concentrados) ---
        int mainCount = 5 + random.nextInt(3); // Solo 5 a 7 rayos grandes
        for (int i = 0; i < mainCount; i++) {
            double startX = targetPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 3.0;
            double startY = targetPos.getY() + 0.2;
            double startZ = targetPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3.0;

            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 5.0 + random.nextDouble() * 4.0;
            double targetX = startX + Math.cos(angle) * distance;
            double targetY = startY + 2.5 + random.nextDouble() * 4.0;
            double targetZ = startZ + Math.sin(angle) * distance;

            ACTIVE_BOLTS.add(new HakiBolt(
                    new Vec3d(startX, startY, startZ),
                    new Vec3d(targetX, targetY, targetZ),
                    now, 0, 800, 12, false
            ));
        }

        // --- FASE 2: RESIDUOS Y CHISPAS (Nacen con retardo en suelo y pilares) ---
        int residualCount = 8 + random.nextInt(5); // 8 a 12 chispas dispersas
        for (int i = 0; i < residualCount; i++) {
            // Dispersión en un radio más amplio alrededor de la estructura
            double rx = targetPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 7.0;
            double rz = targetPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 7.0;
            double ry = targetPos.getY() + 0.1 + random.nextDouble() * 2.5; // Puede pegar en pilares o suelo

            // Arcos muy cortos y pegados a las superficies
            double length = 0.6 + random.nextDouble() * 1.2;
            double angle = random.nextDouble() * Math.PI * 2;
            double tx = rx + Math.cos(angle) * length;
            double ty = ry + (random.nextDouble() - 0.3) * 0.8;
            double tz = rz + Math.sin(angle) * length;

            long delay = 300 + random.nextInt(300); // Aparecen entre 0.3s y 0.6s después del estallido

            ACTIVE_BOLTS.add(new HakiBolt(
                    new Vec3d(rx, ry, rz),
                    new Vec3d(tx, ty, tz),
                    now, delay, 1200, 5, true
            ));
        }
    }

    public static void render(WorldRenderContext context) {
        if (ACTIVE_BOLTS.isEmpty()) return;

        long now = System.currentTimeMillis();

        // Eliminar rayos que completaron su tiempo de vida
        ACTIVE_BOLTS.removeIf(bolt -> (now - bolt.creationTime) > (bolt.delayMs + bolt.lifetimeMs));

        if (ACTIVE_BOLTS.isEmpty()) return;

        Random random = new Random();
        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        VertexConsumerProvider consumers = context.consumers();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        for (HakiBolt bolt : ACTIVE_BOLTS) {
            long age = now - (bolt.creationTime + bolt.delayMs);
            if (age < 0) continue; // Aún está esperando su tiempo de retardo (Delay)

            float progress = (float) age / bolt.lifetimeMs;

            // Desvanecimiento al final del ciclo
            int alpha = 255;
            if (progress > 0.5f) {
                alpha = (int) (255 * (1.0f - ((progress - 0.5f) / 0.5f)));
            }

            List<Vec3d> path = generatePath(bolt, random);

            for (int p = 0; p < path.size() - 1; p++) {
                Vec3d start = path.get(p);
                Vec3d end = path.get(p + 1);

                if (bolt.isResidual) {
                    // --- RESIDUOS: Más delgados y sutiles ---
                    drawSegment(buffer, positionMatrix, start, end, 0.04, 255, 30, 30, alpha);
                    drawLine(buffer, positionMatrix, start, end, 10, 10, 10, alpha);
                } else {
                    // --- PRINCIPALES: Gruesos con aura ---
                    drawSegment(buffer, positionMatrix, start, end, 0.12, 200, 10, 20, alpha);
                    drawSegment(buffer, positionMatrix, start, end, -0.12, 200, 10, 20, alpha);
                    drawSegment(buffer, positionMatrix, start, end, 0.06, 255, 50, 60, alpha);
                    drawLine(buffer, positionMatrix, start, end, 5, 5, 5, alpha);
                }
            }
        }

        matrices.pop();
    }

    private static List<Vec3d> generatePath(HakiBolt bolt, Random random) {
        List<Vec3d> points = new ArrayList<>();
        points.add(bolt.origin);

        Vec3d start = bolt.origin;
        Vec3d target = bolt.target;
        double jitterFactor = bolt.isResidual ? 0.3 : 1.8; // Menos deformación en chispas pequeñas

        for (int i = 1; i < bolt.segments; i++) {
            double ratio = (double) i / bolt.segments;

            double jitterX = (random.nextDouble() - 0.5) * jitterFactor;
            double jitterY = (random.nextDouble() - 0.5) * jitterFactor;
            double jitterZ = (random.nextDouble() - 0.5) * jitterFactor;

            double nextX = start.x + (target.x - start.x) * ratio + jitterX;
            double nextY = start.y + (target.y - start.y) * ratio + jitterY;
            double nextZ = start.z + (target.z - start.z) * ratio + jitterZ;

            points.add(new Vec3d(nextX, nextY, nextZ));
        }

        points.add(bolt.target);
        return points;
    }

    private static void drawSegment(VertexConsumer buffer, Matrix4f matrix, Vec3d start, Vec3d end, double offset, int r, int g, int b, int a) {
        Vec3d off = new Vec3d(offset, offset, offset);
        drawLine(buffer, matrix, start.add(off), end.add(off), r, g, b, a);
    }

    private static void drawLine(VertexConsumer buffer, Matrix4f matrix, Vec3d start, Vec3d end, int r, int g, int b, int a) {
        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(r, g, b, a)
                .normal(0, 1, 0)
                .next();

        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(r, g, b, a)
                .normal(0, 1, 0)
                .next();
    }
}