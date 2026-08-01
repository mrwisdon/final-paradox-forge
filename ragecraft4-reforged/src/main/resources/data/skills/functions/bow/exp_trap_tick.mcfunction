execute at @s run tp @s ~ ~ ~ ~5 ~

scoreboard players add @s cd 1

execute at @s[tag=!trap_charged,scores={cd=85..}] run function skills:bow/exp_trap_charge

execute at @s[scores={cd=..84}] run particle dust 0.925 0.651 0.145 1 ~ ~1.2 ~ 0.1 0.4 0.1 0.1 2 force
execute at @s[scores={cd=85..}] run particle dust 0.925 0.302 0.145 1 ~ ~1.2 ~ 0.1 0.4 0.1 0.1 2 force

execute at @s[scores={cd=100..}] positioned ~ ~0.7 ~0 if entity @e[tag=r4r_ability_target,distance=..2] run function skills:bow/exp_trap_trigger

kill @e[scores={cd=1200..}]
