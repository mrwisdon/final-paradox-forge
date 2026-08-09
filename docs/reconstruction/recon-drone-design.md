# Recon Drone v2 design

Custom item added to the Forge 1.20.1 mod. The map archives contain no drone
item, so behavior and numbers are new design defaults. Version 2 replaces the
old 20 Hz input/ack prediction layer with Minecraft's client-controlled vehicle
path and a vulnerable body proxy at the deploy point.

## Item

- Registry name: `finalparadox:recon_drone`.
- Right-click deploys the drone; stack size 1, EPIC rarity, no durability.
- Cooldown: 180 seconds stored in player persistent data
  (`finalparadox.recon_drone_cooldown`), ticked by `GameplayEvents`.
- Creative tab: end of `finalparadox:forged_items`.

## Vehicle, camera, and streaming

- The real `LocalPlayer` / `ServerPlayer` is an invisible passenger of
  `DroneEntity`. The camera remains the normal first-person player camera; no
  spectator mode, camera packet, or fake camera entity is used.
- The controlling client moves the root vehicle immediately with collision at
  0.45 blocks/tick. Vanilla sends `ServerboundMoveVehiclePacket` every player
  tick, performs its normal speed/collision validation on the server, moves the
  server player with the passenger, and updates chunk streaming around that
  player. There is no custom input sequence, state acknowledgement, replay,
  interpolation correction, or visual offset.
- WASD is relative to player/drone yaw, Space ascends, and Shift descends.
  Ordinary Shift dismounts are canceled on both sides; H is the only normal
  exit and temporarily authorizes the dismount.
- The passenger riding offset is -2.67, so the first-person eye sits about
  1.05 blocks below the drone entity origin, just under the belly at the
  machine-gun sight line. The drone body is drawn around/above that view.
- Remote clients retain tracked-entity interpolation. The recon drone has a
  client tracking range of 128 blocks and update interval 1.
- Flight has no explicit distance limit from the deploy anchor.

## Vulnerable anchor body

- Deployment creates `recon_drone_body`, a `LivingEntity` at the anchor. It is
  rendered with the owner's player skin and copied equipment while the real
  passenger is invisible.
- The proxy owns a Forge ticking chunk ticket for its anchor chunk, so it keeps
  receiving entity/environment ticks after the player flies beyond simulation
  distance. The ticket is released on every exit/cleanup path. At server load,
  stale entity tickets owned by this mod are rejected because drone sessions
  do not survive logout or restart.
- Damage to the proxy is forwarded through `ServerPlayer.hurt`, preserving the
  real player's armor, effects, health, death, and normal damage events. A
  narrow persistent bypass marker admits only that forwarded call; ordinary
  damage against the invisible player at the drone is canceled.
- The real passenger cannot attack, use blocks/items/entities, toss or pick up
  items, collect XP, or enter another dimension while drone mode is active.
  The proxy itself cannot change dimensions.

## Exit and cleanup

- H sends the server-authoritative exit packet. The server dismounts the player,
  restores the prior invisibility and no-gravity values, returns the player to
  the recorded anchor/dimension, releases the chunk ticket, and removes both
  drone and proxy.
- Proxy-forwarded death, logout, login/reconnect, respawn, invalid passenger
  state, missing entities, or a dimension mismatch run the same cleanup and
  anchor-return path without the normal exit message.
- The cooldown survives mode cleanup independently.

## Bomb drop

- R sends the existing server-authoritative `DroneBombPacket`.
- Each deployment carries 8 bombs, synchronized through `SynchedEntityData`.
- `recon_drone_bomb` is a gravity entity with a 40-tick or impact fuse, radius
  3.5, 20 hostile-only damage through `GlaivorusAbilityState`, knockback, and no
  terrain destruction.

## Gatling gun

- Holding the attack key sends only held-state transitions to the server. The
  client still cancels vanilla attacks and block breaking while controlling the
  drone; opening a menu, losing window focus, pressing H, or changing the
  controlled drone sends a release transition.
- The server stores the authoritative held state on the owner drone. A fresh
  press must warm for 20 ticks before the first salvo. Synced states are
  `IDLE`, `WARMING`, `FIRING`, and `OVERHEATED`.
- Each `FIRING` tick adds one heat. At 120 firing ticks the guns enter an
  uninterruptible 80-tick overheat lock; releasing cannot cancel it. The entry
  sends one localized warning, stops the firing loop through the synced state,
  plays the vanilla extinguish sound, and emits a large smoke burst from both
  muzzles. Small two-muzzle smoke bursts continue every 5 ticks during the
  lock. When it ends, held input returns to `WARMING`; released input returns
  to `IDLE`. At all other non-overheated times, heat falls by 2 per tick while
  not firing.
- While firing, both guns fire together every 2 ticks. Each ray deals 1
  player-attributed damage over at most 64 blocks. The two muzzles start 0.30
  blocks left and right of the center, 1.10 blocks below the drone origin, and
  0.56 blocks forward. Their unspread base rays meet on the central sight line
  32 blocks ahead (about 0.537 degrees of inward aim).
- Each ray receives an independent uniform-area circular spread sample. The
  cone grows linearly with heat from 0.25 degrees to 0.85 degrees.
- A collider ray truncates the entity ray at the first block. The closest
  bounding-box intersection wins among monsters and entities in
  `finalparadox:glaivorus_targets`; the owner/passenger, dead, and invulnerable
  entities are excluded. Terrain is never modified.
- Each machine-gun round temporarily clears only the selected target's hurt
  immunity for damage evaluation, then restores the maximum of its old and new
  value so unrelated immunity is never shortened.
