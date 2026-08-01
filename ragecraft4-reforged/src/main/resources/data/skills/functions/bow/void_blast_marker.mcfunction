execute at @s unless entity @e[type=arrow,tag=void_blast,nbt={inGround:0b},distance=..3] run function skills:bow/void_blast_trigger

execute at @s run particle ash ~ ~ ~ 0.1 0.1 0.1 0.05 12 force
execute at @s run particle dust 0.016 0 0.094 1 ~ ~-0.3 ~ 0.1 0.1 0.1 0.2 8 force