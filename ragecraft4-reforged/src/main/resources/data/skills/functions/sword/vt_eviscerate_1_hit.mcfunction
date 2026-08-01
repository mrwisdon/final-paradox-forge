execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage10
damage @s 10 player_attack by @p[tag=vt_eviscerate_attack]
tag @s add hurt