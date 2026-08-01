execute if score #rc4tick tick10 matches 6 as @s[nbt={SelectedItem:{tag:{Enchantments:[{id:"minecraft:channeling",lvl:5s}]}}}] run function skills:custom_durability/add_damage
execute if score #rc4tick tick10 matches 6 as @s[nbt=!{SelectedItem:{tag:{Enchantments:[{id:"minecraft:channeling",lvl:5s}]}}}] run item modify entity @s weapon.mainhand skills:damage_counter

execute if score #rc4tick tick10 matches 7 as @s[nbt={SelectedItem:{tag:{Enchantments:[{id:"minecraft:channeling",lvl:5s}]}}}] run function skills:custom_durability/add_damage
execute if score #rc4tick tick10 matches 7 as @s[nbt=!{SelectedItem:{tag:{Enchantments:[{id:"minecraft:channeling",lvl:5s}]}}}] run item modify entity @s weapon.mainhand skills:damage_counter