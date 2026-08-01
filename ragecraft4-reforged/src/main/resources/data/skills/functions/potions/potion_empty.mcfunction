tag @a[scores={src4.use_potion=1..},sort=nearest,limit=1] add src4.potion_user

execute store result score $temp potion_selected run data get entity @s Item.tag.CustomPotionEffects[{Id:27}].Amplifier
execute as @a[tag=src4.potion_user] if score @s src4.use_potion matches 1 run function skills:potions/potion_empty_mainhand
execute as @a[tag=src4.potion_user] if score @s src4.use_potion matches 2 run function skills:potions/potion_empty_offhand

execute if score $temp potion_selected matches 10..22 run scoreboard players remove $temp potion_selected 9
execute if score $temp potion_selected matches 23..25 run scoreboard players remove $temp potion_selected 3
scoreboard players operation @a[tag=src4.potion_user] potion_selected = $temp potion_selected

execute if entity @a[tag=src4.potion_user,predicate=skills:deadly_shrapnel] run data modify entity @s Item.tag.CustomPotionEffects append value {Id:32,Amplifier:1b}
execute if entity @a[tag=src4.potion_user,predicate=skills:obsidian_shards] run data modify entity @s Item.tag.CustomPotionEffects append value {Id:32,Amplifier:2b}
execute as @a[tag=src4.potion_user,predicate=skills:call_void] run function skills:offhand/call_void_potion

scoreboard players add @a[tag=src4.potion_user] potion_cd 100
tag @a remove src4.potion_user
tag @s add done