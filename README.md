# Glukelonzales

*mariachi music*

*taco power as well*

A Minecraft mod adding new **creatures**, **items**, **sounds**, and **status effects**.

| | |
|---|---|
| Mod loader | Fabric |
| Minecraft | 1.21.1 |
| Java | 21 |
| Mod ID | `glukelonzales` |

> This repo currently contains the **scaffolding only** — every registry is wired up and empty,
> ready for content to be dropped in.

## Building

```bash
gradle wrapper          # first time only, generates ./gradlew
./gradlew build         # jar lands in build/libs/
./gradlew runClient     # launch a dev client
./gradlew runServer     # launch a dev server
```

## Project layout

```
build.gradle                  Fabric Loom build; versions live in gradle.properties
gradle.properties             Minecraft / Yarn / Loader / Fabric API versions
src/main/java/com/glukelonzales/
├── Glukelonzales.java        Common entrypoint — calls every ModX.register()
├── registry/
│   ├── ModItems.java         Item registry
│   ├── ModItemGroups.java    Creative tab
│   ├── ModEntities.java      Creature/entity types + attributes
│   ├── ModSounds.java        SoundEvent registry
│   └── ModEffects.java       Status effect registry
├── entity/custom/            Mob classes (AI, attributes)  — MariachiEntity is the template
├── entity/client/            Renderers + models (client only)
├── item/custom/              Items with behaviour
├── effect/custom/            Status effect classes — TacoPowerEffect is the template
├── client/GlukelonzalesClient.java   Client entrypoint — renderers, model layers
└── util/                     Shared helpers
src/main/resources/
├── fabric.mod.json           Mod metadata + entrypoints
├── glukelonzales.mixins.json Mixin config (empty)
├── assets/glukelonzales/
│   ├── lang/en_us.json       All display names
│   ├── sounds.json           Sound event definitions
│   ├── sounds/               .ogg audio files
│   ├── models/item/          Item models
│   ├── models/entity/        Entity model JSON (if not hardcoded)
│   └── textures/
│       ├── item/             Item textures (16x16)
│       ├── entity/           Mob textures
│       └── mob_effect/       Effect icons (18x18)
└── data/glukelonzales/
    ├── loot_table/entities/  Mob drops
    ├── recipe/               Crafting recipes
    └── tags/                 Item + entity type tags
```

## Adding content

**An item** → register in `ModItems`, add to `ModItemGroups`, then add
`models/item/<id>.json`, `textures/item/<id>.png`, and an `item.glukelonzales.<id>` lang key.

**A creature** → new class in `entity/custom/`, register the `EntityType` and its attributes in
`ModEntities`, register a renderer + model layer in `GlukelonzalesClient`, then add a texture,
a loot table, a spawn egg, and an `entity.glukelonzales.<id>` lang key.

**A sound** → drop the `.ogg` in `assets/glukelonzales/sounds/`, define it in `sounds.json`,
register the `SoundEvent` in `ModSounds`, and add a `subtitles.glukelonzales.<id>` lang key.

**An effect** → new class in `effect/custom/`, register in `ModEffects`, add an 18x18 icon to
`textures/mob_effect/` and an `effect.glukelonzales.<id>` lang key.

## The Taco Boss

A giant, fast, comedic-but-scary boss mob. No custom model/texture yet (it renders as an
oversized placeholder — see the note in `GlukelonzalesClient`) — this pass is entirely about
behavior. Summon it for testing with:

```
/summon glukelonzales:taco_boss ~ ~ ~
```

**Behavior** (`entity/custom/TacoBossEntity.java` and its `TacoBoss*Goal` classes):

1. **Stalking** — lurks 9-18 blocks from the nearest player, doesn't attack. Every nearby
   player's gaze is tracked (`TacoBossStareTrackerGoal`): looking at the boss (within a ~25°
   cone, with line of sight) for **3 continuous seconds** snaps it into Chasing, targeting that
   player. Looking away decays the timer gradually rather than resetting it.
2. **Chasing** — sprints straight at its target at nearly double its stalking speed
   (`TacoBossChargeGoal`). A looping mariachi track starts, anchored to the boss's live position
   client-side (`TacoBossMariachiSound`) — Minecraft's normal distance falloff is what makes it
   swell as the boss closes in, no extra volume code needed. Reaching melee range flips it to
   Attacking.
3. **Attacking** — rapid melee: half a heart (1 damage) roughly every 8 ticks, i.e. ~2.5
   hits/second (`TacoBossRapidMeleeGoal`). The mariachi track cuts out and a random taunt clip
   plays every few seconds instead. Taco projectiles keep firing throughout Chasing/Attacking on
   a ~3.5s cooldown (`TacoBossRangedAttackGoal` + `TacoProjectileEntity`, 4 damage on a direct
   hit).
4. If the target dies, logs off, or gets more than 64 blocks away for 15+ seconds, the boss gives
   up and returns to Stalking (see `TacoBossEntity#tick()`).

**Audio you need to supply** — drop these `.ogg` files in `assets/glukelonzales/sounds/`
(already wired up in `sounds.json` and `ModSounds`):

- `taco_boss_mariachi.ogg` — the chase music (loops; keep it seamless-loopable).
- `taco_boss_taunt_1.ogg` through `taco_boss_taunt_5.ogg` — your friend's funny clips. Want more
  or fewer than 5? Add/remove entries in `ModSounds.TACO_BOSS_TAUNTS`, `sounds.json`, and drop
  the matching files.

If your clips aren't already `.ogg`, convert with ffmpeg, e.g. `ffmpeg -i clip.mp3 clip.ogg`.

**Tuning knobs** — all the numbers above (detection range, stare-FOV, stare duration, speeds,
attack rate/damage, projectile damage/cooldown, give-up thresholds) are named constants at the
top of `TacoBossEntity` and each goal class. Ask if you want any of them exposed as a config file
instead of hardcoded.

**Distribution** — this is a normal Fabric mod: `./gradlew build` produces a jar in
`build/libs/`, which works the same way any Fabric mod does — install Fabric Loader + Fabric API
for 1.21.1, drop the jar in `mods/`, or bundle it into a modpack (CurseForge/Modrinth/Prism all
support installing a jar this way, or you can publish it there directly).

**A note on confidence** — this was written against Fabric Loader 0.16.5 / Yarn 1.21.1+build.3
(from `gradle.properties`) without being compiled in the environment that wrote it (no working
Gradle/JDK 21 available there). The AI/damage/movement logic uses long-stable APIs I'm confident
in. If the build fails, the most likely spots to double-check first are the client-rendering
placeholders in `GlukelonzalesClient` (`MobEntityRenderer`/`PlayerEntityModel` constructor args,
the `scale()` override) and `FlyingItemEntityRenderer` for the projectile — none of that affects
the actual boss behavior, so a rendering tweak there won't touch the logic described above.

## License

MIT — see [LICENSE](LICENSE).
