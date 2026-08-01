playsound block.beehive.exit master @s ~ ~ ~ 0.5 0.8 0.5
playsound block.medium_amethyst_bud.break master @s ~ ~ ~ 1 1 1
execute at @s run particle dust 0.545 0.075 0.255 1 ~ ~0.7 ~ 0.8 0.8 0.8 0.1 200 normal
execute at @s run particle heart ~ ~0.5 ~ 0.8 0.8 0.8 0.1 15 normal

effect give @s instant_health 1 0 false
effect clear @s slowness
effect clear @s weakness
effect clear @s mining_fatigue
effect clear @s blindness
effect clear @s poison
effect clear @s weakness
effect clear @s wither