package slavtp.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import slavtp.block.AltarBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AltarSavedData extends PersistentState {
    private final Map<UUID, BlockPos> playerAltars = new HashMap<>();

    public static AltarSavedData get(ServerWorld world) {
        PersistentStateManager stateManager = world.getServer().getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(
                AltarSavedData::fromNbt,
                AltarSavedData::new,
                "slave_tp_altars"
        );
    }

    public static AltarSavedData fromNbt(NbtCompound nbt) {
        AltarSavedData data = new AltarSavedData();
        NbtCompound altars = nbt.getCompound("PlayerAltars");
        for (String key : altars.getKeys()) {
            UUID playerUuid = UUID.fromString(key);
            int[] posArray = altars.getIntArray(key);
            if (posArray.length == 3) {
                data.playerAltars.put(playerUuid, new BlockPos(posArray[0], posArray[1], posArray[2]));
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound altars = new NbtCompound();
        playerAltars.forEach((uuid, pos) -> {
            altars.putIntArray(uuid.toString(), new int[]{pos.getX(), pos.getY(), pos.getZ()});
        });
        nbt.put("PlayerAltars", altars);
        return nbt;
    }

    public BlockPos getAltar(UUID playerUuid) {
        return playerAltars.get(playerUuid);
    }

    /**
     * Busca el altar válido (con bloque activo y estructura de obsidiana completa) más cercano.
     * Si encuentra una posición registrada donde ya no existe el altar, la elimina.
     */
    public BlockPos getValidNearestAltar(ServerWorld world, BlockPos origin) {
        BlockPos nearest = null;
        double minDistanceSq = Double.MAX_VALUE;

        Map<UUID, BlockPos> copy = new HashMap<>(playerAltars);

        for (Map.Entry<UUID, BlockPos> entry : copy.entrySet()) {
            BlockPos altarPos = entry.getValue();

            // Validar que el bloque siga siendo un AltarBlock y tenga las obsidianas
            if (world.getBlockState(altarPos).getBlock() instanceof AltarBlock && AltarBlock.checkObsidianStructure(world, altarPos)) {
                double distSq = altarPos.getSquaredDistance(origin);
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    nearest = altarPos;
                }
            } else {
                // Si la estructura ya no es válida, la removemos del registro
                removeAltarIfAt(altarPos);
            }
        }

        return nearest;
    }

    public void setAltar(UUID playerUuid, BlockPos pos) {
        playerAltars.put(playerUuid, pos);
        markDirty();
    }

    public void removeAltar(UUID playerUuid) {
        playerAltars.remove(playerUuid);
        markDirty();
    }

    public void removeAltarIfAt(BlockPos pos) {
        playerAltars.entrySet().removeIf(entry -> entry.getValue().equals(pos));
        markDirty();
    }
}