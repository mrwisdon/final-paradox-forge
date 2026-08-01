execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=10}] chal_hit_potion 1
tag @s add hit_by_potion

execute at @s run scoreboard players add @p[scores={potion_selected=10,mana=..19}] mana 1

execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage8
execute at @s run damage @s[nbt=!{HurtTime:10s}] 8 magic by @p[scores={potion_cd=65..}]
tag @s add hurt

execute as @s run function skills:misc/setonfire_7s

#execute as @s store result score @s x run data get entity @s Pos[0] 3
#execute as @s store result score @s y run data get entity @s Pos[1] 3
#execute as @s store result score @s z run data get entity @s Pos[2] 3
#execute as @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] store result score @s x run data get entity @s Pos[0] 3
#execute as @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] store result score @s y run data get entity @s Pos[1] 3
#execute as @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] store result score @s z run data get entity @s Pos[2] 3
#scoreboard players operation @s x -= @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] x
#scoreboard players operation @s y -= @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] y
#scoreboard players add @s y 15
#scoreboard players operation @s z -= @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]}] z
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..5] store result entity @s Motion[0] double 0.03 run scoreboard players get @s x
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..5] store result entity @s Motion[1] double 0.03 run scoreboard players get @s y
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..5] store result entity @s Motion[2] double 0.03 run scoreboard players get @s z
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..3] store result entity @s Motion[0] double 0.05 run scoreboard players get @s x
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..3] store result entity @s Motion[2] double 0.05 run scoreboard players get @s z
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..2] store result entity @s Motion[0] double 0.07 run scoreboard players get @s x
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..2] store result entity @s Motion[2] double 0.07 run scoreboard players get @s z
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..1] store result entity @s Motion[0] double 0.12 run scoreboard players get @s x
#execute at @s if entity @e[type=area_effect_cloud,nbt={Effects:[{Id:27,Amplifier:19b}]},distance=..1] store result entity @s Motion[2] double 0.12 run scoreboard players get @s z