tag @s add cursed_arrow
tag @s add arrow_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:2b,Duration:60}]}
data modify entity @s {} merge value {Color:1048607}

execute at @s run playsound minecraft:entity.phantom.bite master @p ~ ~ ~ 0.2 0.8 0.2

data modify entity @s pickup set value 0b