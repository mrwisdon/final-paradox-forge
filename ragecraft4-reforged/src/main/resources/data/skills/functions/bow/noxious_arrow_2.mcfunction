tag @s add noxious_arrow
tag @s add arrow_tick
tag @s add marker_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:4b,Duration:60}]}
data modify entity @s {} merge value {Color:1650711}

execute at @s run playsound entity.llama.spit master @p ~ ~ ~ 0.3 0.8 0.3

data modify entity @s pickup set value 0b