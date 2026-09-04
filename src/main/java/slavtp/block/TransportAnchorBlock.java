package slavtp.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slavtp.block.entity.ModBlockEntities;
import slavtp.block.entity.TransportAnchorBlockEntity;

public class TransportAnchorBlock extends BlockWithEntity {

    public TransportAnchorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && player.getStackInHand(hand).isEmpty()) {
            player.sendMessage(
                    Text.literal("Ancla de Transporte lista. Usa el activador con 1 nivel de EXP para iniciar el traslado.")
                            .formatted(Formatting.AQUA),
                    true
            );
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL; // Evita que el bloque de calcita se vuelva invisible
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransportAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.TRANSPORT_ANCHOR_BLOCK_ENTITY, TransportAnchorBlockEntity::tick);
    }
}