# Nightfall model evidence

## Source ownership

- The completion reward is an `iron_nugget` with CustomModelData `1202009`.
- `assets/minecraft/models/item/iron_nugget.json` selects
  `customitems/ocaso.json`.
- `ocaso.json` uses the 16x16 `customitems/ocaso.png` inventory texture and
  intentionally hides the original powerless reward in first- and
  third-person view.
- The wieldable sword seen on MarawThar is instead constructed by
  `bossfight/b4/espada/gen.mcfunction`, `setup_rotacion.mcfunction`,
  `rotar.mcfunction`, and `equipar.mcfunction`.

## Composite sword

- Six normal armor-stand head slots hold end rods.
- One normal armor-stand head slot at the core holds a conduit.
- Local positions are copied exactly from `setup_rotacion.mcfunction`.
- Head X rotations are `210, 30, 300, 120, 300, 300, 0.01` degrees.
- The renderer reproduces the normal armor-stand non-skull head scale and
  renders each vanilla block item in `HEAD` display context.

## Corrected item adaptation

- The runtime visual is now one coherent baked model using the vanilla end-rod
  and conduit textures. It keeps the original white long blade, crossguard,
  handle, pommel, and conduit core silhouette, but all geometry shares one
  origin. This replaces the failed direct replay of seven independent helmet
  item matrices, which double-applied per-item offsets and visibly separated
  the rods.
- Static third-person rendering uses MarawThar's B9 idle source pose from
  `bossfight/b9/boss/iddle/caminar_1/run.mcfunction`: local core position
  `^-0.75 ^-0.5 ^0.1`, yaw `body yaw + 20`, and pitch `108`.
- The source positions locate normal-sized armor-stand entities. Vanilla then
  raises helmet rendering by the armor-stand model base height; direct compound
  rendering restores this common `+1.5` block height explicitly. Omitting it
  was the cause of the sword appearing around the player's feet.
- Third-person rendering is player-local world rendering rather than a
  hand-bone transform. This removes the non-source `Z -42 / Y 90` rotation
  that skewed the complete sword.
- First-person rendering pitches the model's local blade axis 68 degrees into
  camera depth at 0.62 scale. This keeps the sword pointed forward while
  exposing more of its blade than the previous 78-degree foreshortened pose.
- GUI rendering uses a separate generated-item proxy backed by the source
  16x16 `ocaso.png` reward sprite. The compound world model remains exclusive
  to held, fixed, and ground contexts, avoiding the nearly blank sub-pixel GUI
  result caused by scaling the world-sized geometry into an inventory slot.
- Fixed and ground contexts use their own centered transforms so changing the
  held model does not displace dropped items or item-frame displays.
