# Final Paradox boss reconstruction baseline

## Authoritative references

- World archive: `Final_Paradox_v1.1.15.zip`
  - SHA-256: `C9A0DBCFBFFC84A128F4ED8C0E0A2D5703D0F7AC16A1CCF93B57B166F5755EC0`
  - Unpacked reference: `.codex-tmp/fp-world-1.1.15/Final_Paradox_v1.1.15`
  - Minecraft version: 1.16.5
  - DataVersion: 2586
- Chinese translation archive: `最终悖论汉化资源包V3-VM汉化组.zip`
  - SHA-256: `9CD00841C08302B502F76E1D7A252A7DE5F4EC40C2CCCCAE1859B73FD89D800C`
  - Language file: `assets/vm/lang/zh_cn.json`
  - Translation keys: 12,879
  - Custom keys referenced by B1-B11: 4,657; missing from V3: 0

Source priority:

1. Runtime behavior and timing: v1.1.15 datapack command chains.
2. Arena blocks and spatial layout: v1.1.15 world region data.
3. Models, textures, sounds, and particles: official resource pack plus runtime summons.
4. Chinese names, dialogue, and hints: VM Chinese translation V3, copied exactly.

The original archive is never opened or upgraded in Minecraft 1.20.1. All inspection and arena export use the unpacked reference copy.

## Target and build baseline

- Minecraft 1.20.1
- Forge 47.4.20
- Java 17 (`C:\Program Files\Java\jdk-17`)
- Gradle 8.8 wrapper
- `compileJava`: passed
- `build`: passed
- Built and synchronized JAR SHA-256 after the corrected complete B1 arena export: `C756EB8201C1754D4869FC8E2FD125B59B1174878661499081D43292B022D7B7`

## Current mod status

- B1 Apiglo has a registered boss entity, renderer, spawn egg, music, dialogue, persistence, three combat phases, and two intermissions. It uses a relative anchor but is not yet owned by a shared encounter instance. Its original arena geometry has now been exported as verified structure tiles.
- The registered Thar Kroo entity represents B2, Thar Kroo the Immortal. It has three phases, a custodian shield mechanic, arena-relative attacks, altered-block persistence, and arena restoration. It does not implement B4, Thar Kroo the Conqueror.
- B6 Shadow of the Conqueror now has a registered skeleton-based boss entity, exact translated
  text and source music, source equipment, a spawn egg, three health phases, discrete-ring
  teleport, four-part armor-stand laser volleys, multiple persistent immunity zones, animated
  Stygian Memory adds, the two-direction Final Judgment barrage, defeat/victory flow, cleanup,
  and reload recovery. The missing B6 `proyectil_hoguera` path is reconstructed from the complete
  B4 implementation while B6's own black-projectile and immunity-zone selectors remain authoritative.
- B3, B4, B7, B8, B10, and B11 do not yet have registered boss entities.
  B5 (Koyomi x Gariheuz) gained registered bosses and a full controller rewrite
  on 2026-08-01; see `b5-baseline.md`.

## Preliminary arena index

Coordinates below are evidence anchors, not final export bounds. Final bounds must be confirmed against region blocks and every absolute coordinate used by the encounter.

| Boss | Chinese bossbar name | Entry / player anchor | Boss / encounter anchor | Preliminary spatial notes |
|---|---|---|---|---|
| B1 | 艾披格罗，猪灵帝国的领主 | `1331 66 1526` | `1348 65 1526` | Loaded area roughly X 1296..1365, Z 1482..1562 |
| B2 | 塔尔·克罗，不朽者 | `-1505 54 2273` | `-1505 53.5 2305` | Loaded area roughly X -1537..-1473, Z 2275..2344 |
| B3 | 埃克特，核裂变学家 | `1533 109 1395` | `1533 107 1383`; plasma center `1533 109 1395` | Long arena; loaded Z extends to about 1489 |
| B4 | 塔尔·克罗，征服者 | `-6416 53 1434` | `-6383 51 1413` | Large citadel arena and cinematic space |
| B5 | 柯约米 | `-1098 49 1426` | Koyomi `-1148 49 1428`; Gari `-1148 49 1424` | Dual-actor encounter |
| B6 | 征服者之影 | Spawn `-270 110 29` | `-277 110 26` | No `0tp_boss6`; compact arena, no terrain writes or block reads |
| B7 | Aphofis，宇宙巨蛇 | `-686 153 -100` | phase anchors near `-786 153 -100`, `-916 160 -69`, and `-1001 156 -69` | Multi-area encounter; cannot be exported as one simple room |
| B8 | 僵尸超级矩阵 | Spawn `-3817 79 1413` | `-3828 85 1412` | No `0tp_boss8`; fixed matrix platform |
| B9 | Maraw‘Thar，征服者之怒 | `-6393 51 1748` | `-6383 51 1748` | Dynamic center, floor backup/clone, magma mutation |
| B10 | Maraw‘Thar，时间梦魇 | `-277 122 22` | `-277 121 22` | Original `b10_arena` footprint approximately X -298..-255, Z 2..44 |
| B11 | 蜜巢蜂后 | `1521 97 -568` | model `1521 97 -571`; hit entity `1521 90 -571` | Loaded area roughly X 1494..1548, Z -598..-549; very large composite animation |

