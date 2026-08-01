# Nightfall: MarawThar B9 small-laser evidence

Target: Final Paradox v1.1.15, `bossfight/b9/h2/combo2`.

## Source call graph

- `h2/combo2/run.mcfunction`
  - tracks the selected player from score 9 onward;
  - calls `laser/gen_proyectil_parry` at scores 9, 11, 13, 15, 17, and 19.
- `h2/combo2/track.mcfunction`
  - continuously faces the selected `b9_targeted_player`.
- `h2/laser/gen_proyectil_parry.mcfunction`
  - creates three glowing armor stands spaced 0.35 blocks apart;
  - each stand holds black concrete with right-arm pose `[345,225,0]`;
  - assigns the red team and starts every part at score 2.
- `h2/laser/run_proyectil.mcfunction`
  - plays the puffer-fish release cue at score 4;
  - steps backward 0.2 blocks at score 6;
  - calls `mov_proyectil` four times per tick;
  - removes the projectile at score 20.
- `h2/laser/mov_proyectil.mcfunction`
  - performs collision from score 9 onward;
  - moves each part forward one block per call, for four blocks per tick.
- `b9/parry/hit/test_proyectil*.mcfunction`
  - uses a one-block hit radius;
  - gives the player a two-tick laser-hit cooldown;
  - allows the red projectile to be parried in the original encounter.
- `b9/parry/hit/dano.mcfunction`
  - clears resistance and applies Instant Damage IV, equivalent to 48 normal
    magic damage; the original enrage branch repeats it.

## Item adaptation

- Activation: airborne right click. This is an item-only input mapping because
  the original boss choreography has no player-item activation gesture.
- Targeting: the nearest valid enemy within 30 blocks substitutes for the
  encounter's randomly selected player. The target stays fixed while valid and
  is reacquired only if it disappears or leaves range.
- Timing, six-shot cadence, three-part construction, arm pose, red outline,
  0.35 spacing, score-6 backstep, score-9..20 travel, four one-block substeps,
  hit radius, two-tick shared hit protection, and 48 damage are source-derived.
- The projectile is offensive in the item adaptation, so the encounter's
  player-parry input is not exposed on the firing item.
- Cooldown: 8 seconds. Each enemy directly killed by a Nightfall sequence hit
  refunds 1 second of the remaining cooldown, stacking per kill. The source boss
  choreography does not define an item cooldown; this is an explicit re-entry
  guard and kill reward for the survival item.
- Caster muzzle height is anatomy-adapted from the B9 armor-stand body marker
  to the player model while preserving the source's horizontal trajectory.
