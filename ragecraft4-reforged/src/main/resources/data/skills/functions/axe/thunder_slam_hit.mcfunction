execute at @s run damage @s[tag=!no_target] 8 player_attack by @p[tag=thunder_slam_attack]

execute at @s if entity @p[nbt={ActiveEffects:[{Id:1,Amplifier:0b}]},tag=thunder_slam_attack,distance=..12] run function ragecraft4reforged:damage/damage4
execute at @s if entity @e[nbt={ActiveEffects:[{Id:1,Amplifier:1b}]},tag=thunder_slam_attack,distance=..12] run function ragecraft4reforged:damage/damage8
execute at @s if entity @e[nbt={ActiveEffects:[{Id:1,Amplifier:2b}]},tag=thunder_slam_attack,distance=..12] run function ragecraft4reforged:damage/damage12

tag @s add hurt

tag @s add thunder_slam_hit