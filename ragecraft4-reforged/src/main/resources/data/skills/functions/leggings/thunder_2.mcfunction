tag @s add thunder_attack

execute at @s positioned ^ ^1 ^3 run summon marker ~ ~2 ~ {Silent:1b,Invulnerable:1b,Tags:["thunder_marker"]}
execute as @e[type=marker,tag=thunder_marker] at @s run function skills:leggings/thunder_marker_down

tag @s remove thunder_attack