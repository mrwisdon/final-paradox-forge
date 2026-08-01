execute store result score $temp potion_selected run data get storage src4:potion_stuff temp[0].tag.spell
execute store result score $slot potion_selected run data get storage src4:potion_stuff temp[0].Slot

execute if score $slot potion_selected matches 0 run function skills:spells/slot_0_empty
execute if score $slot potion_selected matches 1 run function skills:spells/slot_1_empty
execute if score $slot potion_selected matches 2 run function skills:spells/slot_2_empty
execute if score $slot potion_selected matches 3 run function skills:spells/slot_3_empty
execute if score $slot potion_selected matches 4 run function skills:spells/slot_4_empty
execute if score $slot potion_selected matches 5 run function skills:spells/slot_5_empty
execute if score $slot potion_selected matches 6 run function skills:spells/slot_6_empty
execute if score $slot potion_selected matches 7 run function skills:spells/slot_7_empty
execute if score $slot potion_selected matches 8 run function skills:spells/slot_8_empty

execute if score $slot potion_selected matches -106 run function skills:spells/slot_offhand_empty

data remove storage src4:potion_stuff temp[0]
execute if data storage src4:potion_stuff temp[0] run function skills:spells/spell_empty_2
