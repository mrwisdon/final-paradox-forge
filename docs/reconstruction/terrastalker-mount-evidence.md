# B8 Terrastalker mount (`el_montura`) evidence

Authoritative runtime and visual source: `Final_Paradox_v1.1.15.zip`, namespace
`luisb1202`, especially:

- `bossfight/b8/ini_monturas/{ini,gen,particulas}.mcfunction`
- `carga_lanas/14_verde/el_montura/**`
- shared terrain checks in `carga_lanas/14_verde/el_acechador/pathfinding/**`
- `bossfight/b8/danar_montura*.mcfunction`
- `items/megamatriz_perneras/**` for the improved item variant

Authoritative Chinese text: `最终悖论汉化资源包V3-VM汉化组.zip`, extracted
`assets/vm/lang/zh_cn.json`.

## Scope boundary

The map has two related but distinct constructions:

- `el_montura`: the player-operated B8 mount and the improved leggings item mount.
- `el_acechador`: the autonomous hostile Terrastalker encountered in the green area.

B8 calls `el_montura/gen` directly. This reconstruction therefore starts with the
mount. The hostile version must not be silently folded into the same behavior state.

## Visible construction

`resources.zip` contains no overrides for the items below. The source visuals are
vanilla item models rendered by invisible armor stands.

The mount has exactly 18 visible parts:

- 8 full armor-stand head items: `smooth_stone_slab`;
- 2 small armor-stand head items: `netherite_block`;
- 4 full armor-stand right-hand upper-leg shields;
- 4 full armor-stand right-hand foot shields.

The hostile `el_acechador` has 22 visible parts (12 slabs rather than 8) and red
foot accents. Those four extra slabs and red accents do not belong to the B8 mount.

### Cabin armor-stand origins

The rover entity origin corresponds to `14_montura_pata_core`. The cabin core is
at local `y=0.7`. Visible slab armor-stand origins and head poses are:

| Part | Local origin `(x,y,z)` | Stand yaw | Head pose X |
|---|---|---:|---:|
| as1 | `(0, 0.7, 0.35)` | 0 | 45 |
| as5 | `(0, 0.35, 0.5)` | 0 | 90 |
| as6 | `(-0.5, 0.35, 0)` | 90 | 90 |
| as8 | `(0.5, 0.35, 0)` | 270 | 90 |
| as9 | `(0, 0, 0.35)` | 0 | 135 |
| as10 | `(-0.35, 0, 0)` | 90 | 135 |
| as11 | `(0, 0, -0.35)` | 180 | 135 |
| as12 | `(0.35, 0, 0)` | 270 | 135 |

There is no source-wide model scale. In particular, the previous `1.25` global
scale was not supported by the map.

### Cannon

Both cannon armor stands are small, use `netherite_block` as a head item, and are
placed at local `y=0.9`. Generation assigns `rayo_laser` scores 3 and 2; recursive
`recu_pos` advances every positive score by `0.43`, yielding forward offsets
`1.29` and `0.86`. Head-pose X follows the cabin core pitch.

The command text never visibly copies cabin yaw into `14_montura_canon`; only
head-pose X is explicit. Direct in-game requirements supplied during validation
resolve the ambiguous command presentation: the turret follows the player's yaw
both normally and while firing. Its yaw must be stored separately from the leg
core: firing locks the forward/movement direction, and when movement stops the
source no longer runs `patas/paso`, so the legs retain their last accepted heading
instead of following view-yaw changes. The cabin and cannon continue tracking the
player's view while firing.

### Shields

Upper and foot shields share base color 8 and patterns:

1. color 7, `bri`;
2. color 8, `gru`;
3. color 8, `gra`;
4. color 8, `ss`.

Foot shields add color 11, `gra`. The hostile version instead adds color 14.

Initial leg armor-stand origins use local yaw 45/135/225/315. An extended leg is:

- upper: `(-0.6, -0.45, 0.35)`, right arm `[90,0,100]`;
- foot: `(-1.2, -0.8, 0.35)`, right arm `[90,0,20]`.

A raised leg is:

- both parts: `(-0.6, -0.1, 0.45)`;
- upper right arm `[90,0,130]`;
- foot right arm `[90,0,-10]`.

The reconstructed leg shields are rendered from the baked vanilla armor-stand
right-arm model matrix followed by the vanilla `ItemInHandLayer` right-hand
matrix. The eight cabin slabs and two cannon blocks likewise use the baked
armor-stand head bone and vanilla `CustomHeadLayer` HEAD-item matrix; the cannon
path additionally preserves the small-stand branch. None of the 18 visible items
uses a non-ticked, client-only `ArmorStand` entity: those proxies contain
previous/current living-entity render state that the source's persistent, ticked
armor stands never left stale. A single invisible stand remains only for the
mount-hint name text and has no model item equipped.

## Gait score

Movement advances the animation score every tick. Discrete source frames occur at
scores 5, 10, 15 and 20; at 20 the score is reset to 1. The renderer must preserve
these discrete poses rather than interpolating a generic walk cycle.

- frame 5: legs 1 and 3 raised; iron-golem step, volume 1.2, pitch 0;
- frame 10: all extended; netherite step, volume 1.2, pitch 0.8, two 10-count slab
  particle emissions;
- frame 15: legs 2 and 4 raised; iron-golem step, volume 1.2, pitch 0;
- frame 20: all extended with yaw offsets 45/165/225/285; netherite step plus two
  10-count slab particle emissions.

