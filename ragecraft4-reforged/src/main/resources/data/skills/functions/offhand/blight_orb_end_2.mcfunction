execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @a[distance=..12] ~ ~ ~ 1 0.8 1

execute at @s run particle explosion ~ ~1 ~ 0.1 0.1 0.1 0.3 1 force
execute at @s run particle totem_of_undying ~ ~1 ~ 0.6 0.6 0.6 0.05 80 force
execute at @s run particle sneeze ~ ~1 ~ 0.6 0.6 0.6 0.02 100 force
execute at @s run particle dust 0.345 0.486 0.247 1 ~ ~1 ~ 0.5 0.5 0.5 0.2 200 force

execute at @s positioned ~ ~1 ~ run kill @e[type=snowball,tag=blight_orb_proj,distance=..2]
execute at @s run tp @s ~ ~-260 ~
kill @s