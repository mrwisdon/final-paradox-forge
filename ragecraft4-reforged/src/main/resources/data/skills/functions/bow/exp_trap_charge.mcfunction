execute at @s run particle flame ~ ~1 ~ 0.1 1 0.1 0.02 80 force

execute at @s run summon marker ~ ~0.8 ~ {Tags:["exp_trap_anim"]}
execute as @e[type=marker,tag=exp_trap_anim] run function skills:bow/exp_trap_anim

tag @s add trap_charged
