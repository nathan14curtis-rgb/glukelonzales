package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Creature/entity types. Every mob registered here also needs:
 *  - attributes registered (FabricDefaultAttributeRegistry, see below)
 *  - a renderer + model registered client-side in GlukelonzalesClient
 *  - a spawn egg item, lang key, and loot table
 */
public class ModEntities {
	// public static final EntityType<MariachiEntity> MARIACHI = register("mariachi",
	//         EntityType.Builder.create(MariachiEntity::new, SpawnGroup.CREATURE)
	//                 .dimensions(0.6f, 1.95f)
	//                 .build("mariachi"));

	public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
		return Registry.register(Registries.ENTITY_TYPE, Glukelonzales.id(name), type);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering entities for {}", Glukelonzales.MOD_ID);

		// FabricDefaultAttributeRegistry.register(MARIACHI, MariachiEntity.createMariachiAttributes());
	}
}
