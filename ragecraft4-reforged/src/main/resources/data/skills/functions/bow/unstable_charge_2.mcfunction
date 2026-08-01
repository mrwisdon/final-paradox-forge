tag @s add unstable_charge
tag @s add arrow_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:3b,Duration:60}]}
data modify entity @s {} merge value {Color:14599423}

execute as @s store result score @s arrow_damage run data get entity @s damage 10
execute as @s store result entity @s damage double 0.1 run scoreboard players add @s arrow_damage 25

execute at @s run playsound entity.puffer_fish.blow_out master @p ~ ~ ~ 0.5 1 0.5

data modify entity @s pickup set value 0b