execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage10
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 10 player_attack by @p[tag=void_rage_attack]
tag @s add hurt