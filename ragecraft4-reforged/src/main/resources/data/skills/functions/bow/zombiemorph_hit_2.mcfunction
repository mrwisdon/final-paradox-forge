execute at @s run particle dust 0.471 0.8 0.8 1 ~ ~1 ~ 0.8 0.8 0.8 0.1 400 force
execute at @s run particle electric_spark ~ ~1 ~ 0.6 0.6 0.6 0.01 250 force
execute at @s run particle flash ~ ~1 ~ 0.1 0.1 0.1 0.01 1 force

execute at @s run playsound entity.zombie_horse.hurt master @a[distance=..30] ~ ~ ~ 0.4 1 0.4
execute at @s run playsound entity.zombie_horse.hurt master @a[distance=..18] ~ ~ ~ 0.2 1 0.2
execute at @s run playsound entity.zombie_horse.hurt master @a[distance=..10] ~ ~ ~ 0.2 1 0.2
execute at @s run playsound minecraft:entity.illusioner.mirror_move master @a[distance=..30] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound minecraft:entity.illusioner.mirror_move master @a[distance=..18] ~ ~ ~ 0.15 1.2 0.15
execute at @s run playsound minecraft:entity.illusioner.mirror_move master @a[distance=..10] ~ ~ ~ 0.15 1.2 0.15

summon zombie ~ ~ ~

tp ~ ~-300 ~
kill @s[tag=!no_target]