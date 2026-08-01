#execute at @s[scores={meleehit=1..},predicate=skills:holding_sword] run function skills:misc/meleekill
#execute at @s[scores={meleehit=1..},predicate=skills:holding_axe] run function skills:misc/meleekill
#execute at @s[scores={meleehit=1..},predicate=skills:holding_carrot] run function skills:misc/meleekill

execute if score #rc4tick spec_archer matches 1.. if score #rc4tick tick10 matches 7..9 run give @s arrow

scoreboard players set @s mob_kills 0