package com.glukelonzales.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.client.MariachiModel;
import com.glukelonzales.entity.client.MariachiRenderer;
import com.glukelonzales.entity.client.SombreroArmorRenderer;
import com.glukelonzales.entity.client.SombreroModel;
import com.glukelonzales.entity.custom.TacoBossEntity;
import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItems;
import com.glukelonzales.registry.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-only entrypoint: entity renderers, model layers, particles, HUD.
 * Never referenced from common code — the server does not have these classes.
 *
 * NOTE on the renderers below: the taco boss reuses MariachiModel (same skin, same shortened-leg
 * proportions the texture was actually drawn for) scaled up 2.1x — it's meant to be a giant
 * version of the same character. Swap in dedicated art/model whenever it exists; none of the
 * AI/damage/sound logic depends on it.
 */
public class GlukelonzalesClient implements ClientModInitializer {
	private static final Map<Integer, TacoBossMariachiSound> ACTIVE_MARIACHI = new HashMap<>();

	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(MariachiModel.LAYER, MariachiModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MARIACHI, MariachiRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(SombreroModel.LAYER, SombreroModel::getTexturedModelData);
		ArmorRenderer.register(new SombreroArmorRenderer(), ModItems.SOMBRERO);

		EntityRendererRegistry.register(ModEntities.TACO_BOSS, context ->
				new MobEntityRenderer<>(context, new MariachiModel<>(context.getPart(MariachiModel.LAYER)), 1.6f) {
					@Override
					public Identifier getTexture(TacoBossEntity entity) {
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

	/** Normal-track playback volume; the warning track is played back {@link #WARNING_VOLUME}
	 *  instead (quieter, per spec, since the source recording itself runs louder). */
	private static final float NORMAL_VOLUME = 1.0F;
	private static final float WARNING_VOLUME = 0.75F; // 25% quieter than normal

	/** Starts/stops/swaps the looping chase track for every taco boss in render distance, based
	 *  on its synced phase (playing at all) and current health (which of the two tracks). See
	 *  {@link TacoBossMariachiSound} for why the sound itself needs no distance/volume math
	 *  beyond that 25% offset. */
	private static void updateMariachiSounds(MinecraftClient client) {
		if (client.world == null) {
			ACTIVE_MARIACHI.clear();
			return;
		}

		for (var entity : client.world.getEntities()) {
			if (!(entity instanceof TacoBossEntity boss)) {
				continue;
			}
			boolean shouldPlay = boss.isAlive() && boss.getPhase() != TacoBossEntity.Phase.STALKING;
			TacoBossMariachiSound existing = ACTIVE_MARIACHI.get(boss.getId());

			if (!shouldPlay) {
				if (existing != null) {
					ACTIVE_MARIACHI.remove(boss.getId());
				}
				continue;
			}

			boolean warning = boss.isBelowHalfHealth();
			SoundEvent desiredTrack = warning ? ModSounds.TACO_BOSS_MARIACHI_WARNING : ModSounds.TACO_BOSS_MARIACHI;

			if (existing != null && existing.getTrackId() != desiredTrack) {
				// Health crossed the halfway line mid-loop — cut the old track and swap in the other.
				client.getSoundManager().stop(existing);
				existing = null;
			}

			if (existing == null) {
				float volume = warning ? WARNING_VOLUME : NORMAL_VOLUME;
				TacoBossMariachiSound sound = new TacoBossMariachiSound(boss, desiredTrack, volume);
				ACTIVE_MARIACHI.put(boss.getId(), sound);
				client.getSoundManager().play(sound);
			}
		}

		ACTIVE_MARIACHI.keySet().removeIf(id -> client.world.getEntityById(id) == null);
	}
}
