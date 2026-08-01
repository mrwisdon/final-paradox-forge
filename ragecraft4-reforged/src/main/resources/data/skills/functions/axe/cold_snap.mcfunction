execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:axe/cold_snap_2
scoreboard players set @s cold_snap_ready 0

execute at @s run summon marker ^ ^1 ^3 {Tags:["cold_snap_anim"]}
execute as @e[type=marker,tag=cold_snap_anim] run function skills:axe/cold_snap_anim

execute at @s positioned ^ ^1 ^3 run particle dust 0.82 0.875 0.988 1 ~ ~ ~ 1.5 0.6 1.5 0.5 300 normal
execute at @s positioned ^ ^1 ^3 run particle block packed_ice ~ ~ ~ 1.5 0.6 1.5 0.1 200 normal

execute at @s run playsound minecraft:entity.illusioner.cast_spell master @a[distance=..20] ~ ~ ~ 0.9 1.2 0.9
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..20] ~ ~ ~ 0.2 1.5 0.2