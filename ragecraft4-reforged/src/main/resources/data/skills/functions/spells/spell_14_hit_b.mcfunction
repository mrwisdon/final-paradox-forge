#damage @s[tag=!invunlerable] 12 magic

execute at @s run damage @s[tag=!invunlerable] 12 magic by @p[tag=cast_spell_14]
tag @s[tag=!invunlerable] add hurt

execute at @s[tag=!invunlerable] run playsound entity.puffer_fish.blow_out master @a[distance=..14] ~ ~ ~ 0.2 0.8 0.2
execute at @s[tag=!invunlerable] run playsound block.fire.extinguish master @a[distance=..14] ~ ~ ~ 0.2 1.2 0.2
execute at @s[tag=!invunlerable] run playsound entity.puffer_fish.blow_out master @a[distance=..8] ~ ~ ~ 0.1 0.8 0.1
execute at @s[tag=!invunlerable] run playsound block.fire.extinguish master @a[distance=..8] ~ ~ ~ 0.1 1.2 0.1

execute at @s[tag=!invunlerable] run particle electric_spark ~ ~0.7 ~ 0.4 0.7 0.4 0.01 80 force