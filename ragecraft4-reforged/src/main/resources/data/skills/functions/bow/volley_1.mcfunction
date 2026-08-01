execute at @s run summon arrow ^1.2 ^ ^ {Tags:["arrow_done","volley","volley_1a"]}
execute at @s run summon arrow ^-1.2 ^ ^ {Tags:["arrow_done","volley","volley_1b"]}

execute at @s[predicate=mobs:is_on_fire] run execute as @e[type=arrow,tag=volley,distance=..3] run data modify entity @s Fire set value 100b
execute at @s run execute as @e[type=arrow,tag=volley,distance=..3] run data modify entity @s Owner set from entity @p[predicate=skills:bow] UUID

execute at @s if score #rc4tick spec_archer matches 1.. run execute as @e[type=arrow,tag=volley,tag=!spec_archer,distance=..3] run function skills:bow/spec_archer_volley

execute at @s[tag=puncture] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/puncture_volley
execute at @s[tag=chilling_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/chilling_arrow_volley
execute at @s[tag=webbed_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/webbed_arrow_volley
execute at @s[tag=cursed_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/cursed_arrow_volley
execute at @s[tag=charged_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/charged_arrow_volley
execute at @s[tag=noxious_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/noxious_arrow_volley
execute at @s[tag=arcane_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/arcane_arrow_volley
execute at @s[tag=arctic_arrow] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/arctic_arrow_volley
execute at @s[tag=arcane_suprem] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/arcane_suprem_volley
execute at @s[tag=amplified] run execute as @e[type=arrow,tag=volley_1a,distance=..3] run function skills:bow/amplified_volley_1a
execute at @s[tag=amplified] run execute as @e[type=arrow,tag=volley_1b,distance=..3] run function skills:bow/amplified_volley_1b
execute at @s[tag=dreadfire] run execute as @e[type=arrow,tag=volley_1a,distance=..3] run function skills:bow/dreadfire_volley_1a
execute at @s[tag=dreadfire] run execute as @e[type=arrow,tag=volley_1b,distance=..3] run function skills:bow/dreadfire_volley_1b
execute at @s[tag=void_blast] run execute as @e[type=arrow,tag=volley_1a,distance=..3] run function skills:bow/void_blast_volley_1a
execute at @s[tag=void_blast] run execute as @e[type=arrow,tag=volley_1b,distance=..3] run function skills:bow/void_blast_volley_1b
execute at @s[tag=trueshot] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/trueshot_volley
execute at @s[tag=sharpshot_1] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/sharpshot_1_2
execute at @s[tag=sharpshot_2] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/sharpshot_2_2
execute at @s[tag=sharpshot_3] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/sharpshot_3_2
execute at @s[tag=arrow_arcanist] run execute as @e[type=arrow,tag=volley,distance=..3] run function skills:bow/arcanist_volley

execute at @s run data modify entity @e[type=arrow,tag=volley_1a,tag=!volley_done,limit=1,sort=nearest] Motion set from entity @s Motion
execute at @s run data modify entity @e[type=arrow,tag=volley_1a,tag=!volley_done,limit=1,sort=nearest] damage set from entity @s damage
execute at @s run data modify entity @e[type=arrow,tag=volley_1a,tag=!volley_done,limit=1,sort=nearest] crit set from entity @s crit
execute at @s run data modify entity @e[type=arrow,tag=volley_1a,tag=!volley_done,limit=1,sort=nearest] Fire set from entity @s Fire

execute at @s run data modify entity @e[type=arrow,tag=volley_1b,tag=!volley_done,limit=1,sort=nearest] Motion set from entity @s Motion
execute at @s run data modify entity @e[type=arrow,tag=volley_1b,tag=!volley_done,limit=1,sort=nearest] damage set from entity @s damage
execute at @s run data modify entity @e[type=arrow,tag=volley_1b,tag=!volley_done,limit=1,sort=nearest] crit set from entity @s crit
execute at @s run data modify entity @e[type=arrow,tag=volley_1b,tag=!volley_done,limit=1,sort=nearest] Fire set from entity @s Fire

execute at @s run tag @e[type=arrow,distance=..10] add volley_done