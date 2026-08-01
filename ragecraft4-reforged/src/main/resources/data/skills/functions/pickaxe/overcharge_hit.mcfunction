execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage30
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 30 player_attack by @p[tag=overcharge_attack]