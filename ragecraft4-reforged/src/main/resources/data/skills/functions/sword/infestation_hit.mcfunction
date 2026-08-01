effect give @s weakness 8 1 true
tag @s[tag=!no_target] add r4r_fungus_dot_apply
tag @s add hurt

execute at @s run particle sneeze ~ ~0.5 ~ 0.3 0.5 0.3 0.01 40 normal