## B8 controls and state

- Mounting immediately calls `frame/index`: gait score becomes 1 and one `0.14`
  step is attempted even with a held item. A sneaking rider uses the leg core's
  previous yaw for that step; otherwise the player yaw is adopted.
- Empty selected hand: advance `0.14` blocks/tick and steer from player yaw.
- Held item: stop.
- Sneaking: fire every 3 ticks and keep the previous rover yaw.
- Cabin and turret yaw continue to follow the player while firing, so upper-body
  aim and the movement/leg yaw are distinct pieces of state. The firing score
  belongs to the rider and is not reset merely by releasing sneak.
- Movement/leg yaw changes only on an accepted non-firing movement step and is
  held while firing or stopped. Cannon pitch continues following the source
  head-pose value.
- Two sneak presses within the five-tick `montura_shift_t` window: dismount outside
  a boss encounter; an active `boss` entity rejects dismount. The implementation
  sends one C2S request per physical Shift press edge because vanilla riding clears
  the normal sneak input when its first dismount attempt is canceled. A separate
  C2S held-state packet synchronizes continuous firing from the physical Shift key;
  it is reset on release, UI capture, rider change, dismount, disconnect, or
  meltdown. Rover variant alone is not encounter state, so a command-spawned B8
  rover remains dismountable while no `boss` exists.
- Rider source position: cabin core +0.8, rendered standing (the map teleports and
  levitates the player; it does not use a vanilla seated vehicle pose).
- Observation-driven recreation adjustment: the rendered rider is raised by 0.6
  in total to rover local `y=1.4`. The gun origin is independently anchored to the
  rover's current position at local `y=1.95`, so rider-height tuning cannot move or
  stale the firing point.
- Initial B8 energy: 100.
- While a `boss` exists, `el_montura/main` does not passively reduce energy.
- B8 hazards subtract the current `b8_damage_handler` value. Direct player damage
  during B8 subtracts 1.
- At zero energy during B8, the source kills the assigned player, then performs a
  100-tick warning/meltdown sequence.

## Gun

- Cadence: one shot every 3 held-sneak ticks.
- Speed: 1.8 blocks/tick.
- Lifetime: 30 ticks.
- Source spawn position: the generator runs at the synchronized mount core,
  advances core-local forward `1.6`, then adds absolute `y + 1.15`.
- The recreation recalculates every shot from the rover's current world position
  and current rider aim, retaining the validated rover-local base height `1.95`.
  It does not use the vanilla passenger position, whose update occurs after the
  rover's manual same-tick movement.
- Source hit sample: from bullet position `y-1`, nearest hostile within radius 1.6.
- Normal damage score: 7; improved item variant: 9.
- Hit target receives slowness II for one second before the damage handler.
- Block impact uses the `#luisb1202:noground` block tag; the improved variant may
  destroy a spawner at the impact block.

## Spawn and cleanup

B8 spawns one mount per non-spectator player around the arena, advancing a marker
by 20 degrees per player. Each spawn emits 32 end-rod radial emissions at radius 3,
then one explosion and one flash particle.

The original explosion/meltdown sequence is 100 ticks and uses smoke, flame, lava,
slab-item particles, hostile knockback/damage, and instant damage to nearby players.
Build success alone cannot validate this sequence visually.

The source's obstructed-cabin B8 fallback is the fixed world position
`(-3815,79,1412)`. The reconstructed entity accepts an arena-translated recovery
position from the future B8 controller; the standalone test command falls back to
its own spawn position. Exact arena recovery therefore remains a controller-level
validation item, not a model approximation.

## Improved-item conflicts

Executable functions set the improved mount to 25 energy units and decrement about
once per 19 ticks, while tooltip prose says 100 seconds. Runtime functions are the
behavior authority; the conflict must remain documented rather than silently
rewriting runtime to match the tooltip.

Likewise, `megamatriz_perneras/cd` starts at 36 and decrements every 5 seconds
(about 180 seconds), while tooltip prose says one minute.

## Required validation

- The compound renderer must interpolate cabin yaw, turret yaw, movement/leg yaw
  and cannon pitch against the same `partialTick`. Applying the latest synchronized
  values directly to child parts makes them visibly step against Minecraft's
  interpolated entity origin. Source gait keyframes remain discrete gameplay
  states, while their visible armor-stand origins and yaws receive the same
  three client ticks of interpolation as `LivingEntity#lerpTo`; right-arm pose
  NBT changes remain immediate.
- Assert 18 source-visible parts in code.
- Compare front, side and top views in original and mod at identical yaw.
- Capture all four gait frames and the stationary initial pose.
- Before mounting, observe all eight leg shields from a fixed camera for at least
  ten seconds; their transforms must remain bit-for-bit stationary between frames.
- Observe all eight cabin slabs before mounting and after stopping a turn; every
  seam must remain fixed with no relative motion between adjacent slabs.
- Sweep the view slowly, stop, fire, then release fire; both cannon blocks must
  remain coincident with each other and turn smoothly throughout firing, while the
  movement/leg heading remains locked.
- Verify cannon pitch and the source cannon-yaw quirk in the original map.
- Verify standing rider pose/camera, held-item stop, sneak fire, double-sneak handling,
  B8 dismount rejection, bullet origin, terrain rise/drop and energy loss from hazards.
