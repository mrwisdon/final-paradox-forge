# Nightfall B9 Chainblade reconstruction evidence

## Authoritative source chain

The fifth Nightfall ability is adapted from the original B9 `h3` choreography:

- `bossfight/b9/coreografia/poner_espada/ini.mcfunction`
  starts `h3/espada/ini`.
- `h3/espada/ini2.mcfunction` constructs MarawThar's seven-part white blade with
  `bossfight/b4/espada/gen`, then retags the parts as the planted H3 sword.
- `h3/espada/caer.mcfunction` drives the sword into the floor, creates the
  persistent core marker, and begins the floor warning.
- `h3/espada/run_wait.mcfunction` rotates the core, draws four white end-rod
  points at local offsets `+/-4`, and intermittently emits black dust.
- `h3/espada/tiron/ini_recu.mcfunction` and `tiron/recu.mcfunction` draw an
  end-rod chain from every non-spectator player to the sword. The source moves
  distant players by `1 + 1 + 0.25` blocks per tick toward the core.
- `h3/explosion/run.mcfunction` spends 37 ticks drawing black radial warnings.
  `explosion/particulas/boom.mcfunction` then applies
  `instant_damage 1 4` after clearing resistance, and
  `run_explosion.mcfunction` maintains the expanding black aftermath through
  score 15.

The composite model uses six end-rod parts and one conduit core. The mod renders
the already reconstructed coherent Nightfall model as one tracked entity rather
than networking seven armor stands.

## User-directed player adaptation

- Default activation key: `B`, rebindable in Minecraft controls.
- The model is planted on the first collidable floor found directly below the
  player within 48 blocks, avoiding a world-surface heightmap.
- The requested warning is exactly 60 ticks (3 seconds).
- At tick 60, every valid non-allied living target in a spherical 20-block
  radius is chained and moved to the planted blade.
- The item user takes MarawThar's role: they appear 10 blocks above the blade,
  descend at a constant 1 block per tick for 10 ticks, and impact at tick 70.
- The impact reproduces the source damage tier as 96 indirect magic damage
  after removing resistance. It emits the source-style explosion, flash,
  squid-ink ring, smoke, Wither-shot, Totem, and explosion cues.
- Black fog remains for the source's 15-tick aftermath window. The tracked
  blade is discarded on completion, cancellation, death, dimension change, or
  its independent 100-tick failsafe.
- Base cooldown is 12 seconds. Direct kills use the common Nightfall
  one-second-per-kill cooldown refund.

## Deliberate differences

The original boss separates planting the sword and triggering darkness across
the larger fight choreography, and its final damage selector covers players in
a 28-block arena radius. The item version combines those phases into one
explicit skill, using the user-requested 3-second delay and 20-block pull
radius. Friendly/allied entities, creative players, spectators, armor stands,
invulnerable targets, and the caster are excluded by the common Nightfall
target predicate.