- The server sends no sampled tracer particles. Each salvo sends one compact
  S2C packet containing both start/end pairs and their miss/block/entity impact
  types. A client-only, entity-free renderer advances a 0.65-block, two-layer
  white-yellow/orange tracer at 12 blocks/tick on a continuous game-time clock.
  The maximum 64-block trajectory therefore takes about 5.33 ticks (267 ms). The
  clock avoids whole-tick packet-boundary jumps and guarantees one rendered
  frame for short paths before cleanup. Small renderer-owned muzzle flashes
  replace continuous flame particles; low-frequency smoke avoids a muzzle fire
  column. On visual arrival, block hits use local block-state fragments plus a
  spark and entity hits use two small critical sparks. Tracers remain
  camera-relative and clear on world switch or disconnect.
- Client-only tickable sounds attach to every tracked drone. `WARMING` plays the
  non-looping `drone_gatling_spinup`; `FIRING` plays the looping
  `drone_gatling_fire`. Both stop immediately when the synchronized state
  changes or the entity leaves tracking. The sound classes are never referenced
  from common code, preserving dedicated-server class loading.

## Drone model and texture

- The entity renderer uses an original 64x64 cutout texture and a dedicated
  `ModelPart` layer instead of stacking vanilla item models. The silhouette is
  a low dark-gray fuselage with four diagonal arms, one motor per arm, thin
  two-blade rotors, a rear antenna, and a cyan-blue forward gimbal lens.
- Two independent belly Gatlings hang at model X -4.8 and +4.8 (world lateral
  offsets -0.30 and +0.30 blocks). Each has its own mount, receiver, magazine,
  grip, and six-tube `CubeListBuilder` barrel cluster. Both gimbals follow the
  pilot pitch; static 0.5-degree inward yaw visually matches the 32-block
  convergence. The clusters counter-rotate while `WARMING` or `FIRING`.
- The four rotors use a checkerboard direction pattern: front-left and
  rear-right turn clockwise while front-right and rear-left turn
  counter-clockwise. Every horizontally or vertically adjacent pair therefore
  counter-rotates.
- The eight visible payload canisters form two belly-side columns of four
  canisters each (a 4x2 longitudinal grid). Their visibility follows the
  synchronized remaining-bomb count; payload is never represented as a
  vertical stack.
- The complete frame is centered about 0.6 blocks below the drone entity
  origin, with the pilot eye at about 1.05 blocks below the origin. The gun
  barrel is drawn slightly below the eye line so it reads as an under-slung
  attack camera; placement still requires an in-game visual check at normal and
  extreme pitch angles, and the low camera can clip into terrain when hugging
  the ground.
- `textures/entity/recon_drone.png` is the matching original 64x64 ARGB model
  atlas. `textures/item/recon_drone.png` remains a separate original 16x16 ARGB
  inventory icon; neither texture contains assets copied from the map archive.

## Network protocol

- Custom channel protocol version: 11.
- Drone custom packets are bomb (id 8), exit request (id 9), and the explicit
  client dismount authorization (id 10), Gatling held state (id 11), and the
  double-tracer S2C visual (id 12). Movement and look use vanilla player-input
  and vehicle-movement packets.

## Files

- `item/ReconDroneItem.java`
- `entity/DroneEntity.java`, `DroneBodyProxyEntity.java`,
  `DroneBombEntity.java`
- `network/DroneBombPacket.java`, `DroneExitPacket.java`,
  `DroneDismountPacket.java`, `DroneGunInputPacket.java`,
  `DroneTracerPacket.java`, `ModNetwork.java`
- `event/DroneEvents.java`, `DroneModEvents.java`
- `client/DroneKeyMappings.java`, `DroneInput.java`, `DroneRenderer.java`,
  `DroneBodyProxyRenderer.java`, `DroneBombRenderer.java`,
  `DroneGatlingSoundInstance.java`, `DroneGatlingSoundManager.java`,
  `DroneTracerRenderer.java`
- `entity/DroneGatlingCycle.java`, `DroneGatlingBallistics.java` are pure logic
  classes with no `DroneEntity` static-initialization dependency.
- `registry/ModEntities.java`

## Remaining in-game validation

- Verify first-person camera height, drone model visibility, all six movement
  directions, collision correction, abrupt reversal, and high-latency feel.
- Verify Shift continuously descends without a transient local dismount and H
  always returns exactly once.
- Verify the proxy skin/equipment, melee/projectile/environment damage, death
  at the anchor, and that the real player cannot be damaged at drone range.
- Verify long-distance chunk generation/streaming and that the single forced
  anchor chunk stays ticking without leaving tickets after H, death, logout,
  reconnect, server restart, or dimension anomalies.
- Verify multiplayer remote drone rendering and that other players can see and
  damage the anchor proxy while the owner is far away.
- Verify the exact one-second spin-up feel, continuous-fire loop seam and
  volume/attenuation for both the pilot and remote players. Confirm both tracers
  line up with the rendered muzzles at extreme pitch, visibly converge near 32
  blocks without spread, blocks always occlude targets, and same-target salvos
  apply both one-point hits. Observe the six-tube geometry and 0.5-degree inward
  gimbals from front/side views.
- Hold through the full 120 firing ticks and verify the warning, immediate loop
  stop, 4-second continuing smoke lock, release/re-press behavior, and automatic
  re-warm when attack remains held. Recheck menu, focus loss, H, death, cleanup,
  multiplayer tracking, dedicated-server startup, and dimension/world switches.
