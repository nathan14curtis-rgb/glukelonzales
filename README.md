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

## License

MIT — see [LICENSE](LICENSE).
