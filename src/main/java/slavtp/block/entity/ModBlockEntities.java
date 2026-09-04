package slavtp.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import slavtp.SlaveTp;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<AltarBlockEntity> ALTAR_BLOCK_ENTITY;

    public static void registerBlockEntities() {
        ALTAR_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                SlaveTp.id("altar_block_entity"),
                FabricBlockEntityTypeBuilder.create(AltarBlockEntity::new, SlaveTp.ALTAR_BLOCK).build()
        );
    }

    public static final BlockEntityType<TransportAnchorBlockEntity> TRANSPORT_ANCHOR_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier("slave-tp", "transport_anchor_block_entity"),
                    FabricBlockEntityTypeBuilder.create(TransportAnchorBlockEntity::new, SlaveTp.TRANSPORT_ANCHOR_BLOCK).build()
            );
}
