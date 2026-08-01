execute at @s run damage @s 24 magic by @p[predicate=skills:blast_mining]

#effect give @s[type=#minecraft:undead] instant_health 1 2
#effect give @s[type=#minecraft:non_undead] instant_damage 1 2

execute as @s run function skills:misc/setonfire_7s
data modify entity @s[tag=!no_knockback] Motion[1] set value 0.6d