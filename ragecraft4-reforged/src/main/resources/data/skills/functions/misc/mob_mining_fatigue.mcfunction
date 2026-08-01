#Triggerd from mobs:tick_everymob with mining fatigue effect

execute at @s[nbt={ActiveEffects:[{Id:4,Amplifier:5b}]}] run function skills:bow/puncture_hit
execute at @s[nbt={ActiveEffects:[{Id:4,Amplifier:6b}]}] run function skills:bow/arcanist_hit
execute at @s[nbt={ActiveEffects:[{Id:4,Amplifier:7b}]}] run function skills:bow/puncture_hit
execute at @s[nbt={ActiveEffects:[{Id:4,Amplifier:7b}]}] run function skills:bow/arcanist_hit

effect clear @s mining_fatigue