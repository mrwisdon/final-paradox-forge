execute if entity @s[type=#ragecraft4reforged:native_damage_targets] if entity @p run damage @s 32 minecraft:magic by @p
execute if entity @s[type=#ragecraft4reforged:training_dummies] if entity @p run tag @s add hurt
execute unless entity @s[type=#ragecraft4reforged:native_damage_targets] run function custom_damage:damage32
