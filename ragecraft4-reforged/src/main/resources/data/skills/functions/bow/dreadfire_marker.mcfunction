execute at @s unless entity @e[type=arrow,tag=dreadfire,nbt={inGround:0b},distance=..3] run function skills:bow/dreadfire_trigger

execute at @s run particle flame ~ ~-0.3 ~ 0.1 0.1 0.1 0.01 2 force
execute at @s run particle dust 0.133 0 0.039 1 ~ ~-0.3 ~ 0.1 0.1 0.1 0.2 5 force