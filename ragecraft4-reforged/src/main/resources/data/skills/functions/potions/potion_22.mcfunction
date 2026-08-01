effect give @s strength 20 3 false
effect give @s slowness 20 1 false

execute at @s run playsound entity.ravager.roar master @a[distance=..12] ~ ~ ~ 0.4 1 0.4
execute at @s run particle sneeze ~ ~0.5 ~ 0.5 0.8 0.5 0.1 120 normal
execute at @s run particle cloud ~ ~0.5 ~ 0.5 0.8 0.5 0.1 50 normal

function skills:potions/potion_empty_2
scoreboard players add @s potion_cd 100