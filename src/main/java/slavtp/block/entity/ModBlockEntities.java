package slavtp.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import slavtp.SlaveTp;

public class ModBlockEntities {
    public static BlockEntityType<AltarBlockEntity> ALTAR_BLOCK_ENTITY;

    public static void registerBlockEntities() {
        ALTAR_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                SlaveTp.id("altar_block_entity"),
                FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new, SlaveTp.ALTAR_BLOCK).build()
        );
    }
}
