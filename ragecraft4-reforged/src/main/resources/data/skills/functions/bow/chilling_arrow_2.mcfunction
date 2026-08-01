tag @s add chilling_arrow
tag @s add arrow_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:1b,Duration:60}]}
data modify entity @s {} merge value {Color:10608895}

execute at @s run playsound entity.glow_squid.hurt master @a[distance=..12] ~ ~ ~ 0.2 1.5 0.2

data modify entity @s pickup set value 0b