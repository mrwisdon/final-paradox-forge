execute as @s[nbt={ActiveEffects:[{Id:2}]}] run function ragecraft4reforged:damage/damage3
execute at @s[nbt={ActiveEffects:[{Id:2}]}] run particle block blue_ice ~ ~1 ~ 0.3 0.3 0.3 0.5 10 force
execute at @s[nbt={ActiveEffects:[{Id:2}]}] run particle item_snowball ~ ~1 ~ 0.3 0.3 0.3 0.2 10 force
effect give @s slowness 10 1 false