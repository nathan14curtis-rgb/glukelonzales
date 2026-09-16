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

> Scaffolding plus the first creature. Items, sounds and effects registries are wired up
> and still empty, ready for content to be dropped in.

## Creatures

### Mariachi

Player-shaped, but **4 model pixels shorter in the leg** than Steve. Since 16 model pixels
make one block, that is a quarter of a block off both the collision box and the eye height —
width is untouched, because only the legs were shortened.

| | Steve | Mariachi |
|---|---|---|
| Leg height (model px) | 12 | **8** |
| Model height (model px) | 32 | **28** |
| Hitbox width | 0.6 | 0.6 |
| Hitbox height | 1.8 | **1.55** |
| Eye height | 1.62 | **1.37** |

Vanilla proportions run head `-8..0`, body `0..12`, legs `12..24` with the feet resting at
y=24. Shortening the legs on their own would leave him floating, so head, body and arms are
pushed down by the same 4 px and the feet stay planted.

- Skin: `assets/glukelonzales/textures/entity/mariachi.png` — a standard 64x64 skin, with
  the sombrero on the hat layer and the charro suit on the second (overlay) layer.
- Model: `entity/client/MariachiModel.java` — all the sizing constants live here.
- Hitbox: `registry/ModEntities.java` — `MARIACHI_WIDTH` / `MARIACHI_HEIGHT` / `MARIACHI_EYE_HEIGHT`.
- Spawn egg: `mariachi_spawn_egg`, in the Glukelonzales creative tab.
- Natural spawning: every 3 ticks, each player standing in a desert-family biome (desert,
  badlands, eroded/wooded badlands — the terracotta ones) gets a 1% roll to spawn one nearby
  (`entity/custom/MariachiSpawner.java`).
