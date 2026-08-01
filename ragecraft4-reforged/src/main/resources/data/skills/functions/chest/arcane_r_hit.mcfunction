execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage8
execute at @s[nbt=!{HurtTime:10s}] run damage @s[tag=!no_target] 8 player_attack by @p[tag=arcane_r_attack]