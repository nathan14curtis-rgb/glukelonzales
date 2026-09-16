package com.glukelonzales.client;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.client.MariachiRenderer;
import com.glukelonzales.entity.client.SombreroArmorRenderer;
import com.glukelonzales.entity.client.SombreroModel;
import com.glukelonzales.entity.custom.MariachiRitual;
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
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-only entrypoint: entity renderers, model layers, particles, HUD.
 * Never referenced from common code — the server does not have these classes.
 *
 * NOTE on the renderers below: the taco boss reuses the stock player model scaled up 2.1x —
 * it's meant to be a giant version of the same character. Swap in dedicated art/model whenever
 * it exists; none of the AI/damage/sound logic depends on it.
 */
public class GlukelonzalesClient implements ClientModInitializer {
	private static final Map<Integer, TacoBossMariachiSound> ACTIVE_MARIACHI = new HashMap<>();
	private static final Set<Integer> TRIGGERED_RITUAL_MARKERS = new HashSet<>();

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.MARIACHI, MariachiRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(SombreroModel.LAYER, SombreroModel::getTexturedModelData);
		ArmorRenderer.register(new SombreroArmorRenderer(), ModItems.SOMBRERO);

		EntityRendererRegistry.register(ModEntities.TACO_BOSS, context ->
				new MobEntityRenderer<>(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 1.6f) {
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
		ClientTickEvents.END_CLIENT_TICK.register(GlukelonzalesClient::updateRitualSound);
		SombreroClientHandler.register();

		Glukelonzales.LOGGER.debug("Client init for {}", Glukelonzales.MOD_ID);
	}

	/** Normal-track playback volume; the warning track is played back {@link #WARNING_VOLUME}
	 *  instead (quieter, per spec, since the source recording itself runs louder). Both halved
	 *  again from their original values per a later "turn all the songs down" request. */
	private static final float NORMAL_VOLUME = 0.5F;
	private static final float WARNING_VOLUME = 0.375F; // 25% quieter than normal, same ratio as before

	/** Starts/stops/swaps the looping chase track for every taco boss in render distance, based
	 *  on its synced phase (playing at all) and current health (which of the two tracks). See
	 *  {@link TacoBossMariachiSound} for why the sound itself needs no distance/volume math
	 *  beyond that 25% offset — it's a positioned sound, so normal engine distance falloff
	 *  already makes it (and everything else here) quieter the farther away you are. */
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

	/** The ritual song is anchored to a single invisible marker entity (see {@link
	 *  MariachiRitual}) rather than broadcast directly, specifically so it only ever plays once
	 *  no matter how the server-side ritual bookkeeping gets re-evaluated — each marker triggers
	 *  its one-shot, non-looping playback exactly once here, the first tick it's seen. */
	private static void updateRitualSound(MinecraftClient client) {
		if (client.world == null) {
			TRIGGERED_RITUAL_MARKERS.clear();
			return;
		}

		for (var entity : client.world.getEntities()) {
			if (!(entity instanceof ArmorStandEntity stand) || !MariachiRitual.isRitualMarker(stand)) {
				continue;
			}
			if (TRIGGERED_RITUAL_MARKERS.add(stand.getId())) {
				// Was 4.0 before the "turn all the songs down 50%" request.
				var sound = new PositionedSoundInstance(ModSounds.RITUAL_SONG, SoundCategory.RECORDS,
						2.0F, 1.0F, stand.getRandom(), stand.getX(), stand.getY(), stand.getZ());
				client.getSoundManager().play(sound);
			}
		}

		TRIGGERED_RITUAL_MARKERS.removeIf(id -> client.world.getEntityById(id) == null);
	}
}
