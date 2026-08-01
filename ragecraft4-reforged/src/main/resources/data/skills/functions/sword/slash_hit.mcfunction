execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage3
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 3 player_attack by @p[tag=slash_attack]
scoreboard players set @s slice_1 6
tag @s add hurt