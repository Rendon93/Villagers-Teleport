package slavtp.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransportAnchorBlockEntity extends BlockEntity{

    private int renderTicks = 0;

    public TransportAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSPORT_ANCHOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, TransportAnchorBlockEntity entity) {
        entity.renderTicks++;
    }

    public int getRenderTicks() {
        return renderTicks;
    }

    // Variable para almacenar el nombre personalizado
    private Text customName;

    // Método setter que te falta
    public void setCustomName(Text customName) {
        this.customName = customName;
        this.markDirty();
        if (this.world != null && !this.world.isClient()) {
            // Notifica al cliente/servidor para sincronizar el cambio de nombre
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.customName != null) {
            nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("CustomName", 8)) {
            this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
        }
    }
}
