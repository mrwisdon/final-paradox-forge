execute at @s run data modify entity @s Fire set from entity @e[type=arrow,limit=1,sort=nearest,nbt={inGround:0b}] Fire
tag @s add arrow_copied