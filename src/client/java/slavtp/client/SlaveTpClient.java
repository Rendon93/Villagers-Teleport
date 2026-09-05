package slavtp.client;

import slavtp.SlaveTp;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import slavtp.block.entity.ModBlockEntities;
import slavtp.client.render.AltarBlockEntityRenderer;
import slavtp.client.render.TransportAnchorBlockEntityRenderer;

public class SlaveTpClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BlockRenderLayerMap.INSTANCE.putBlock(SlaveTp.ALTAR_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(SlaveTp.TRANSPORT_ANCHOR_BLOCK, RenderLayer.getTranslucent());

		// Registro del renderizador para proyectar el círculo mágico sobre la obsidiana
		BlockEntityRendererFactories.register(ModBlockEntities.ALTAR_BLOCK_ENTITY, AltarBlockEntityRenderer::new);

		BlockEntityRendererFactories.register(
				ModBlockEntities.TRANSPORT_ANCHOR_BLOCK_ENTITY,
				TransportAnchorBlockEntityRenderer::new
		);
	}
}