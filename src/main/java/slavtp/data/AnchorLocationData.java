package slavtp.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class AnchorLocationData extends PersistentState {

    private final Map<BlockPos, Text> anchors = new HashMap<>();

    public static AnchorLocationData get(ServerWorld world) {
        // Leemos siempre del estado del Overworld para mantener el registro unificado
        ServerWorld overworld = world.getServer().getWorld(World.OVERWORLD);
        if (overworld == null) return new AnchorLocationData();

        return overworld.getPersistentStateManager().getOrCreate(
                AnchorLocationData::readNbt,
                AnchorLocationData::new,
                "slave_tp_anchors"
        );
    }

    public void addAnchor(BlockPos pos, Text name) {
        this.anchors.put(pos.toImmutable(), name);
        this.markDirty();
    }

    public void removeAnchor(BlockPos pos) {
        this.anchors.remove(pos);
        this.markDirty();
    }

    public Map<BlockPos, Text> getAnchors() {
        return this.anchors;
    }

    // --- MANEJO DE NBT ---

    public static AnchorLocationData readNbt(NbtCompound nbt) {
        AnchorLocationData data = new AnchorLocationData();
        NbtList list = nbt.getList("Anchors", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound anchorNbt = list.getCompound(i);
            BlockPos pos = BlockPos.fromLong(anchorNbt.getLong("Pos"));
            Text name = Text.Serializer.fromJson(anchorNbt.getString("Name"));
            data.anchors.put(pos, name);
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Map.Entry<BlockPos, Text> entry : anchors.entrySet()) {
            NbtCompound anchorNbt = new NbtCompound();
            anchorNbt.putLong("Pos", entry.getKey().asLong());
            anchorNbt.putString("Name", Text.Serializer.toJson(entry.getValue()));
            list.add(anchorNbt);
        }
        nbt.put("Anchors", list);
        return nbt;
    }
}