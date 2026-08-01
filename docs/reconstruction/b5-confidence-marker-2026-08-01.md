# B5 confidence marker evidence (2026-08-01)

Authoritative source: `Final_Paradox_v1.1.15`, under
`data/luisb1202/functions/bossfight/b5/h1/confianza/**`.

- `h1/switch` recreates one invisible, small marker armor stand 2.4 blocks above the
  currently unshielded boss.
- Gariheuz is confident when Koyomi is farther than 15 blocks. Koyomi is confident
  when Gariheuz is within 15 blocks.
- `b5/run` refreshes the confidence state once every 20 ticks.
- `h1/run` teleports the marker every tick and emits two `crit` particles every tick
  while the owner is confident.
- Diffident text is the gray, bold `descofiar.1` translation. Confident text begins
  with the light-purple, bold `confiar.1` translation, then `run_colorines` advances
  frames 1 through 9 and holds frame 9 until its 20-tick counter wraps.
- Both intermissions and phase 4 call `h1/reset`, so the marker must not exist there.

Java implementation: `B5EncounterController.tickTrustMarker`. The armor stand uses
the existing `B5Marker` cleanup ownership, is recreated after a normal-phase reload,
and is discarded on shield changes, intermissions, phase 4, reset, defeat cleanup,
and victory cleanup.

Validation:

- Java 17.0.12 full Gradle build: passed.
- `finalparadox-0.1.0.jar`: `AB535084409FEE0784A36E126E50BAA48FBCB7A3EF0E9A1F64A5E0C4E70FC61A`.
- `ragecraft4reforged-0.1.0.jar`: `031030A7A41573917504041F44BC2B6D1CDC0A66BD8AA597509C137F70E75456`.
- Both deployed JAR hashes match their build outputs.
- In-game visual placement and readability still require observation.
