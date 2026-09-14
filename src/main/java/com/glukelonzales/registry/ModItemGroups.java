package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/** Creative-inventory tab that holds the mod's items and spawn eggs. */
public class ModItemGroups {
	public static final ItemGroup GLUKELONZALES_GROUP = Registry.register(
			Registries.ITEM_GROUP,
			Glukelonzales.id("glukelonzales"),
			ItemGroup.create(ItemGroup.Row.TOP, 7)
					.displayName(Text.translatable("itemgroup.glukelonzales"))
					.icon(() -> new ItemStack(ModItems.SOMBRERO))
					.entries((displayContext, entries) -> {
						entries.add(ModItems.SOMBRERO);
						entries.add(ModItems.TACO);
						entries.add(ModItems.MARIACHI_SPAWN_EGG);
					})
					.build());

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering item groups for {}", Glukelonzales.MOD_ID);
	}
}
