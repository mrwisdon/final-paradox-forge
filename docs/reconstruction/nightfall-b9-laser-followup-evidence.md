# Nightfall: MarawThar B9 laser follow-up evidence

Target: Final Paradox v1.1.15,
`bossfight/b9/coreografia/laseres_1`.

## Source call graph and timing

- `coreografia/laseres_1/ini.mcfunction`
  - starts `h2/combo2` immediately;
  - starts `h2/combo1` at 1.75 seconds (35 ticks);
  - starts `h2/combo1` again at 3.5 seconds (70 ticks);
  - starts `h2/combo3` at 8 seconds (160 ticks).
- `h2/combo1/run.mcfunction`
  - at local scores 5..10, draws a 45-point end-rod warning and launches
    projectiles at yaw offsets 120, 180, 240, 300, 360, and 60 degrees;
  - at local scores 19..27, fills the remaining 10-degree directions from
    130 through 470 degrees while skipping multiples of 60;
  - therefore each of the two blue volleys contains 36 projectiles.
- `h2/laser/gen_proyectil.mcfunction`
  - creates three glowing black-concrete parts spaced 0.35 blocks apart;
  - starts at score -3, steps backward 0.2 blocks at score 6, and changes to
    the aqua team at that same score.
- `h2/laser/run_proyectil.mcfunction` and `mov_proyectil.mcfunction`
  - execute four one-block movement/collision substeps per tick from score 9;
  - remove the projectile at score 20.
- `h2/combo3/run.mcfunction`
  - follows one selected target with a marker;
  - activates eight radius-8 positions around it at local scores
    1, 6, 9, 12, 15, 18, 21, and 24;
  - fires red parry projectiles inward from those positions at local scores
    43, 46, 49, 52, 55, 58, 61, and 64.

## Item adaptation

- The airborne-right-click attack remains one server-authoritative state, so
  repeated input cannot duplicate the follow-up.
- The nearest valid enemy within 30 blocks replaces the source player target.
  It remains locked while valid and is reacquired only after invalidation.
- Blue and red projectiles retain the source three-part construction, ages,
  team colors, 0.35 spacing, 0.2 backstep, 4 blocks/tick travel, one-block
  sampling radius, two-tick shared hit protection, and 48 magic damage.
- The source teleports MarawThar through the eight orbit positions. The item
  adaptation now moves the user to the first position immediately when
  `combo3` starts, then through the remaining positions at local scores
  6, 9, 12, 15, 18, 21, and 24, keeps the user facing the selected target,
  and returns to the first position for local scores 27..40. The player body
  uses the source `^-0.3 ^-1.3 ^-1` relationship as a horizontal one-block
  inset and 0.3-block side offset from each radius-eight warning point; a
  nearby vertical fallback prevents teleporting inside solid blocks outside
  the source arena. The established position lock remains through the initial
  red barrage, then releases the user until this orbit phase begins.
- The two unrequested intermediate choreography steps at ticks 105 and 125
  (a sword fake and a repeated straight red barrage) remain omitted. To remove
  the resulting dead interval, the retained `combo3` orbit now reuses tick 105:
  eight ticks after the second blue volley stops generating and while its last
  projectiles are still in flight.
- Cooldown is 8 seconds, with 1 second refunded for every enemy directly killed
  by a Nightfall sequence hit. The
  compressed active sequence lasts 188 ticks and blocks overlapping activation.
  The ready cue is deferred until the active sequence ends so it cannot claim
  the item is usable while the overlap guard is still active.
