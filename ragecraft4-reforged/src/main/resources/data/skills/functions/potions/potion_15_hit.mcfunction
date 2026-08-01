execute at @s[type=silverfish,tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=21}] chal_hit_potion 1
tag @s add hit_by_potion

execute at @s[type=silverfish] run scoreboard players add @p[scores={potion_selected=21,mana=..19}] mana 1

execute at @s[type=silverfish] run scoreboard players add @p[scores={potion_selected=21},distance=..30] bug_spray_buff 1

execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage12
execute at @s run damage @s[nbt=!{HurtTime:10s}] 12 magic by @p[scores={potion_cd=65..}]
tag @s add hurt