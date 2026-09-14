package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** All items added by the mod. Add new entries here, then a model + texture + lang key. */
public class ModItems {
	/** Spawn egg colours are taken from the skin: black charro suit, marigold trim. */
	public static final Item MARIACHI_SPAWN_EGG = register("mariachi_spawn_egg",
			new SpawnEggItem(ModEntities.MARIACHI, 0x2B2B2B, 0xE8A13A, new Item.Settings()));

	public static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Glukelonzales.id(name), item);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering items for {}", Glukelonzales.MOD_ID);
	}
}
