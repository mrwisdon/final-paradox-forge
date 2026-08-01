tag @s add puncture
tag @s add arrow_tick

data modify entity @s {} merge value {CustomPotionEffects:[{Id:4,Amplifier:5b,Duration:60}]}
data modify entity @s {} merge value {Color:9175040}

execute at @s run playsound entity.llama.spit master @p ~ ~ ~ 0.3 0.8 0.3