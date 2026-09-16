package com.glukelonzales;

import com.glukelonzales.entity.custom.MariachiRitual;
import com.glukelonzales.entity.custom.MariachiSpawner;
import com.glukelonzales.item.custom.SombreroTacoAbility;
import com.glukelonzales.registry.ModEffects;
import com.glukelonzales.registry.ModEntities;
import com.glukelonzales.registry.ModItemGroups;
import com.glukelonzales.registry.ModItems;
import com.glukelonzales.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (client + server) entrypoint. Everything the mod adds is registered from here.
 */
public class Glukelonzales implements ModInitializer {
	public static final String MOD_ID = "glukelonzales";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Helper so every registry name is namespaced consistently. */
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModSounds.register();
		ModEffects.register();
		ModEntities.register();
		ModItems.register();
		ModItemGroups.register();
		SombreroTacoAbility.register();
		MariachiSpawner.register();
		MariachiRitual.register();

		LOGGER.info("[{}] mariachi music starts playing", MOD_ID);
	}
}
