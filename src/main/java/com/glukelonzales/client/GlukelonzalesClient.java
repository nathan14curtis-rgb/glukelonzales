package com.glukelonzales.client;

import com.glukelonzales.Glukelonzales;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-only entrypoint: entity renderers, model layers, particles, HUD.
 * Never referenced from common code — the server does not have these classes.
 */
public class GlukelonzalesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// EntityModelLayerRegistry.registerModelLayer(MariachiModel.LAYER, MariachiModel::getTexturedModelData);
		// EntityRendererRegistry.register(ModEntities.MARIACHI, MariachiRenderer::new);

		Glukelonzales.LOGGER.debug("Client init for {}", Glukelonzales.MOD_ID);
	}
}
