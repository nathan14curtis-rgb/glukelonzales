package com.glukelonzales.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.client.MariachiModel;
import com.glukelonzales.entity.client.MariachiRenderer;
import com.glukelonzales.entity.custom.TacoBossEntity;
import com.glukelonzales.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-only entrypoint: entity renderers, model layers, particles, HUD.
 * Never referenced from common code — the server does not have these classes.
 *
 * NOTE on the renderers below: the Mariachi has a real model and skin. The taco boss is still a
 * placeholder that exists only so the client doesn't crash when it spawns — it reuses the vanilla
 * player model scaled up. Swap it out for real art whenever it's ready; none of the AI/damage/sound
 * logic depends on it.
 */
public class GlukelonzalesClient implements ClientModInitializer {
	private static final Map<Integer, TacoBossMariachiSound> ACTIVE_MARIACHI = new HashMap<>();

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(MariachiModel.LAYER, MariachiModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MARIACHI, MariachiRenderer::new);

		EntityRendererRegistry.register(ModEntities.TACO_BOSS, context ->
				new MobEntityRenderer<>(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 1.6f) {
					@Override
					public Identifier getTexture(TacoBossEntity entity) {
						// Placeholder: no real skin yet, so this resolves to Minecraft's
						// missing-texture checkerboard. Swap in a real texture identifier here.
						return Identifier.of("glukelonzales", "textures/entity/taco_boss.png");
					}

					@Override
					protected void scale(TacoBossEntity entity, MatrixStack matrices, float amount) {
						float scale = 2.1f;
						matrices.scale(scale, scale, scale);
					}
				});

		EntityRendererRegistry.register(ModEntities.TACO_PROJECTILE, FlyingItemEntityRenderer::new);

		ClientTickEvents.END_CLIENT_TICK.register(GlukelonzalesClient::updateMariachiSounds);

		Glukelonzales.LOGGER.debug("Client init for {}", Glukelonzales.MOD_ID);
	}

	/** Starts/stops the looping chase track for every taco boss in render distance based on its
	 *  synced phase. See {@link TacoBossMariachiSound} for why the sound itself needs no volume math. */
	private static void updateMariachiSounds(MinecraftClient client) {
		if (client.world == null) {
			ACTIVE_MARIACHI.clear();
			return;
		}

		for (var entity : client.world.getEntities()) {
			if (!(entity instanceof TacoBossEntity boss)) {
				continue;
			}
			boolean shouldPlay = boss.isAlive() && boss.getPhase() == TacoBossEntity.Phase.CHASING;
			TacoBossMariachiSound existing = ACTIVE_MARIACHI.get(boss.getId());

			if (shouldPlay && existing == null) {
				TacoBossMariachiSound sound = new TacoBossMariachiSound(boss);
				ACTIVE_MARIACHI.put(boss.getId(), sound);
				client.getSoundManager().play(sound);
			} else if (!shouldPlay && existing != null) {
				ACTIVE_MARIACHI.remove(boss.getId());
			}
		}

		ACTIVE_MARIACHI.keySet().removeIf(id -> client.world.getEntityById(id) == null);
	}
}
