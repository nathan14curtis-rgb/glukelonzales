package com.glukelonzales.registry;

import com.glukelonzales.Glukelonzales;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Sound events. Each entry also needs a matching key in
 * assets/glukelonzales/sounds.json pointing at an .ogg file.
 */
public class ModSounds {
	// public static final SoundEvent MARIACHI_MUSIC = register("mariachi_music");

	/** Looping chase track while the boss is above half health. */
	public static final SoundEvent TACO_BOSS_MARIACHI = register("taco_boss_mariachi");

	/** Looping chase track once the boss drops to half health or below — louder recording,
	 *  played back quieter (see TacoBossMariachiSound) than the normal track. */
	public static final SoundEvent TACO_BOSS_MARIACHI_WARNING = register("taco_boss_mariachi_warning");

	/** The funny voice clips, picked at random once the boss starts attacking. Add/remove
	 *  entries from both this list and sounds.json to change how many there are. */
	public static final SoundEvent TACO_BOSS_TAUNT_1 = register("taco_boss_taunt_1");
	public static final SoundEvent TACO_BOSS_TAUNT_2 = register("taco_boss_taunt_2");
	public static final SoundEvent TACO_BOSS_TAUNT_3 = register("taco_boss_taunt_3");
	public static final SoundEvent TACO_BOSS_TAUNT_4 = register("taco_boss_taunt_4");
	public static final SoundEvent TACO_BOSS_TAUNT_5 = register("taco_boss_taunt_5");

	public static final List<SoundEvent> TACO_BOSS_TAUNTS = List.of(
			TACO_BOSS_TAUNT_1, TACO_BOSS_TAUNT_2, TACO_BOSS_TAUNT_3, TACO_BOSS_TAUNT_4, TACO_BOSS_TAUNT_5);

	/** Plays once, for the first 8.7 seconds of The Mexican Spirit / Strength effects (30s
	 *  total) granted by eating a taco. */
	public static final SoundEvent MEXICAN_SPIRIT_JINGLE = register("mexican_spirit_jingle");

	/** The summoning ritual's song — plays once, in full, while three mariachis (one each with
	 *  a vihuela, trumpet, and violin) perform together. See MariachiRitual. */
	public static final SoundEvent RITUAL_SONG = register("ritual_song");

	public static SoundEvent register(String name) {
		Identifier id = Glukelonzales.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void register() {
		Glukelonzales.LOGGER.debug("Registering sounds for {}", Glukelonzales.MOD_ID);
	}
}
