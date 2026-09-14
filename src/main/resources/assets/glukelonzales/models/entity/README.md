# Entity models

`sombrero.geo.json` is a **Bedrock** geometry file (`format_version` 1.12.0, a Blockbench
export). Minecraft **Java** does not load these at runtime — Java entity and armor models are
built in code as `ModelPart` trees, not from JSON.

It is kept here as the authoritative source for the sombrero's shape. To put the hat on a head
in game, its cubes need porting into a Java model class under
`com.glukelonzales.entity.client` (the same way `MariachiModel` is written), paired with
`textures/entity/sombrero.png` (128x128, matching the file's `texture_width`/`texture_height`).

Cubes, in Bedrock coordinates (`origin` is the corner, y=24 is the top of the head):

| origin | size | uv | what |
|---|---|---|---|
| -16, 24.5, -16 | 32 x 1 x 32 | 0, 0 | outer brim |
| -14, 25.5, -14 | 28 x 1 x 28 | 0, 34 | inner brim |
| -5, 25.5, -5 | 10 x 9 x 10 | 0, 64 | crown |
| -6, 26, -6 | 12 x 2 x 12 | 44, 64 | hat band |
| -4, 34.5, -4 | 8 x 1 x 8 | 44, 84 | crown cap |
