scoreboard players remove @s[scores={virulent_time=1..}] virulent_time 1
tag @s[scores={virulent_time=0}] remove virulent_plague
execute at @s[tag=virulent_plague] run execute as @e[tag=r4r_ability_target,tag=!virulent_plague,distance=..3] run function skills:sword/virulent_hit
execute at @s run particle sneeze ~ ~1 ~ 0.3 0.5 0.3 0.02 5 normal