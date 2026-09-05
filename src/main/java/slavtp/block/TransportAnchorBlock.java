package slavtp.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
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
import slavtp.data.AnchorLocationData;

public class TransportAnchorBlock extends BlockWithEntity {

    public TransportAnchorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            ItemStack stack = player.getStackInHand(hand);
            BlockEntity be = world.getBlockEntity(pos);

            // 1. Asignar nombre personalizado usando un Name Tag
            if (stack.isOf(Items.NAME_TAG) && be instanceof TransportAnchorBlockEntity anchor) {
                if (stack.hasCustomName()) {
                    Text newName = stack.getName();
                    anchor.setCustomName(newName);

                    // Actualizar el nombre en la lista global guardada en el servidor
                    if (world instanceof ServerWorld serverWorld) {
                        AnchorLocationData.get(serverWorld).addAnchor(pos, newName);
                    }

                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }

                    player.sendMessage(Text.literal("Ancla renombrada a: ").formatted(Formatting.GREEN).append(newName), true);
                    return ActionResult.SUCCESS;
                }
            }

            // 2. Mensaje informativo si se interactúa con la mano vacía
            if (stack.isEmpty()) {
                player.sendMessage(
                        Text.literal("Ancla de Transporte lista. Usa el activador con 1 nivel de EXP para iniciar el traslado.")
                                .formatted(Formatting.AQUA),
                        true
                );
            }
        }
        return ActionResult.SUCCESS;
    }

    // --- GESTIÓN DE REGISTRO GLOBAL DE ANCLAS ---

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (world.getRegistryKey() == World.OVERWORLD) {
                // Registrar ancla con nombre predeterminado al colocar el bloque
                AnchorLocationData.get(serverWorld).addAnchor(pos, Text.literal("Ancla de Transporte"));
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient() && world instanceof ServerWorld serverWorld) {
            // Eliminar de la lista global si el bloque es destruido
            AnchorLocationData.get(serverWorld).removeAnchor(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL; // Evita que el bloque se vuelva invisible
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