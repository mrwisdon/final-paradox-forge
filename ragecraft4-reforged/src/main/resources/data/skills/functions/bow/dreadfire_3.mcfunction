execute at @s run data modify entity @s Motion set from entity @e[type=arrow,tag=dreadfire_base,limit=1,tag=!arrow_copied,sort=nearest] Motion
execute at @s run data modify entity @s damage set from entity @e[type=arrow,tag=dreadfire_base,limit=1,tag=!arrow_copied,sort=nearest] damage
execute at @s run data modify entity @s {} merge value {Color:4849684}
data modify entity @s Fire set value 100

tag @s add arrow_copied