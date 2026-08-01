scoreboard players remove @s frostburn_debuff 1
execute at @s run particle dust 0.910 1.000 0.992 1 ~ ~1 ~ 0.3 0.5 0.3 0.3 2 normal
execute as @s[predicate=mobs:is_on_fire] run function skills:potions/potion_5_damage