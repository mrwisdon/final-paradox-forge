execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @a[distance=..12] ~ ~ ~ 1 0.8 1

execute at @s run particle explosion ~ ~1 ~ 0.1 0.1 0.1 0.3 1 normal
execute at @s run particle reverse_portal ~ ~1 ~ 0.6 0.6 0.6 0.03 80 normal
execute at @s run particle squid_ink ~ ~1 ~ 0.1 0.1 0.1 0.01 5 normal
execute at @s run particle dust 0.431 0.247 0.486 1 ~ ~1 ~ 0.5 0.5 0.5 0.2 200 normal

execute at @s positioned ~ ~1 ~ run kill @e[type=snowball,tag=dark_orb_proj,distance=..2]
execute at @s run tp @s ~ ~-260 ~
kill @s