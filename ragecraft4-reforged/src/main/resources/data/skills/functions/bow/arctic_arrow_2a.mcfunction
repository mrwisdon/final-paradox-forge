tag @s add arctic_arrow
tag @s add arrow_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:5b,Duration:60}]}
data modify entity @s {} merge value {Color:9949936}

execute at @s run playsound entity.glow_squid.hurt master @a[distance=..12] ~ ~ ~ 0.2 1.5 0.2

data modify entity @s pickup set value 0b