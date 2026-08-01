execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..15] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..15] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..7] ~ ~ ~ 0.2 1.3 0.2
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..7] ~ ~ ~ 0.3 0.8 0.3

kill @e[type=marker,tag=trinity_wind_m]

execute at @s run summon marker ~ ~ ~ {NoGravity:1b,Tags:["trinity_wind_m","trinity_wind_m1"]}
execute at @s run summon marker ^ ^1.3 ^2 {NoGravity:1b,Tags:["marker_tick","trinity_wind_m","trinity_wind_m2"]}

execute at @s run execute as @e[type=marker,tag=trinity_wind_m2,distance=..5] run data modify entity @s Rotation set from entity @p[tag=trinity_attack] Rotation

item modify entity @s[predicate=skills:model_1] weapon.mainhand skills:model_2
item modify entity @s[predicate=skills:model_5] weapon.mainhand skills:model_2
item modify entity @s[predicate=skills:model_11] weapon.mainhand skills:model_12
item modify entity @s[predicate=skills:model_15] weapon.mainhand skills:model_12
item modify entity @s[predicate=skills:model_21] weapon.mainhand skills:model_22
item modify entity @s[predicate=skills:model_25] weapon.mainhand skills:model_22
item modify entity @s[predicate=skills:model_31] weapon.mainhand skills:model_32
item modify entity @s[predicate=skills:model_35] weapon.mainhand skills:model_32
item modify entity @s[predicate=skills:model_41] weapon.mainhand skills:model_42
item modify entity @s[predicate=skills:model_45] weapon.mainhand skills:model_42
item modify entity @s[predicate=skills:model_51] weapon.mainhand skills:model_52
item modify entity @s[predicate=skills:model_55] weapon.mainhand skills:model_52