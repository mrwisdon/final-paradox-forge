#Triggerd from mobs:tick_everymob with badluck effect

execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:1b}]}] run function skills:bow/chilling_arrow_hit
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:2b}]}] run function skills:bow/cursed_arrow_hit
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:3b}]}] run function skills:bow/unstable_charge_hit
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:4b}]}] run function skills:bow/noxious_arrow_hit
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:5b}]}] run function skills:bow/arctic_arrow_hit1
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:6b}]}] run function skills:bow/arctic_arrow_hit2
execute as @s[nbt={ActiveEffects:[{Id:27,Amplifier:7b}]}] run function skills:bow/zombiemorph_hit

effect clear @s unluck