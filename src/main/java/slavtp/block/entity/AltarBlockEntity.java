package slavtp.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.UUID;

public class AltarBlockEntity extends BlockEntity {

    private boolean structureComplete = false;
    private float renderProgress = 0.0f;
    private static final float ANIMATION_SPEED = 0.0035f;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_BLOCK_ENTITY, pos, state);
    }

    // --- LÓGICA DE DETECCIÓN DE LA ESTRUCTURA 5x5 ---
    private int tickCounter = 0;

    public static void tick(World world, BlockPos pos, BlockState state, AltarBlockEntity entity) {
        if (world == null) return;

        // LÓGICA DEL SERVIDOR: Comprobar la obsidiana cada 20 ticks (1 segundo)
        if (!world.isClient()) {
            entity.tickCounter++;
            if (entity.tickCounter >= 20) {
                entity.tickCounter = 0;

                boolean wasComplete = entity.structureComplete;
                entity.structureComplete = checkStructure(world, pos);

                if (wasComplete != entity.structureComplete) {
                    entity.markDirty();
                    world.updateListeners(pos, state, state, 3);
                }
            }
        }

        // LÓGICA DEL CLIENTE Y SERVIDOR: Avanzar la animación en cada tick
        if (entity.structureComplete) {
            if (entity.renderProgress < 1.0f) {
                entity.renderProgress = Math.min(1.0f, entity.renderProgress + ANIMATION_SPEED);
            }
        } else {
            if (entity.renderProgress > 0.0f) {
                entity.renderProgress = Math.max(0.0f, entity.renderProgress - (ANIMATION_SPEED * 2));
            }
        }
    }

    //Getter para que el Renderer pueda leer el progreso ---
    public float getRenderProgress() {
        return renderProgress;
    }

    private static boolean checkStructure(World world, BlockPos posAltar) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue; // Saltamos el centro (donde está el Altar)

                BlockPos targetPos = posAltar.add(x, 0, z);
                if (!world.getBlockState(targetPos).isOf(Blocks.OBSIDIAN)) {
                    return false; // Si falta un solo bloque de obsidiana, no se activa
                }
            }
        }
        return true;
    }

    public boolean isStructureComplete() {
        return structureComplete;
    }

    // --- SINCRONIZACIÓN DE DATOS (NBT & RED) ---

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean("StructureComplete", structureComplete);
        nbt.putFloat("RenderProgress", renderProgress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        structureComplete = nbt.getBoolean("StructureComplete");
        renderProgress = nbt.getFloat("RenderProgress");
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    // --- TU LÓGICA ORIGINAL DE TELETRANSPORTE ---

    public void teleportVillagerFromItem(NbtCompound nbt) {
        if (world instanceof ServerWorld serverWorld && nbt.contains("TargetVillager")) {
            UUID villagerUuid = nbt.getUuid("TargetVillager");

            // Buscar al aldeano en el mundo cargado
            if (serverWorld.getEntity(villagerUuid) instanceof VillagerEntity villager) {
                BlockPos targetPos = pos.up();

                // Teletransporte compatible con Minecraft 1.20.1
                villager.teleport(
                        serverWorld,
                        targetPos.getX() + 0.5,
                        targetPos.getY(),
                        targetPos.getZ() + 0.5,
                        Collections.emptySet(),
                        villager.getYaw(),
                        villager.getPitch()
                );
            }
        }
    }
}