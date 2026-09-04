package slavtp.block;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slavtp.block.entity.AltarBlockEntity;
import slavtp.block.entity.ModBlockEntities;
import slavtp.item.VillagerLinkItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import slavtp.data.AltarSavedData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import java.util.UUID;

public class AltarBlock extends BlockWithEntity {

    public AltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // Necesario en BlockWithEntity para que el bloque no se vuelva invisible
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.ALTAR_BLOCK_ENTITY, AltarBlockEntity::tick);
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

    // --- LÓGICA AL COLOCAR EL BLOQUE ---
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient() && placer instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            AltarSavedData data = AltarSavedData.get(serverWorld);
            UUID uuid = player.getUuid();

            BlockPos previousPos = data.getAltar(uuid);

            if (previousPos != null && !previousPos.equals(pos)) {
                // Generamos un botón interactivo [SÍ] que ejecuta un comando al hacer clic
                Text yesBtn = Text.literal(" [SÍ]")
                        .formatted(Formatting.GREEN, Formatting.BOLD)
                        .styled(style -> style.withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/slavetp confirm_altar " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        )));

                Text message = Text.literal("Ya tienes un altar en otra ubicación. ¿Deseas construir este? (Se perderá el otro altar)")
                        .formatted(Formatting.YELLOW)
                        .append(yesBtn);

                player.sendMessage(message, false);
            } else {
                // Si es el primer altar que coloca, lo registramos directamente
                data.setAltar(uuid, pos);
            }
        }
    }

    // --- LÓGICA AL DESTRUIR EL BLOQUE MANUALE MENTE ---
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && !world.isClient() && world instanceof ServerWorld serverWorld) {
            AltarSavedData data = AltarSavedData.get(serverWorld);
            data.removeAltarIfAt(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Registra la estadística de romper el bloque en el jugador
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005F);

        if (!world.isClient()) {
            // Verificar si la herramienta principal tiene el encantamiento Silk Touch (Toque de Seda)
            boolean hasSilkTouch = EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) > 0;

            if (hasSilkTouch) {
                // Si tiene Silk Touch, dropea el ítem del bloque de manera normal
                dropStack(world, pos, new ItemStack(this));
            }
            // Si no tiene Silk Touch, simplemente no se llama a dropStack y el bloque desaparece por completo
        }
    }
}