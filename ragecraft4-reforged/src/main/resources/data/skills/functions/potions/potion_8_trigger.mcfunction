execute at @s run summon armor_stand ~ ~ ~ {Silent:1b,Invulnerable:1b,Small:1b,Invisible:1b,Tags:["marker_tick","stink_bomb"]}
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.8 0.3
execute at @s run particle dust 0.22 0.475 0.282 1 ~ ~1 ~ 0.3 1.5 0.3 0.5 250 normal
execute at @s run particle smoke ~ ~0.5 ~ 0.8 0.5 0.8 0.1 80 normal