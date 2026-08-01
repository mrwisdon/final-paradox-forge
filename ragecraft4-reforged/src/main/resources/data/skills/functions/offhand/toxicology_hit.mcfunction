effect give @s[tag=!arrow_shield] wither 10 1 false

execute at @s[tag=!arrow_shield] run particle dust 0.388 0.549 0.329 1 ~ ~1 ~ 0.3 0.5 0.3 0.1 80 normal
execute at @s[tag=!arrow_shield] run particle squid_ink ~ ~0.7 ~ 0.2 0.3 0.2 0.02 1 normal

execute at @s[tag=!arrow_shield] run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 0.7 1
execute at @s[tag=!arrow_shield] run playsound block.frogspawn.place master @a[distance=..8] ~ ~ ~ 1 0.7 1
execute at @s[tag=!arrow_shield] run playsound entity.glow_squid.hurt master @a[distance=..15] ~ ~ ~ 0.3 0.8 0.3
execute at @s[tag=!arrow_shield] run playsound entity.glow_squid.hurt master @a[distance=..8] ~ ~ ~ 0.2 0.8 0.2