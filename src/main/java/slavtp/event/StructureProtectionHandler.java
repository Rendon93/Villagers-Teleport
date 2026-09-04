package slavtp.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slavtp.block.AltarBlock;
import slavtp.block.TransportAnchorBlock;

public class StructureProtectionHandler implements PlayerBlockBreakEvents.Before{

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(new StructureProtectionHandler());
    }

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return true;

        // Revisamos en un área de 5x5 si hay un bloque central activo (Altar o Ancla)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Saltamos la posición actual si es el mismo bloque que se intenta romper
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = pos.add(dx, 0, dz);
                BlockState centerState = world.getBlockState(checkPos);

                // Si hay un Altar o Ancla de Transporte en el centro del rango 5x5
                if (centerState.getBlock() instanceof AltarBlock || centerState.getBlock() instanceof TransportAnchorBlock) {

                    // Verificamos si la posición que se intenta romper pertenece a los 24 bloques de la plataforma
                    if (Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
                        if (!player.isCreative()) { // Opcional: permite a los jugadores en Creativo saltarse la regla
                            player.sendMessage(
                                    Text.literal("Debes destruir el bloque central de la plataforma primero.")
                                            .formatted(Formatting.RED),
                                    true
                            );
                            return false; // Cancela la destrucción del bloque
                        }
                    }
                }
            }
        }

        return true; // Permite romper el bloque normalmente
    }
}
