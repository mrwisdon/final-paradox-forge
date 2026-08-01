execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage16
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 16 player_attack by @p[tag=trinity_attack]
effect give @s slowness 8 2 false
tag @s add hurt