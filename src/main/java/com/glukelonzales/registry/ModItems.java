package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** All items added by the mod. Add new entries here, then a model + texture + lang key. */
public class ModItems {
	/** A real helmet: diamond-tier durability/protection/enchantability (see
	 *  {@code ArmorMaterials.DIAMOND}) so it takes the same enchantments and repairs the same
	 *  way as a diamond helmet — it's also added to the vanilla {@code #minecraft:head_armor}
	 *  tag (see data/minecraft/tags/item/head_armor.json) which is what every "which
	 *  enchantments/behaviors apply to head armor" check in the game keys off. Its shoot-a-taco
	 *  ability lives in {@code Glukelonzales#onInitialize} (a global empty-hand-right-click
	 *  hook), not here, since that needs to fire regardless of what item triggered the use. */
	public static final Item SOMBRERO = register("sombrero", new ArmorItem(
			ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET,
			new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(33)).maxCount(1)));

	/** 4 hunger bars (8 points), and applies The Mexican Spirit for 10 seconds. */
	public static final Item TACO = register("taco", new Item(new Item.Settings().food(
			new FoodComponent.Builder()
					.nutrition(8)
					.saturationModifier(0.6f)
					.statusEffect(new StatusEffectInstance(ModEffects.MEXICAN_SPIRIT, 200, 0), 1.0f)
					.build())));

	/** Spawn egg colours are taken from the skin: black charro suit, marigold trim. */
	public static final Item MARIACHI_SPAWN_EGG = register("mariachi_spawn_egg",
			new SpawnEggItem(ModEntities.MARIACHI, 0x2B2B2B, 0xE8A13A, new Item.Settings()));

	/** Easter-egg palette (pastel pink/mint) rather than anything drawn from the boss itself —
	 *  makes it easy to spot in the creative inventory/search. */
	public static final Item LUKES_SPECIAL_EGG = register("lukes_special_egg",
			new SpawnEggItem(ModEntities.TACO_BOSS, 0xF7C6D9, 0xB5E8D5, new Item.Settings()));

	public static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Glukelonzales.id(name), item);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering items for {}", Glukelonzales.MOD_ID);
	}
}
