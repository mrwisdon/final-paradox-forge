execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage24
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 24 player_attack
execute as @s if entity @p[predicate=skills:flame_quake,nbt={ActiveEffects:[{Id:5,Amplifier:0b}]}] run function ragecraft4reforged:damage/damage3
execute as @s if entity @p[predicate=skills:flame_quake,nbt={ActiveEffects:[{Id:5,Amplifier:1b}]}] run function ragecraft4reforged:damage/damage6
execute as @s if entity @p[predicate=skills:flame_quake,nbt={ActiveEffects:[{Id:5,Amplifier:2b}]}] run function ragecraft4reforged:damage/damage9
execute as @s if entity @p[predicate=skills:flame_quake,nbt={ActiveEffects:[{Id:5,Amplifier:3b}]}] run function ragecraft4reforged:damage/damage12
execute as @s if entity @p[predicate=skills:flame_quake,nbt={ActiveEffects:[{Id:5,Amplifier:4b}]}] run function ragecraft4reforged:damage/damage15
tag @s add hurt

execute as @s run function skills:misc/setonfire_9s
data modify entity @s[tag=!no_knockback] Motion[1] set value 1.2d