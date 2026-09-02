package slavtp.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.UUID;

public class AltarBlockEntity extends BlockEntity{
    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_BLOCK_ENTITY, pos, state);
    }

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
