package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** All items added by the mod. Add new entries here, then a model + texture + lang key. */
public class ModItems {
	// public static final Item TACO = register("taco", new Item(new Item.Settings().food(ModFoodComponents.TACO)));

	public static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Glukelonzales.id(name), item);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering items for {}", Glukelonzales.MOD_ID);
	}
}
