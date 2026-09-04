package slavtp.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
}
