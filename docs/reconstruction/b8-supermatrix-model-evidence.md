# B8 Zombie Supermatrix model evidence

Authoritative runtime source: `Final_Paradox_v1.1.15.zip`, especially:

- `bossfight/b8/matriz/gen.mcfunction`
- `bossfight/b8/matriz/run.mcfunction`
- `bossfight/b8/matriz/run_invulnerable.mcfunction`
- `bossfight/b8/matriz/run_vulnerable.mcfunction`
- `bossfight/b8/matriz/hacer_invulnerable.mcfunction`
- `bossfight/b8/matriz/hacer_vulnerable.mcfunction`

Authoritative Chinese name: VM Chinese translation V3,
`luisb1202.functions.bossfight.b8.matriz.gen.1` = `<僵尸超级矩阵>`.

## Source construction

The model contains 15 visible armor-stand helmet items:

- six full-size stands with gold-block helmets and head pose X=90 degrees;
- four full-size stands with gold-block helmets and head pose X=45 degrees;
- four more full-size stands with gold-block helmets and head pose X=45 degrees;
- one full-size sea-lantern helmet used by the source hitbox stand.

The source also places one upright world lantern at matrix core + `(0,1,0)` and
uses one small invisible armor stand for the visible name. These are separate
from the 15 helmet-item assertion.

All item parts are rendered through the vanilla 1.20.1 full-size armor-stand
head bone followed by `CustomHeadLayer`'s HEAD-item transform. The reconstruction
uses one synchronized matrix entity and one compound renderer; it does not
network 15 individual armor stands.

## Steady-state transforms

Every source `run_*` tick first copies each child to the matrix core, applies a
global local Y offset of `-0.3`, and then applies the listed local offset.
Consequently the stable positions differ by `-0.3` Y from the one-tick generation
pose in `gen.mcfunction`.

| Group | Count | Initial yaw spacing | Head X | Compact `(radius,y)` | Vulnerable `(radius,y)` | Spin |
|---|---:|---:|---:|---:|---:|---:|
| gold 1 | 6 | 60 degrees | 90 | `(0.8,-0.3)` | `(2.4,-0.3)` | -3 degrees/tick |
| gold 2 | 4 | 90 degrees | 45 | `(0.56,0.26)` | `(1.68,1.38)` | +3 degrees/tick |
| gold 3 | 4 | 90 degrees | 45 | `(0.56,-1.2)` | `(1.68,-2.5)` | +3 degrees/tick |
| sea-lantern core | 1 | 0 degrees | 0 | `(0,-0.5)` | `(0,-0.5)` | +3 degrees/tick |

Name-stand origins are local Y `2.7` while compact and `4.7` while vulnerable.
The command resolves each ring part's local position from its previous yaw and
then applies the new relative yaw in the same teleport command. The renderer
therefore keeps position yaw one three-degree step behind item orientation. It
also interpolates compact/expanded offsets over the same three client ticks used
by source armor-stand teleports.

## Transition and idle visuals

- The source rotates every helmet stand by exactly three degrees per game tick.
- Both states emit one end-rod particle per tick at local `(0,1.5,0)` with
  spread `(0.3,0.3,0.3)`.
- Opening uses 32 radial end-rod emissions with motion radius 3 and speed 0.25,
  plus explosion and flash particles and the ender-chest-open sound.
- Closing starts 32 emissions at radius 5, moves them three blocks inward at
  speed 0.15, then emits explosion and flash particles and the
  ender-chest-close sound.

## Model-only test commands

- `/finalparadox supermatrix spawn`
- `/finalparadox supermatrix spawn vulnerable`
- `/finalparadox supermatrix compact`
- `/finalparadox supermatrix vulnerable`
- `/finalparadox supermatrix toggle`
- `/finalparadox supermatrix remove`

The spawn command places the matrix core at integer coordinates five blocks in
front of and four blocks above the command player. State and removal commands
target the nearest matrix model within 96 blocks.

## Deliberate scope boundary

This first stage does not implement the 250-point encounter health, bossbar,
mount assignment, falling modules, add waves, damage, victory, defeat, music or
arena controller. The lantern is rendered at the exact source block position
instead of mutating the test world; the future encounter controller can own the
real light-emitting arena block and its cleanup.

Required in-game validation remains: compact/vulnerable front, side and top
comparison; ten seconds of stationary seam observation; rotation-direction and
three-degrees-per-tick confirmation; name height; and lighting comparison around
the source lantern.
