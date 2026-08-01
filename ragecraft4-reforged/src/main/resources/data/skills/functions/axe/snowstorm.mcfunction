execute at @s run playsound minecraft:entity.illusioner.cast_spell master @a[distance=..20] ~ ~ ~ 0.9 1.2 0.9
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..20] ~ ~ ~ 0.2 1.5 0.2

tag @e[tag=r4r_ability_target,tag=snowstorm_hit] remove snowstorm_hit
scoreboard players set @s snowstorm_ready 0

scoreboard players set @s delayed_att 40
execute at @s positioned ^ ^1 ^4 run function skills:axe/snowstorm_2