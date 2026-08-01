execute store result score $temp potion_selected run data get entity @s ActiveEffects[{Id:27}].Amplifier

data modify storage src4:potion_stuff temp set value []
data modify storage src4:potion_stuff temp append from entity @s Inventory[{id:"minecraft:glass_bottle"}]
execute if data storage src4:potion_stuff temp[{Slot:-106b}] run scoreboard players set $slot potion_selected -1
execute unless data storage src4:potion_stuff temp[{Slot:-106b}] store result score $slot potion_selected run data get storage src4:potion_stuff temp[0].Slot
execute unless data storage src4:potion_stuff temp[0] run scoreboard players set $slot potion_selected -2

execute if score $temp potion_selected matches 25 run function skills:potions/potion_empty_2/rock_ale
execute if score $temp potion_selected matches 26 run function skills:potions/potion_empty_2/mana_elixir
execute if score $temp potion_selected matches 27 run function skills:potions/potion_empty_2/metamorph
execute if score $temp potion_selected matches 28 run function skills:potions/potion_empty_2/adrenaline
