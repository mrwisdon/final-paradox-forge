# Arena waiting-entity uniqueness

## Root cause

The READY tick previously treated `ServerLevel#getEntity(savedUuid) == null` as proof that a saved boss or guide had disappeared. That lookup only resolves loaded entities. Re-entering an arena could therefore clear the SavedData UUID and create a replacement before the original entity's chunk loaded; the original would then load from disk beside the replacement.

## Invariant and reconciliation policy

Each arena waiting slot now resolves only after its expected entity chunk is loaded. Candidates are restricted to live entities owned by the recorded arena anchor (and Koros guide mode where applicable). A saved UUID wins only when it is present in that filtered candidate set. Otherwise the candidate nearest the expected position is retained, with UUID ordering as a deterministic distance tie-break. Surplus waiting candidates are removed and only missing slots are created.

The reconciled batches are:

- B1: one waiting Apiglo and one B1 Echo of Koros. Apiglo's initialization moves it 16.5 blocks east of its logical anchor, so both the anchor chunk and the actual waiting-position chunk are loaded.
- B2: one waiting Thar-Kroo and one B2 Echo of Koros.
- B5: one staged Koyomi, one staged Gari, and one B5 Echo of Koros. Koyomi and Gari persist the deployment anchor; legacy in-bounds instances without the newer anchor field can be adopted and written back.
- B8: one pre-trigger B8 Echo of Koros.
- Maraw'Thar: one pre-trigger Eothar echo, retaining the existing 18-block arrival and 30-block departure thresholds.

Active encounters never create a replacement waiting batch. B1/B2 retain or adopt an arena-owned active boss instead of pruning it. B5 returns before touching combat entities. B8 never creates after its trigger, while still pruning surplus matching guides. Maraw'Thar removes staged Eothar duplicates after the fight is triggered or a boss is recorded, without touching the boss.

Cleanup paths load the expected chunks and scan all matching arena-owned waiting entities, rather than deleting only the single saved UUID. Entities belonging to a different anchor are left unchanged.

## Automated verification

- Java 17 `compileJava` succeeds.
- `ArenaEntitySelectionTest` covers saved-UUID preference, stale-UUID fallback, nearest selection, deterministic UUID tie-breaking, empty candidates, and surplus selection.
- The focused Gradle test run succeeds.
- `git diff --check` succeeds.

## Required in-game verification

- For B1, B2, B5, B8, and Maraw'Thar: enter, leave until chunks unload, and re-enter repeatedly; verify only one waiting batch remains.
- Restart the game/server while a waiting batch is present, then enter each arena.
- Repeat with two players entering from different sides at nearly the same time.
- Test a legacy save that already contains duplicates and confirm one deterministic batch is retained.
- Start each encounter, leave and re-enter during combat, and confirm no waiting boss/guide is recreated and no active boss is removed.
- For Maraw'Thar, verify Eothar appears inside 18 blocks, remains while a player is within 30 blocks, and departs outside 30 blocks.
