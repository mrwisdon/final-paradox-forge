tag @s add arrow_arcanist
tag @s add arrow_tick

data modify entity @s[nbt=!{CustomPotionEffects:[{Id:4,Amplifier:5b}]}] {} merge value {CustomPotionEffects:[{Id:4,Amplifier:6b,Duration:60}]}
data modify entity @s[nbt={CustomPotionEffects:[{Id:4,Amplifier:5b}]}] {} merge value {CustomPotionEffects:[{Id:4,Amplifier:7b,Duration:60}]}
data modify entity @s {} merge value {Color:12491209}

data modify entity @s damage set value 0.5d

execute at @s run playsound entity.squid.squirt master @p ~ ~ ~ 0.2 1.8 0.2