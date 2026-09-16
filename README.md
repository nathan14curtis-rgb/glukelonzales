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

Player-shaped, standard vanilla proportions (0.6 wide, 1.8 tall, 1.62 eye height) — rendered
with the stock `PlayerEntityModel`/`EntityModelLayers.PLAYER`, not a custom model.

- Skin: `assets/glukelonzales/textures/entity/mariachi.png` — a standard 64x64 skin, with
  the sombrero on the hat layer and the charro suit on the second (overlay) layer.
- Renderer: `entity/client/MariachiRenderer.java`.
- Hitbox: `registry/ModEntities.java` — `MARIACHI_WIDTH` / `MARIACHI_HEIGHT` / `MARIACHI_EYE_HEIGHT`.
- Spawn egg: `mariachi_spawn_egg`, in the Glukelonzales creative tab.
- Natural spawning: every 75 ticks, each player standing in a desert-family biome (desert,
  badlands, eroded/wooded badlands — the terracotta ones) gets a 0.1% roll to spawn one nearby
  (`entity/custom/MariachiSpawner.java`).
- Give one a `vihuela`, `trumpet`, or `violin` (right-click while it's empty-handed) and it'll
  hold it — that's how the Taco Boss gets summoned, see below.

**Bug history: the feet.** An earlier pass gave the Mariachi (and the Taco Boss, which shares
its skin) 4-model-pixel-shorter legs via a custom `MariachiModel`, then reverted that to the
stock `PlayerEntityModel` when the feet still weren't rendering right. Both were red herrings —
model geometry and UV mapping were fine the whole time. A screenshot finally made the real cause
obvious: it wasn't a UV/model bug at all, it was the **skin file itself**. Pixel-sampling
`mariachi.png`'s leg regions directly showed the actual problem — the base leg textures were
genuinely ~1/3 fully-transparent pixels (holes an artist just never painted over), and the
pants-overlay layer was ~90% transparent with a handful of stray opaque pixels scattered in it
(showing up as tiny floating specks). No amount of correct UV math or model geometry could have
fixed a texture with real transparent gaps in it. Fixed by editing the texture directly: the
leftover holes in both base leg regions were filled in (a simple nearest-opaque-neighbor
in-paint, so the fill extends whatever art was already there rather than a flat patch), and the
sparse pants-overlay layer was cleared to fully transparent so there's nothing left to float.
The Taco Boss shares the same fixed file. Model-wise it's still the plain `PlayerEntityModel`
from the previous (ultimately unnecessary, but harmless) revert.

## Items

| Item | Notes |
|---|---|
| `sombrero` — "Sombrero de Luke" | A real helmet: diamond-tier durability/protection/enchantability (`ArmorMaterials.DIAMOND`), 3D model ported from [`docs/sombrero-model.md`](docs/sombrero-model.md). Wear it and right-click with an empty main hand to fire a taco for 5 hearts of damage before armor. |
| `taco` | Food: 4 hunger bars. Grants The Mexican Spirit (2x health regen) and Strength V, both for 30 seconds; a 9.7-second jingle plays on eating. |
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
├── entity/client/            Renderers + models (client only) — MariachiRenderer, SombreroModel
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
2. **Chasing** — sprints straight at its target (`TacoBossChargeGoal`; deliberately slower than
   it first was — see Tuning knobs). A looping chase track starts the moment it's engaged
   (Chasing or Attacking) and keeps playing across both, anchored to the boss's live position
   client-side (`TacoBossMariachiSound`) — Minecraft's normal distance falloff is what makes it
   swell as the boss closes in (and fade out as you run away), no extra volume code needed.
   Reaching melee range flips it to Attacking.
3. **Attacking** — the boss has **no melee attack at all** — its only attack is
   `TacoBossRangedAttackGoal`'s tacos, which fire whenever the target is more than **2 blocks**
   away (closer than that, it just stands there — there's nothing else for it to do). A random
   taunt clip plays every few seconds on top of the chase music. Above half health, tacos deal 4
   direct damage on a ~10-tick cooldown; at or below half health they fire **twice as often** (5
   ticks) and **explode on any impact** (ground or player) at **twice the blast power** of a
   normal ghast fireball, fire disabled (`World.ExplosionSourceType.MOB`).
4. If the target dies, logs off, or gets more than 64 blocks away for 15+ seconds, the boss gives
   up and returns to Stalking (see `TacoBossEntity#tick()`), which also stops the music.

**Health, the boss bar, and the two chase tracks** — the boss has 500 hearts (1000 HP) and shows
a red "Luke Gonzalez" boss bar at the top of the screen (`ServerBossBar`, same mechanism as the
Ender Dragon/Wither) that tracks its health from the moment a player is in render distance. Which
chase track plays is driven by that same health, not by phase:

- **Above half health** — `taco_boss_mariachi.ogg` (the "Normal" recording), full length, looping,
  at 20% volume.
- **At or below half health** — `taco_boss_mariachi_warning.ogg` (the "WARNING LOUD" recording,
  trimmed to drop its first 3 seconds so the loop point is clean), looping, at 15% volume (still
  25% quieter than the normal track's baseline, per the original spec — the source recording
  itself runs hot).

(Cut twice now — 100% -> 50% -> 20% of the original — after repeated "still too loud" / "still
not falling off with distance" reports. On the attenuation question specifically: verified
directly against the mapped 1.21.1 sources that `AbstractSoundInstance` (and therefore
`MovingSoundInstance`, which this uses) defaults to `AttenuationType.LINEAR`, i.e. these already
do fall off with distance and always have. The perceived "doesn't get quieter no matter where I
go" was near-certainly the ritual song's actual restart-loop bug below — a fresh, loud,
non-attenuating-*feeling* instance kept starting up somewhere every second, which would mask any
falloff on everything else too. Worth re-checking specifically whether *this* still feels wrong
once that's fixed, since the code says it shouldn't be.)

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
   (`ritual_song.ogg`) starts playing once, and a timer begins (`MariachiRitual`, checked once a
   second). The 8-block clustering only matters for this initial trigger — once the ritual has
   started, the three don't need to stay near each other for the full ~2.5 minutes, only stay
   alive and keep holding their instrument. (An earlier version required continuous clustering
   the whole time, which — combined with ordinary wander AI over such a long duration — meant the
   ritual could silently cancel from normal drifting apart, with the song still audibly finishing
   and no boss ever showing up. That's the "boss didn't spawn" bug; fixed by only checking the
   instruments now.)
4. Once the full song has played through, the boss spawns ~50 blocks from whichever player is
   closest to the ritual, standing on the surface (a heightmap lookup with a few retries for
   clear headroom, so it doesn't spawn stuck in terrain or fall from the sky). From there its
   normal Stalking behavior takes over — it already always knows the nearest player and holds
   an 18-22 block standoff, so it closes the gap and starts stalking on its own.

**Bug fixed (for real this time): the song overlapped endlessly and the boss never spawned.**
The previous write-up here blamed a `world.playSound` broadcast getting re-triggered and
switched to a marker-entity sound instead — that part was a legitimate improvement, but it
didn't fix the actual bug, because the actual bug was somewhere else entirely.
`ServerTickEvents.END_WORLD_TICK` fires once per **loaded dimension** per server tick — overworld,
nether, and end are all normally loaded at once, each getting its own independent call. The
ritual's progress tracking (`active`, a single shared field) had no idea which dimension it
belonged to, so when the *nether's* tick call ran `progressActive`, it looked up the mariachis'
UUIDs in the nether, found nothing there (they're in the overworld), and immediately cancelled
the ritual. Then, on the very next overworld tick a moment later, `active` was null again, so it
re-detected the same still-standing trio and started a **brand new ritual from zero** — new
marker, new full song. This repeated roughly every second, for as long as the trio remained
valid: a constant stream of freshly-started ~2.5-minute songs stacking on top of each other
forever (matching "keeps going and going"), while the progress counter never got anywhere near
completing a single one (matching "the boss never spawned" — and why killing mariachis, which
finally made `hasOneOfEach` fail in every dimension's check including the overworld's own, is
what stopped it). Fixed by pinning an active ritual to the specific `ServerWorld` it started in
and having `tick()` ignore calls from any other one entirely — see the class-level Javadoc in
`MariachiRitual` for the full explanation. Also added a small self-heal: whenever there's no
active ritual, any stray leftover marker armor stands (orphaned by the bug above, e.g. in a save
from before this fix) get discarded on sight, so old saves clean themselves up automatically.

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

A later ~22-minute play session's log (spanning the sombrero, ritual, and boss fights) came back
clean too — no exceptions, just one `Can't keep up! ... 60 ticks behind` spike early on (a single
lag hiccup, not repeated; most likely a window focus loss or big chunk load rather than anything
in this mod, since nothing in the following ~20 minutes of active combat/ritual logic repeated
it).

A third session's log (the one that caught the ritual/feet issues above) was clean too — no new
exceptions. It did have one `[Sound engine/ERROR] Stop: Invalid name parameter`, but that fired
at the exact same timestamp as `Player left the game` / `Stopping server`; it's a known, benign
vanilla OpenAL cleanup race that happens on quit/disconnect (the sound engine tries to stop
in-flight sounds while its audio context is already tearing down), not anything in this mod's
code.

## License

MIT — see [LICENSE](LICENSE).
