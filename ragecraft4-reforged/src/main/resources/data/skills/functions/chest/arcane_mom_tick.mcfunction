scoreboard players add @s[predicate=skills:arcane_momentum,nbt={FallFlying:1b}] gliding 1
execute at @s[scores={gliding=1..},nbt={FallFlying:0b}] run function skills:chest/arcane_mom_nofly

execute at @s[nbt={FallFlying:1b}] run function skills:chest/arcane_mom_particle