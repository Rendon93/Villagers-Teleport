package slavtp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slavtp.block.AltarBlock;
import slavtp.block.entity.ModBlockEntities;
import slavtp.item.VillagerLinkItem;

public class SlaveTp implements ModInitializer {
	public static final String MOD_ID = "slave-tp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//Delacarion del item
	public static final Item VILLAGER_LINK_ITEM = new VillagerLinkItem(new Item.Settings().maxCount(1));
	public static final Block ALTAR_BLOCK = new AltarBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).requiresTool());
	public static final Item ALTAR_BLOCK_ITEM = new BlockItem(ALTAR_BLOCK, new Item.Settings());

	@Override
	public void onInitialize() {
		LOGGER.info("Inicializando SlaveTp Mod...");

		//Registro del item
		Registry.register(Registries.ITEM, id("villager_link_item"), VILLAGER_LINK_ITEM);
		Registry.register(Registries.BLOCK, id("altar_block"), ALTAR_BLOCK);
		Registry.register(Registries.ITEM, id("altar_block"), ALTAR_BLOCK_ITEM);

		//Registro de Block Entities
		ModBlockEntities.registerBlockEntities();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
