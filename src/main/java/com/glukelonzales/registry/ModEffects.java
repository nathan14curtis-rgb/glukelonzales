package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import com.glukelonzales.effect.custom.MexicanSpiritEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/** Status effects (potion effects). Needs a lang key + a 18x18 icon texture. */
public class ModEffects {
	// public static final RegistryEntry<StatusEffect> TACO_POWER =
	//         register("taco_power", new TacoPowerEffect(StatusEffectCategory.BENEFICIAL, 0xE8B24A));

	/** Granted by eating a taco — see {@link MexicanSpiritEffect}. */
	public static final RegistryEntry<StatusEffect> MEXICAN_SPIRIT =
			register("mexican_spirit", new MexicanSpiritEffect(StatusEffectCategory.BENEFICIAL, 0xE8B24A));

	public static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
		StatusEffect registered = Registry.register(Registries.STATUS_EFFECT, Glukelonzales.id(name), effect);
		return Registries.STATUS_EFFECT.getEntry(registered);
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering status effects for {}", Glukelonzales.MOD_ID);
	}
}
