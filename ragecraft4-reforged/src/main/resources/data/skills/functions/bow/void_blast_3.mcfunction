execute at @s run data modify entity @s Motion set from entity @e[type=arrow,tag=void_blast_base,limit=1,tag=!arrow_copied,sort=nearest] Motion
execute at @s run data modify entity @s damage set from entity @e[type=arrow,tag=void_blast_base,limit=1,tag=!arrow_copied,sort=nearest] damage
execute at @s run data modify entity @s {} merge value {Color:458780}
execute at @s run data modify entity @s Fire set from entity @e[type=arrow,tag=void_blast_base,limit=1,tag=!arrow_copied,sort=nearest] Fire
tag @s add arrow_copied