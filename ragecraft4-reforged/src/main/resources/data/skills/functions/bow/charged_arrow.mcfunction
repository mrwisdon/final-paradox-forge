execute at @s[scores={mana=20..}] run execute as @e[type=minecraft:arrow,tag=!arrow_done,nbt={pickup:1b,inGround:0b,crit:1b},distance=..3] run function skills:bow/charged_arrow_2
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15