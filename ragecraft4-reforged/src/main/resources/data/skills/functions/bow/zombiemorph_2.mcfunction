tag @s add zombiemorph
tag @s add arrow_tick
tag @s add arrow_done

data modify entity @s {} merge value {CustomPotionEffects:[{Id:27,Amplifier:7b,Duration:60}]}
data modify entity @s {} merge value {Color:5767139}

execute at @s run playsound entity.glow_squid.hurt master @a[distance=..12] ~ ~ ~ 0.2 1.1 0.2

data modify entity @s pickup set value 0b