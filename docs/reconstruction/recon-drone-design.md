# Recon Drone v1 design

Custom item added to the Forge 1.20.1 mod, not reconstructed from a map
datapack. The map archives contain no drone item, so behavior and numbers are
new design defaults approved by the user with the change that the player body
must stay at the deploy point.

## Item

- Registry name: `finalparadox:recon_drone`
- Right-click deploys the drone; stack size 1, EPIC rarity, no durability.
- Cooldown: 180 seconds stored in player persistent data
  (`finalparadox.recon_drone_cooldown`), ticked by `GameplayEvents`.
- Creative tab: end of `finalparadox:forged_items`.

## Camera and body

- `ServerPlayer.setCamera(DroneEntity)` switches the client camera to the
  drone while the player stays in survival mode.
- The body is frozen at the deploy anchor every server tick
  (`setNoGravity`, zero delta movement, `setPos` back to anchor).
- Client hides hands and cancels attack/use input while
  `Minecraft.getCameraEntity()` is a drone.
- Exit (H) calls `setCamera(null)`, clears persistent state, restores gravity,
  and teleports the client body back to the anchor if needed.
- Death, respawn, and logout clear drone mode without restoring camera.

## Movement and bomb drop

- WASD moves relative to drone yaw, Space ascends, Shift descends, speed
  0.45 blocks/tick; server-authoritative with `DroneInputPacket` held state.
- R drops a `recon_drone_bomb`: gravity entity, up to 40 ticks or impact fuse,
  radius 3.5, 20 damage to hostile entities via
  `GlaivorusAbilityState.isHostileTarget`, knockback, no terrain destruction.
- Each deployment carries 8 bombs, synchronized through `SynchedEntityData`.

## Files

- `item/ReconDroneItem.java`
- `entity/DroneEntity.java`, `entity/DroneBombEntity.java`
- `network/DroneInputPacket.java`, `DroneBombPacket.java`, `DroneExitPacket.java`
- `event/DroneEvents.java`
- `client/DroneKeyMappings.java`, `DroneInput.java`, `DroneRenderer.java`,
  `DroneBombRenderer.java`
- Item model/texture and `en_us.json` / `zh_cn.json` keys

## Remaining validation

- In-game camera behavior and body freeze must be observed on a client.
- Drone model placement relative to the camera needs a visual pass.
- Multiplayer ownership and disconnect cleanup need a second-client check.
