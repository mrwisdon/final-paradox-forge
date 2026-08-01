execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage12
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 12 magic by @p[tag=thunder_attack]
effect give @s slowness 2 6 true
tag @s add hurt

execute at @s run particle electric_spark ~ ~ ~ 0.4 0.7 0.4 0.01 120 normal