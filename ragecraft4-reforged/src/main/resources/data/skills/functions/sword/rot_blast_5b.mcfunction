scoreboard players add @s chal_mana_spent 8

tag @s add rot_blast_attack

scoreboard players remove @s mana 8

execute at @s positioned ^ ^1.5 ^2.5 run particle dust 0.188 0.502 0.094 1 ~ ~ ~ 0.9 0.7 0.9 0.1 80 normal
execute at @s positioned ^ ^1.5 ^2.5 run particle sneeze ~ ~ ~ 0.3 0.3 0.3 0.2 50 normal
execute at @s positioned ^ ^1.5 ^2.5 run particle totem_of_undying ~ ~ ~ 0.9 0.3 0.9 0.2 60 normal
execute at @s positioned ^ ^1.5 ^2.5 run particle explosion ~ ~ ~ 0.4 0.2 0.4 0.6 3 normal
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 0.8 1
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..15] ~ ~ ~ 1 1.2 1
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..15] ~ ~ ~ 1 1.2 1

execute at @s positioned ^ ^1 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5] run function skills:sword/rot_blast_5_hit

tag @s remove rot_blast_attack