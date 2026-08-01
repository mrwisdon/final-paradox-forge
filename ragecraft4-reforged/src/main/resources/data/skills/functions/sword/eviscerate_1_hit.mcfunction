execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage5
damage @s 5 player_attack by @p[tag=eviscerate_attack]
tag @s add hurt