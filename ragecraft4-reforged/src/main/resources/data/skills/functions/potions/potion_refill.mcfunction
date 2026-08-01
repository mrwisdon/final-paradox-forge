data modify storage src4:potion_stuff temp set value []
data modify storage src4:potion_stuff temp append from entity @s Inventory[{id:"minecraft:leather_horse_armor",tag:{infinite_potion:1b}}]
execute if data storage src4:potion_stuff temp[0] run function skills:potions/potion_refill_2

scoreboard players add @s potion_cd 20