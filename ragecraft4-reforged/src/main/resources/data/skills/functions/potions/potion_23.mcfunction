effect give @s speed 25 1 false
effect give @s haste 25 1 false

scoreboard players set @s no_mana_regen 25

execute at @s run particle end_rod ~ ~0.5 ~ 0.5 0.8 0.5 0.05 20 normal
execute at @s run particle cloud ~ ~0.5 ~ 0.5 0.8 0.5 0.1 50 normal

function skills:potions/potion_empty_2
scoreboard players add @s potion_cd 100