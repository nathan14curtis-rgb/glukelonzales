package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.custom.MariachiEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Creature/entity types. Every mob registered here also needs:
 *  - attributes registered (see {@link #register()})
 *  - a renderer + model layer registered client-side in GlukelonzalesClient
 *  - a spawn egg item, lang key, and loot table
 */
public class ModEntities {
	/**
	 * Hitbox for the Mariachi. He is built on the vanilla player model but his legs are
	 * 4 model pixels shorter (see MariachiModel), and 4 px is a quarter of a block, so his
	 * collision box and eye height both sit 0.25 below a player's 1.8 / 1.62. Width is
	 * unchanged — only the legs were shortened, not the body.
	 */
	public static final float MARIACHI_WIDTH = 0.6f;
	public static final float MARIACHI_HEIGHT = 1.55f;   // player 1.8 - 0.25
	public static final float MARIACHI_EYE_HEIGHT = 1.37f; // player 1.62 - 0.25

	public static final EntityType<MariachiEntity> MARIACHI = register("mariachi",
			EntityType.Builder.create(MariachiEntity::new, SpawnGroup.CREATURE)
					.dimensions(MARIACHI_WIDTH, MARIACHI_HEIGHT)
					.eyeHeight(MARIACHI_EYE_HEIGHT)
					.maxTrackingRange(10)
					.build("mariachi"));

	public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
		return Registry.register(Registries.ENTITY_TYPE, Glukelonzales.id(name), type);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering entities for {}", Glukelonzales.MOD_ID);

		FabricDefaultAttributeRegistry.register(MARIACHI, MariachiEntity.createMariachiAttributes());
	}
}
