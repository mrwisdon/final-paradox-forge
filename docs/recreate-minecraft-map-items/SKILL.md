---
name: recreate-minecraft-map-items
description: Analyze Minecraft adventure-map archives, datapacks, resource packs, and translation packs, then faithfully recreate selected custom items, abilities, visual effects, entities, bosses, or arena structures in a Forge mod. Use when migrating command/datapack content to Java, inventorying custom map items, matching translated lore, diagnosing differences between an original map and a mod recreation, exporting or validating structure tiles/arenas, or building and syncing a local test JAR.
---

# Recreate Minecraft Map Items

Reconstruct map content from evidence rather than from tooltip summaries. Preserve behavior, visuals, text, and timing independently, because their sources may disagree.

## Task routing

- New item, boss, or arena reconstruction -> Evidence map -> Plan -> Implement -> Validate -> Self-review.
- Reported problem or parity difference -> read-only diagnosis first; report and wait for confirmation (see Diagnosis-first).
- Build or JAR sync only -> run the build, sync, and hash check; skip evidence extraction.
- In-game visual/behavior comparison -> use deploy/test commands and screenshots; make no code changes without a prior diagnosis.

## Diagnosis-first workflow

Treat every newly reported problem as diagnosis-only, even when the initial report is phrased as an instruction to fix it. Planning-only requests must not be implemented.

1. Inspect workspace instructions (AGENTS.md) and existing changes before editing.
2. Identify the exact Minecraft, Forge, and Java versions, project folder, source archives, translation pack, and test-mod destination.
3. Perform read-only inspection and compare the current implementation with the authoritative source.
4. Report the evidence, root cause, affected paths, and proposed correction without editing, building, synchronizing, or otherwise mutating the project.
5. Wait for an explicit follow-up confirmation after the diagnosis before implementing. A later request to fix an already diagnosed problem counts as confirmation.
6. Treat original assets as user-provided reference material. Do not assume permission to redistribute them.

## Build an evidence map

Use `rg --files`, `rg`, and archive listing/extraction tools before broad unpacking. Locate:

- item creation or forge-recipe functions;
- activation triggers and cooldown functions;
- scheduled calls and animation loops;
- damage, targeting, status-effect, and cleanup functions;
- resource-pack item overrides, textures, models, sounds, and particles;
- requested localization keys and exact translated strings.

Trace the complete call graph for one item before implementing it. Record exact constants, tick timings, selectors, tag exclusions, fallback branches, coordinates, sounds, and particle counts.

Persist extracted evidence in the project's `docs/reconstruction/` so later sessions reuse it instead of re-extracting. When migrating many items, inventory the full set first and group by shared mechanics (cooldowns, renderers, tags, creative tabs) so one implementation pattern covers the group.

Apply this source hierarchy:

- Runtime behavior: actual function/command chain.
- Visual construction: resource pack plus summoned/display-entity coordinates and poses.
- User-facing text: the translation pack requested by the user.
- Tooltip prose: explanatory evidence only; do not use it to override executable behavior silently.

When sources conflict, preserve each layer faithfully where possible and report the conflict. Never silently paraphrase localized lore.

## Plan the reconstruction

Before coding, give the user a per-item plan covering:

- base item, attributes, enchantments, durability, foil state, and stack size;
- activation gesture and inventory semantics;
- cooldown, costs, random branches, and player state;
- target selection and compatibility with modded entities;
- animation phases, rendering approach, particles, sounds, and damage volumes;
- localization and creative-tab behavior;
- known differences that cannot yet be reproduced exactly.

For batches, present one shared plan covering the common systems plus per-item deltas instead of repeating the full plan.

Prefer a server-authoritative ability state and lightweight synchronized visual entities. Keep client-only render registration isolated from common/server code.

## Implement faithfully

### Item and activation

- Register the item, entity types, creative entry, translations, models, and textures through the target Forge APIs.
- Preserve the exact thrown stack once: prevent duplication, deletion, or loss on failed activation.
- Store long-running ability state per player or per ability instance, not in global static fields.
- Persist cooldown state when appropriate. Restore its client display after reconnecting.
- Make unusual compatibility sets extensible through item, entity-type, or biome tags.

### Animation and rendering

- Model the original animation score separately from entity age when the command logic jumps or rewrites scoreboard values.
- Translate scheduled seconds into exact game ticks. Verify whether an initial strike is immediate or part of the repeated schedule.
- Reproduce coordinate frames and yaw conventions with a small numerical check before visual tuning.
- If the map builds a composite visual from blocks or display entities, convert those parts into one compound renderer or baked model; apply the part-count and pose checks in [references/forge-1.20.1.md](references/forge-1.20.1.md). Do not replace it with a scaled 2D item icon unless the user approves an approximation.
- Match rise, hold, impact, sweep, terrain-following, descent, and cleanup phases separately.
- Match damage geometry to the original sampling volumes rather than using one convenient oversized AABB.
- Register a renderer for every client-tracked custom entity; a missing renderer can cause a client crash.

### Text and item stacks

- Copy the selected translation keys exactly, including line breaks, punctuation, formatting codes, and rarity markers.
- Use one stack-preparation path for the creative tab, commands/default instances, and items already in player inventories.
- Check `HideFlags` deliberately and use the smallest mask. See [references/forge-1.20.1.md](references/forge-1.20.1.md) for the bit table, including the bit-`32` `appendHoverText` pitfall.
- Compare the tooltip in the creative inventory and after moving the stack into the player inventory.

For Minecraft 1.20.1 Forge-specific implementation checks, read [references/forge-1.20.1.md](references/forge-1.20.1.md).

## Validate in layers

1. Compile after structural changes; fix target-version mapping names rather than guessing APIs.
2. Validate JSON resources before the full build.
3. Run the project tests and full reobfuscated build.
4. Sync the JAR only after a successful build. Verify destination existence, timestamp, size, or hash.
5. Reuse existing project tooling (`tools/reconstruction/`, test functions, deploy commands) instead of rewriting scripts.
6. Smoke-test logic on a dedicated server or with in-game test commands before the full client visual pass.
7. Request in-game screenshots in one batch: inventory, held item, each animation phase, hit registration, indoor terrain, and multiple targets.
8. Read crash reports from the bottom-up cause chain and match them to client/server registration boundaries.

Build success is code/resource validation, not visual validation.

## Perform a mandatory self-review

After every implementation or fix, perform a fresh self-review before reporting completion.

1. Re-read the user's request and the original evidence without relying on the implementation plan.
2. Inspect the actual diff and final files for accidental omissions, approximations, duplicate entities or triggers, stale assets, wrong model ownership, missing translation keys, incorrect timings, and client/server state errors.
3. Trace every changed activation path through success, cooldown, repeated input, cancellation, reconnect or persistence, cleanup, and multiplayer ownership where applicable.
4. Compare behavior, visuals, item stacks, and localized text independently against their authoritative sources. Check that a resource belongs to the selected item rather than merely having a similar name.
5. Verify all custom client-tracked entities have renderers, long-running effects have one authoritative owner, and repeated input cannot create unintended duplicate instances.
6. Re-run the relevant compile, resource validation, full build, synchronization, and hash checks after fixing anything found by the review.
7. Report the self-review outcome: issues found and corrected, checks that passed, and anything still requiring in-game observation. If the review finds an unresolved material mismatch, do not call the work complete.

After each iteration, state exactly what was verified and what still requires in-game observation. Do not claim parity until behavior and visuals have both been compared with the original.
