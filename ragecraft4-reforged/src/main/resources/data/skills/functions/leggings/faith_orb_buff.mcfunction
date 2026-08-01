playsound block.beehive.exit master @s ~ ~ ~ 0.5 0.8 0.5
playsound block.medium_amethyst_bud.break master @s ~ ~ ~ 1 1 1
execute at @s run particle dust 1 0.973 0.612 1 ~ ~0.7 ~ 0.8 0.8 0.8 0.1 200 normal
execute at @s run particle dragon_breath ~ ~0.5 ~ 0.8 0.8 0.8 0.03 35 normal

effect give @s absorption 30 1 true
effect clear @s slowness
effect clear @s weakness
effect clear @s mining_fatigue
effect clear @s blindness
effect clear @s poison
effect clear @s weakness
effect clear @s wither