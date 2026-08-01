effect give @s[tag=!no_target] wither 10 1 false
effect give @s[tag=!no_target] weakness 10 0 false
tag @s[tag=!no_target] add mob_tick20
tag @s[tag=!no_target] add virulent_plague
scoreboard players set @s[tag=!no_target] virulent_time 10
execute at @s[tag=!no_target] run particle sneeze ~ ~1 ~ 0.3 0.5 0.3 0.02 10 normal

execute at @s run summon marker ~ ~0.1 ~ {Tags:["virulent_anim"]}
execute as @e[type=marker,tag=virulent_anim] run function skills:sword/virulent_anim