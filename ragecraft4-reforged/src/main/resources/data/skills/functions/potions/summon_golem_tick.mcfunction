scoreboard players add @s cd 1

execute as @s[scores={cd=..40}] run tp @s ~ ~ ~ ~12 ~

data modify entity @s[scores={cd=40}] NoAI set value 0b
data modify entity @s[scores={cd=40}] Glowing set value 0b

execute at @s[scores={cd=40}] run playsound minecraft:entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.3 0.8 0.3
execute at @s[scores={cd=40}] run particle campfire_cosy_smoke ~ ~ ~ 1 0.2 1 0.05 30 force

execute at @s run particle soul_fire_flame ~ ~1 ~ 0.5 1.0 0.5 0.01 1 force
execute at @s[scores={cd=640}] run particle campfire_cosy_smoke ~ ~ ~ 1 0.2 1 0.01 50 force
execute at @s[scores={cd=640}] run particle poof ~ ~1 ~ 0.6 1.2 0.6 0.01 150 force

kill @s[scores={cd=640..}]