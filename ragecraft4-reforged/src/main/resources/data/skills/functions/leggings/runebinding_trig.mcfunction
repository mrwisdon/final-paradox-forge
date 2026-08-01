tag @s add runebinding_attack

execute at @s run particle explosion ^ ^1.2 ^2.5 0.1 0.1 0.1 0.8 1 normal
execute at @s run particle dust 0.671 0.71 0.941 1 ^ ^1.2 ^2.5 0.9 0.5 0.9 0.5 400 normal
execute at @s run particle enchant ^ ^1.2 ^2.5 1.3 0.5 1.3 0.3 500 normal
execute at @s run particle flash ^ ^1.2 ^2.5 0 0 0 0.1 1 normal
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..18] ~ ~ ~ 0.25 1.2 0.25
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.7 1.2 0.7
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..18] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound block.fire.extinguish master @a[distance=..18] ~ ~ ~ 0.15 0.8 0.15
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..9] ~ ~ ~ 0.15 1.2 0.15
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..9] ~ ~ ~ 0.4 1.2 0.4
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..9] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound block.fire.extinguish master @a[distance=..9] ~ ~ ~ 0.15 0.8 0.15

execute if score @s r4r_rune_power matches 5 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_5
execute if score @s r4r_rune_power matches 6 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_6
execute if score @s r4r_rune_power matches 7 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_7
execute if score @s r4r_rune_power matches 8 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_8
execute if score @s r4r_rune_power matches 9 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_9
execute if score @s r4r_rune_power matches 10 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_10
execute if score @s r4r_rune_power matches 11 positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_11
execute if score @s r4r_rune_power matches 12.. positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:leggings/runebinding_hit_12

tag @s remove runebinding_attack
