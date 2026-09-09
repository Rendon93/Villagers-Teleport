package slavtp.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import slavtp.block.entity.AltarBlockEntity;
import slavtp.block.entity.ModBlockEntities;
import slavtp.data.AltarSavedData;
import slavtp.item.VillagerLinkItem;

import java.util.UUID;

public class AltarBlock extends BlockWithEntity {

    public AltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    /**
     * Valida que exista una plataforma 5x5 de obsidiana en la misma altura Y que el Altar.
     */
    public static boolean checkObsidianStructure(World world, BlockPos altarPos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue; // Salta el bloque central
                BlockPos checkPos = altarPos.add(x, 0, z);
                if (!world.getBlockState(checkPos).isOf(Blocks.OBSIDIAN)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!checkObsidianStructure(world, pos)) return;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 1.2;
        double centerZ = pos.getZ() + 0.5;

        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.5;
            double offsetY = random.nextDouble() * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 2.5;

            double vx = (random.nextDouble() - 0.5) * 0.015;
            double vy = random.nextDouble() * 0.008 + 0.002;
            double vz = (random.nextDouble() - 0.5) * 0.015;

            world.addParticle(
                    net.minecraft.particle.ParticleTypes.END_ROD,
                    centerX + offsetX,
                    centerY + offsetY,
                    centerZ + offsetZ,
                    vx, vy, vz
            );
        }
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

            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    // --- LÓGICA AL COLOCAR EL BLOQUE ---
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient() && placer instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;

            if (!checkObsidianStructure(world, pos)) {
                player.sendMessage(
                        Text.literal("Altar colocado, pero la estructura de 24 obsidianas está incompleta. Complétala para activarlo.")
                                .formatted(Formatting.RED),
                        false
                );
                return;
            }

            AltarSavedData data = AltarSavedData.get(serverWorld);
            UUID uuid = player.getUuid();

            BlockPos previousPos = data.getAltar(uuid);

            if (previousPos != null && !previousPos.equals(pos)) {
                Text yesBtn = Text.literal(" [SÍ]")
                        .formatted(Formatting.GREEN, Formatting.BOLD)
                        .styled(style -> style.withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/slavetp confirm_altar " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        )));

                Text message = Text.literal("Ya tienes un altar en otra ubicación. ¿Deseas activar este? (Se perderá el anterior)")
                        .formatted(Formatting.YELLOW)
                        .append(yesBtn);

                player.sendMessage(message, false);
            } else {
                data.setAltar(uuid, pos);
                player.sendMessage(
                        Text.literal("¡Altar de Transporte vinculado con éxito!").formatted(Formatting.GREEN),
                        false
                );
            }
        }
    }

    // --- LÓGICA AL DESTRUIR EL BLOQUE MANUALMENTE ---
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
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005F);

        if (!world.isClient()) {
            boolean hasSilkTouch = EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) > 0;
            if (hasSilkTouch) {
                dropStack(world, pos, new ItemStack(this));
            }
        }
    }
}