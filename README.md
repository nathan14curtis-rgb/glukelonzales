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

## License

MIT — see [LICENSE](LICENSE).
