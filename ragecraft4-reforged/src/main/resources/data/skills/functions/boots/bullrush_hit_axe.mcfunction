execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage15
damage @s[nbt=!{HurtTime:10s}] 15 player_attack by @p[tag=bullrush_attack]
tag @s add hurt