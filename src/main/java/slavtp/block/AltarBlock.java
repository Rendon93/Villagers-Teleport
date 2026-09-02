package slavtp.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slavtp.block.entity.AltarBlockEntity;
import slavtp.item.VillagerLinkItem;

public class AltarBlock extends Block implements BlockEntityProvider{

    public AltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() instanceof VillagerLinkItem && stack.hasNbt()) {
                NbtCompound nbt = stack.getNbt();
                BlockEntity blockEntity = world.getBlockEntity(pos);

                if (blockEntity instanceof AltarBlockEntity altar) {
                    altar.teleportVillagerFromItem(nbt);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}
