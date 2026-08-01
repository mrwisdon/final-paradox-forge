execute as @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage10
execute as @s[nbt=!{HurtTime:10s}] run function ragecraft4reforged:damage/damage4
effect give @s[nbt=!{HurtTime:10s},type=#minecraft:undead] instant_health 1 0
effect give @s[nbt=!{HurtTime:10s},type=#minecraft:non_undead] instant_damage 1 0
data modify entity @s[tag=!no_knockback] Motion[1] set value 1.0d
effect give @s weakness 12 0 false