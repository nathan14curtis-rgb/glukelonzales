package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.entity.custom.MariachiEntity;
import com.glukelonzales.entity.custom.TacoBossEntity;
import com.glukelonzales.entity.custom.TacoProjectileEntity;
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

	/** Not naturally spawning — summon it with {@code /summon glukelonzales:taco_boss}. */
	public static final EntityType<TacoBossEntity> TACO_BOSS = register("taco_boss",
			EntityType.Builder.create(TacoBossEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.4f, 3.6f)
					.maxTrackingRange(80)
					.build("taco_boss"));

	public static final EntityType<TacoProjectileEntity> TACO_PROJECTILE = register("taco_projectile",
			EntityType.Builder.<TacoProjectileEntity>create(TacoProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.4f, 0.4f)
					.maxTrackingRange(64)
					.trackingTickInterval(10)
					.build("taco_projectile"));

	public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
		return Registry.register(Registries.ENTITY_TYPE, Glukelonzales.id(name), type);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering entities for {}", Glukelonzales.MOD_ID);

		FabricDefaultAttributeRegistry.register(MARIACHI, MariachiEntity.createMariachiAttributes());
		FabricDefaultAttributeRegistry.register(TACO_BOSS, TacoBossEntity.createTacoBossAttributes());
	}
}
