# Forge 1.20.1 reconstruction checks

Read this reference when the target is Minecraft 1.20.1 with Forge 47.x.

## Toolchain

- Build with Java 17. Configure Gradle's Java home when the desktop default is newer.
- Prefer the project's Gradle wrapper.
- Run `compileJava` for fast mapping checks, then `build` for resources, reobfuscation, tests, and packaging.
- Keep local JAR synchronization as a finalizer of successful `build`, not of compilation alone.
- Warn that a running game may lock the destination JAR on Windows.

## Item stack consistency

Prepare all variants through one method:

- `getDefaultInstance()` for creative/command-created stacks;
- the creative-tab event should accept that initialized stack;
- migrate existing inventory stacks carefully, commonly in `inventoryTick` or an inventory event.

Avoid overwriting unrelated user NBT. Only normalize fields owned by the mod.

Common `HideFlags` bits:

| Bit | Value | Hides |
|---|---:|---|
| Enchantments | 1 | Enchantment list |
| Attribute modifiers | 2 | Attack attributes |
| Unbreakable | 4 | “Unbreakable” line |
| Can destroy | 8 | Adventure destroy list |
| Can place | 16 | Adventure placement list |
| Additional | 32 | Additional tooltip, including `appendHoverText` |
| Dye | 64 | Dyed-color line |

Choose the smallest mask. For visible enchantments and lore but hidden “Unbreakable,” use `4`.

## Thrown-item activation

With `ItemTossEvent`:

- operate server-side;
- identify the exact custom item;
- ensure the physical item entity is removed or the event is canceled consistently;
- return exactly one copy of the original stack;
- activate only once;
- on failed activation, keep the item and do not start cooldown.

Test full inventory behavior; `placeItemBackInInventory` may drop a stack if no slot exists.

## Custom ability entities

- Register through `DeferredRegister<EntityType<?>>`.
- Use `NetworkHooks.getEntitySpawningPacket` for the custom entity.
- Register `EntityRenderer` on the client MOD event bus.
- Keep damage and state changes on the server.
- Synchronize any values required for client rendering. Do not rely on unsynchronized server-only fields for orientation or phase.
- Set tracking range and update interval proportionally to ability speed.

## Datapack-to-Java timing

Minecraft commands scheduled in seconds use 20 ticks per second. A scoreboard jump such as `17 -> 60` is an animation-state transition, not 43 ticks of waiting. Represent it explicitly:

```text
animationScore(age) = age <= 17 ? age : age + 43
```

Check command ordering within a tick. A function may damage at score 17 and then rewrite the score, creating a distinct rise impact immediately before the sweep.

## Composite visuals

For armor-stand helmets or display-entity mosaics:

1. Parse every local coordinate, pose, block/item type, and scale.
2. Count source parts and assert the generated renderer has the same count.
3. Render all parts in one entity renderer or baked model to avoid dozens of networked entities.
4. Match the source pose rotations before tuning global yaw.
5. Test from front, side, above, and during motion.

## Damage and targeting

- `MobCategory.MONSTER` does not cover every modded hostile or boss. Add an extensible entity-type tag.
- Preserve exclusions such as invulnerable entities, phantoms, or ghasts where present.
- Use explicit boss tags plus known vanilla bosses if the source distinguishes boss damage.
- If the source samples several radius-based volumes, reproduce those samples. An AABB changes both reach and feel.
- Determine whether hit-once tags reset per strike, per sweep, or per entire activation.

## Final checks

- Creative and player-inventory tooltips match.
- Enchantments, lore, rarity marker, attributes, foil, and unbreakable state match.
- No missing translation keys in supported languages.
- First strike and repeated strikes use the correct origins.
- Indoor/cave ground search does not use the world surface heightmap blindly.
- Client renderer exists and dedicated-server class loading remains safe.
- Built and destination JARs match by timestamp plus size or hash.