- Give one a `vihuela`, `trumpet`, or `violin` (right-click while it's empty-handed) and it'll
  hold it — that's how the Taco Boss gets summoned, see below.

**Bug fixed:** its feet weren't rendering (looked like it was walking on air). The shortened
leg geometry cuts the *top* of the leg (near the hip) to keep the *bottom* (the feet) touching
the ground, but the texture UV sampling was still reading from vanilla's un-shifted V-coordinate
— so it sampled the top 8 rows of the texture's 12-row leg band (thigh) instead of the bottom 8
(where feet/shoes are drawn), and the feet pixels were never read at all. Fixed by shifting the
UV origin down by `LEG_SHORTENING` on all four leg/pants parts in `MariachiModel`. Same texture,
same model — the Taco Boss (which reuses this model) is fixed by the same change.

## Items

| Item | Notes |
|---|---|
| `sombrero` — "Sombrero de Luke" | A real helmet: diamond-tier durability/protection/enchantability (`ArmorMaterials.DIAMOND`), 3D model ported from [`docs/sombrero-model.md`](docs/sombrero-model.md). Wear it and right-click with an empty main hand to fire a taco for 5 hearts of damage before armor. |
| `taco` | Food: 4 hunger bars. Grants The Mexican Spirit (2x health regen) and Strength V, both for 30 seconds; an 8.7-second jingle plays on eating. |
| `vihuela` / `trumpet` / `violin` | The summoning-ritual instruments — see **The Summoning Ritual** below. Craftable (see `data/glukelonzales/recipe/`). |
| `mariachi_spawn_egg` | Spawns the Mariachi. |
| `lukes_special_egg` | Spawns the Taco Boss. Pastel pink/mint "Easter egg" colors rather than anything drawn from the boss's own palette, so it's easy to pick out in the creative inventory/search. |

**Bug fixed: the sombrero's shoot ability didn't fire.** It was hooked to Fabric's
`UseItemCallback`, which relies on the vanilla client's own decision about whether an empty-hand,
nothing-targeted right-click is even worth sending a packet for — that isn't guaranteed the way
it is for a held item, and in practice it wasn't reliably reaching the server. Replaced with a
direct approach: the client watches the vanilla "use item" key itself every tick
(`SombreroClientHandler`) and, on a rising edge with an empty main hand and the sombrero worn,
sends a small custom packet (`ShootTacoPayload`) straight to the server, which fires the taco
unconditionally (`SombreroTacoAbility`). No dependency on what's being looked at.

## Building

```bash
./gradlew build         # jar lands in build/libs/
./gradlew runClient     # launch a dev client
./gradlew runServer     # launch a dev server
```

(The wrapper — `gradlew` / `gradlew.bat` / `gradle/wrapper/gradle-wrapper.jar` — is committed, so no separate `gradle wrapper` bootstrap step is needed; just make sure you have JDK 21.)

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
├── entity/custom/            Mob classes (AI, attributes)  — MariachiEntity
├── entity/client/            Renderers + models (client only) — MariachiModel, MariachiRenderer
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

A giant, fast, comedic-but-scary boss mob — a giant version of the Mariachi (same skin,
`textures/entity/taco_boss.png`, currently a copy of `mariachi.png`; give it its own look
whenever there's dedicated art, the renderer's texture lookup doesn't care which). Get one with
`lukes_special_egg` (search "Luke's Special Egg" in the creative inventory, right-click the
ground to spawn), or summon it directly for testing:

```
/summon glukelonzales:taco_boss ~ ~ ~
```

**Behavior** (`entity/custom/TacoBossEntity.java` and its `TacoBoss*Goal` classes):

1. **Stalking** — always knows where the nearest player is (no distance cap) and holds a tight
   18-22 block standoff, staring them down. Every player's gaze is tracked
   (`TacoBossStareTrackerGoal`): looking at the boss (within a ~25° cone, with line of sight) for
   **2 continuous seconds** snaps it into Chasing, targeting that player. Looking away decays the
   timer gradually rather than resetting it.
2. **Chasing** — sprints straight at its target at nearly double its stalking speed
   (`TacoBossChargeGoal`). A looping chase track starts the moment it's engaged (Chasing or
   Attacking — see below) and keeps playing across both, anchored to the boss's live position
   client-side (`TacoBossMariachiSound`) — Minecraft's normal distance falloff is what makes it
   swell as the boss closes in, no extra volume code needed. Reaching melee range flips it to
   Attacking.
3. **Attacking** — rapid melee: half a heart (1 damage) roughly every 8 ticks, i.e. ~2.5
   hits/second (`TacoBossRapidMeleeGoal`). A random taunt clip plays every few seconds on top of
   the chase music. Taco projectiles keep firing throughout Chasing/Attacking every 10 ticks
   (`TacoBossRangedAttackGoal` + `TacoProjectileEntity`) — above half health they just deal 4
   direct damage; at or below half health they instead **explode on any impact** (ground or
   player), a small ghast-fireball-style blast with fire disabled (`World.ExplosionSourceType.MOB`,
   power 1.0, same as a ghast fireball).
4. If the target dies, logs off, or gets more than 64 blocks away for 15+ seconds, the boss gives
   up and returns to Stalking (see `TacoBossEntity#tick()`), which also stops the music.

**Health, the boss bar, and the two chase tracks** — the boss has 500 hearts (1000 HP) and shows
a red "Luke Gonzalez" boss bar at the top of the screen (`ServerBossBar`, same mechanism as the
Ender Dragon/Wither) that tracks its health from the moment a player is in render distance. Which
chase track plays is driven by that same health, not by phase:

- **Above half health** — `taco_boss_mariachi.ogg` (the "Normal" recording), full length, looping.
- **At or below half health** — `taco_boss_mariachi_warning.ogg` (the "WARNING LOUD" recording,
  trimmed to drop its first 3 seconds so the loop point is clean), looping, played back at 75%
  volume (25% quieter than the normal track, per spec — the source recording itself runs hot).

If health crosses the halfway line mid-loop, `GlukelonzalesClient` stops the current track and
starts the other one immediately rather than waiting for the loop to finish.

**Audio** — both chase tracks are already in `assets/glukelonzales/sounds/` (converted from the
two recordings with `ffmpeg`, trimming applied to the warning one). Still needed:

- `taco_boss_taunt_1.ogg` through `taco_boss_taunt_5.ogg` — your friend's funny clips. Want more
  or fewer than 5? Add/remove entries in `ModSounds.TACO_BOSS_TAUNTS`, `sounds.json`, and drop
  the matching files.

If your clips aren't already `.ogg`, convert with ffmpeg, e.g. `ffmpeg -i clip.mp3 clip.ogg`.

**Bug fixed: tacos hanging near the boss.** They were spawning dead-center inside its own
(large) hitbox, which could let them re-collide with their own owner instead of flying out
cleanly — most noticeable shooting from directly above. Fixed in `TacoBossRangedAttackGoal` by
spawning them offset outward, toward the target, clear of the boss's bounding box.

## The Summoning Ritual

How you actually get a Taco Boss the "real" way (`lukes_special_egg` still works too, for quick
testing):

1. Get three `MariachiEntity`s standing within 8 blocks of each other.
2. Right-click one with a `vihuela`, one with a `trumpet`, and one with a `violin` (each needs
   an empty hand — see `MariachiEntity#interactMob`).
3. The moment all three instruments are present in one cluster, the ritual song
   (`ritual_song.ogg`) starts playing and a timer begins (`MariachiRitual`, checked once a
   second). If any of the three wander apart, die, or lose their instrument before the song
   finishes, the ritual just quietly fizzles — no partial-progress penalty, you can immediately
   try again.
4. Once the full song has played through, the boss spawns ~50 blocks from whichever player is
   closest to the ritual, standing on the surface (a heightmap lookup with a few retries for
   clear headroom, so it doesn't spawn stuck in terrain or fall from the sky). From there its
   normal Stalking behavior takes over — it already always knows the nearest player and holds
   an 18-22 block standoff, so it closes the gap and starts stalking on its own.

**Recipes** (`data/glukelonzales/recipe/`) — shapes chosen to loosely mirror each instrument
where a 3x3 grid allows it:

```
Vihuela (1 wood, 1 stick, 3 string, 2 iron nuggets)   Violin (1 wood, 1 stick, 2 string, 2 iron nuggets)
 . S .                                                  N . N
 N K N                                                  . K .
 S W S                                                  S W S

Trumpet (3 gold ingots, diagonal tube + bell)
 . . G
 . G .
 G . .
```
(`S`=string, `N`=iron nugget, `K`=stick, `W`=any planks, `G`=gold ingot)

**Tuning knobs** — all the numbers above (detection range, stare-FOV, stare duration, speeds,
attack rate/damage, projectile damage/cooldown, give-up thresholds) are named constants at the
top of `TacoBossEntity` and each goal class. Ask if you want any of them exposed as a config file
instead of hardcoded.

**Distribution** — this is a normal Fabric mod: `./gradlew build` produces a jar in
`build/libs/`, which works the same way any Fabric mod does — install Fabric Loader + Fabric API
for 1.21.1, drop the jar in `mods/`, or bundle it into a modpack (CurseForge/Modrinth/Prism all
support installing a jar this way, or you can publish it there directly).

**Build status** — `./gradlew build` passes clean (compiles and jars) against the exact Fabric
Loader 0.16.5 / Yarn 1.21.1+build.3 versions pinned in `gradle.properties` — actually run, not
just written by hand. A few API mismatches turned up along the way and are now fixed:
`ThrownItemEntity` lives under `entity.projectile.thrown` (not `entity.projectile`) in this Yarn
build, `Entity#damage` takes just `(DamageSource, float)` with no `ServerWorld` param, and the
placeholder boss renderer needed an explicit `getTexture()` override. None of that touched the
actual AI/behavior — it was all in the client-rendering and projectile plumbing.

**Bugs found by actually running the client** — two real issues turned up in `run/logs/latest.log`
after a `runClient` session, both now fixed:
- `sounds.json` had a `"_comment"` entry with a plain string value at the top level. Every entry
  in a Minecraft `sounds.json` must be a sound-definition object — a stray string value there
  threw a `JsonSyntaxException` that invalidated the *entire file*, silently dropping every
  custom sound (including the taco boss's) even though they were correctly registered on the
  Java side. Removed it.
- Two `README.md` files under `assets/glukelonzales/` (in `sounds/` and `models/entity/`) were
  being picked up by the resource-pack scanner and logged as `Invalid path in mod resource-pack`
  warnings (harmless — it just ignores them — but noisy). Moved the useful one to
  [`docs/sombrero-model.md`](docs/sombrero-model.md); anything under `assets/` should be an
  actual resource, not documentation.

Everything else in the log (`No data fixer registered for <entity>`, `Missing sound for event`
for the not-yet-supplied audio files, a `Sampler2` shader warning, missing vanilla goat-horn
sounds) is normal dev-environment noise, not a bug.

## License

MIT — see [LICENSE](LICENSE).
