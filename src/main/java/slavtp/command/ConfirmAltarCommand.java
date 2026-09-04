package slavtp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import slavtp.data.AltarSavedData;

public class ConfirmAltarCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("slavetp")
                .then(CommandManager.literal("confirm_altar")
                        .then(CommandManager.argument("x", IntegerArgumentType.integer())
                                .then(CommandManager.argument("y", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    ServerWorld world = context.getSource().getWorld();

                                                    int x = IntegerArgumentType.getInteger(context, "x");
                                                    int y = IntegerArgumentType.getInteger(context, "y");
                                                    int z = IntegerArgumentType.getInteger(context, "z");
                                                    BlockPos newPos = new BlockPos(x, y, z);

                                                    AltarSavedData data = AltarSavedData.get(world);
                                                    BlockPos oldPos = data.getAltar(player.getUuid());

                                                    if (oldPos != null) {
                                                        // Romper o reemplazar el altar anterior
                                                        world.setBlockState(oldPos, Blocks.AIR.getDefaultState());
                                                    }

                                                    // Actualizar la nueva ubicación
                                                    data.setAltar(player.getUuid(), newPos);
                                                    player.sendMessage(Text.literal("Altar vinculado a tu nueva ubicación.").formatted(Formatting.GREEN), false);

                                                    return 1;
                                                })
                                        )))
                )
        );
    }
}