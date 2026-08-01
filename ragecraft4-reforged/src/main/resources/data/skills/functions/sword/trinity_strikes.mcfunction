scoreboard players add @s chal_mana_spent 6

scoreboard players remove @s mana 6
scoreboard players add @s trinity_element 1
scoreboard players set @s trinity_cd 0

tag @s add trinity_attack
execute at @s[scores={trinity_element=1}] positioned ^ ^1 ^4 if block ~ ~5 ~ #minecraft:nonsolid run function skills:sword/trinity_fire
execute at @s[scores={trinity_element=1}] positioned ^ ^1 ^4 unless block ~ ~5 ~ #minecraft:nonsolid run scoreboard players add @s trinity_element 1
execute at @s[scores={trinity_element=2}] run function skills:sword/trinity_wind
execute at @s[scores={trinity_element=3}] run function skills:sword/trinity_ice

scoreboard players set @s[scores={trinity_element=3..}] trinity_element 0