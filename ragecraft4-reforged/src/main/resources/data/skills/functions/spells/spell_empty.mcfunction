data modify storage src4:potion_stuff temp set value []
data modify storage src4:potion_stuff temp append from entity @s Inventory[{id:"minecraft:carrot_on_a_stick",tag:{is_spell:1b}}]

execute if data storage src4:potion_stuff temp[0] run function skills:spells/spell_empty_2
