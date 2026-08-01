scoreboard players add @s chal_mana_spent 10

execute at @s run execute as @e[type=minecraft:arrow,tag=!arrow_done,nbt={pickup:1b,inGround:0b,crit:1b},distance=..3] run function skills:bow/zombiemorph_2
scoreboard players remove @s mana 10