## Datapack complexity snapshot

| Boss | Function files | Lines | Terrain writes | Block reads | Clone lines | Summons | Teleports |
|---|---:|---:|---:|---:|---:|---:|---:|
| B1 | 152 | 2,476 | 11 | 15 | 1 | 198 | 66 |
| B2 | 295 | 6,604 | 121 | 69 | 14 | 350 | 366 |
| B3 | 202 | 2,842 | 3 | 178 | 1 | 239 | 82 |
| B4 | 514 | 8,956 | 4 | 7 | 0 | 989 | 459 |
| B5 | 235 | 3,645 | 26 | 50 | 1 | 91 | 110 |
| B6 | 80 | 1,129 | 0 | 0 | 0 | 309 | 28 |
| B7 | 358 | 5,435 | 65 | 7 | 24 | 348 | 648 |
| B8 | 158 | 1,820 | 4 | 1 | 0 | 168 | 62 |
| B9 | 444 | 9,394 | 238 | 21 | 7 | 506 | 678 |
| B10 | 246 | 3,360 | 81 | 0 | 0 | 156 | 111 |
| B11 | 917 | 17,524 | 3 | 7 | 1 | 91 | 7,473 |

These counts indicate migration risk, not implementation completeness. In particular, B11's teleport count mostly reflects composite-model animation, while B3's block reads show strong dependence on arena state despite few terrain writes.

## Confirmed arena exports

### B1 Apiglo

- Runtime combat bounds from `bossfight/b1/run.mcfunction`: X 1309..1353, Y 65..85, Z 1505..1547.
- West and east gate planes: X 1308 and X 1354; dynamic iron bars occupy Z 1522..1530.
- Valid export: `src/main/resources/data/finalparadox/structures/arenas/b1_candidate/` (24 tiles plus
  manifest, 171 x 64 x 121). The manifest lists source world `b1-amulet-20260719-231254`; block-level
  comparison shows the tiles map onto the v1.1.15 region at X 1287..1457, Y 62..125, Z 1471..1591
  (local offset +1487/+125/+1564), covering the building plus its surrounding terrain.
- Independent verification against the v1.1.15 region source: white terracotta Dice 0.9997 and polished
  andesite Dice 1.0000 across the building levels, confirming a faithful copy of the original geometry.
- Bedrock cleanup: the source region's surrounding bedrock terrain mass (355,697 blocks, all outside the
  arena building footprint) was replaced with air, so the deployed structure carries no bedrock blob.
- Geometry-only policy: air is preserved; entities and block entities are omitted; command, structure, and jigsaw blocks are replaced with air.
- The earlier 8-tile `arenas/b1` export was a generated reconstruction whose upper building (Y 64+) did
  not match the v1.1.15 region source; it was removed together with its test helpers
  `finalparadox:test/place_b1_arena` and `finalparadox:test/setup_b1`.
- Java deployment (`/arena deploy b1` through `ArenaDefinitions.B1`) already targets `arenas/b1_candidate`.

### B2 Thar Kroo the Immortal

- Original encounter anchor: `-1505 53.5 2305`; player entry and spectator anchors remain inside the same room.
- Runtime floor backup and restoration cover X -1541..-1468, Y 52, Z 2270..2344. Encounter initialization extends its temporary barrier envelope to Z 2258..2346.
- The original player containment check uses X -1558..-1465 and Z 2253..2352. Region inspection confirms this is the smallest safe outer box that preserves the complete combat floor, north recess, side openings, south edge, and a margin around all runtime terrain writes.
- Source bounds: X -1558..-1465, Y 48..64, Z 2253..2352. Y 48 is the first live under-floor lava/obsidian layer and Y 64 is the combat-room upper edge.
- The Y=1 floor cache, Y=204..215 fire-circle staging structure, the dummy `8 5 7` cooldown entities, and the post-fight citadel/reward coordinates around X -1456, Z 1362..1413 are external implementation or story locations and are deliberately excluded.
- Export size: 94 x 17 x 100 blocks, split into six templates with a maximum tile axis of 48.
- Resource directory: `src/main/resources/data/finalparadox/structures/arenas/b2/`.
- Geometry-only policy matches B1: air is preserved; entities and block entities are omitted; command, structure, jigsaw, and spawner blocks are replaced with air.
- Test helpers `finalparadox:test/place_b2_arena` and `finalparadox:test/setup_b2` place the arena relative to the original encounter anchor and optionally summon Thar Kroo.
- Independent verification reopened all six NBT files and compared 159,800 unique positions against the v1.1.15 region source. Result: exact match, with no overlaps, gaps, out-of-bounds blocks, block entities, structure entities, unsafe blocks, or manifest hash mismatches.

Inspection and export tools:

- `tools/reconstruction/region_probe.py`
- `tools/reconstruction/export_structure_tiles.py`

## Next evidence task

Determine final export bounds and tile layouts for B2-B11 from region data. For each encounter, enumerate all absolute coordinates used during combat and cinematics, classify them as arena-local or external story locations, and then select the smallest safe set of structure tiles. B2 is next because its existing Java boss already has arena mutation and restoration logic that can be tested against the exported original geometry.
