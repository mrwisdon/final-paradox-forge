scoreboard players add @s ray_range 1

execute at @s[tag=!earth_spikes_ground] positioned ~ ~-0.3 ~ if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s[tag=!earth_spikes_ground] positioned ~ ~-0.3 ~ unless block ~ ~ ~ #minecraft:nonsolid run tag @s add earth_spikes_ground

execute as @s[scores={ray_range=..50},tag=!earth_spikes_ground] run function skills:axe/earth_spikes_ray_ns
kill @s[scores={ray_range=50..},tag=!earth_spikes_ground]