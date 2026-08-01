execute at @s run particle sweep_attack ~ ~ ~ 0.1 0.1 0.1 0.5 1 force
execute at @s run particle crit ~ ~0.5 ~ 0.4 0.2 0.4 0.05 8 force
execute at @s run playsound minecraft:entity.puffer_fish.blow_out master @a[distance=..16] ~ ~ ~ 0.02 0.5 0.02

#execute at @s run tp @s ~ ~ ~ ~5 ~

scoreboard players remove @s cyclone_time 1
effect give @s[scores={cyclone_time=1}] slowness 4 6 false