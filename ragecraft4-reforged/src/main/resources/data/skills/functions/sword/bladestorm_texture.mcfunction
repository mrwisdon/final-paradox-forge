execute at @s if entity @p[distance=..5,predicate=skills:stone_sword_4] run scoreboard players set @s texture 1
execute at @s if entity @p[distance=..5,predicate=skills:stone_sword_14] run scoreboard players set @s texture 2
execute at @s if entity @p[distance=..5,predicate=skills:stone_sword_24] run scoreboard players set @s texture 3
execute at @s if entity @p[distance=..5,predicate=skills:stone_sword_34] run scoreboard players set @s texture 4
execute at @s if entity @p[distance=..5,predicate=skills:stone_sword_44] run scoreboard players set @s texture 5
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_4] run scoreboard players set @s texture 6
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_14] run scoreboard players set @s texture 7
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_24] run scoreboard players set @s texture 8
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_34] run scoreboard players set @s texture 9
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_44] run scoreboard players set @s texture 10
execute at @s if entity @p[distance=..5,predicate=skills:iron_sword_54] run scoreboard players set @s texture 11
execute at @s if entity @p[distance=..5,predicate=skills:diamond_sword_4] run scoreboard players set @s texture 12
execute at @s if entity @p[distance=..5,predicate=skills:diamond_sword_14] run scoreboard players set @s texture 13
execute at @s if entity @p[distance=..5,predicate=skills:diamond_sword_24] run scoreboard players set @s texture 14
execute at @s if entity @p[distance=..5,predicate=skills:netherite_sword_4] run scoreboard players set @s texture 15
execute at @s if entity @p[distance=..5,predicate=skills:netherite_sword_14] run scoreboard players set @s texture 16
execute at @s if entity @p[distance=..5,predicate=skills:netherite_sword_24] run scoreboard players set @s texture 17
execute at @s if entity @p[distance=..5,predicate=skills:netherite_sword_34] run scoreboard players set @s texture 18
execute at @s if entity @p[distance=..5,predicate=skills:netherite_sword_44] run scoreboard players set @s texture 19

item replace entity @s[scores={texture=1}] weapon.mainhand with stone_sword{CustomModelData:4} 1
item replace entity @s[scores={texture=2}] weapon.mainhand with stone_sword{CustomModelData:14} 1
item replace entity @s[scores={texture=3}] weapon.mainhand with stone_sword{CustomModelData:24} 1
item replace entity @s[scores={texture=4}] weapon.mainhand with stone_sword{CustomModelData:34} 1
item replace entity @s[scores={texture=5}] weapon.mainhand with stone_sword{CustomModelData:44} 1
item replace entity @s[scores={texture=6}] weapon.mainhand with iron_sword{CustomModelData:4} 1
item replace entity @s[scores={texture=7}] weapon.mainhand with iron_sword{CustomModelData:14} 1
item replace entity @s[scores={texture=8}] weapon.mainhand with iron_sword{CustomModelData:24} 1
item replace entity @s[scores={texture=9}] weapon.mainhand with iron_sword{CustomModelData:34} 1
item replace entity @s[scores={texture=10}] weapon.mainhand with iron_sword{CustomModelData:44} 1
item replace entity @s[scores={texture=11}] weapon.mainhand with iron_sword{CustomModelData:54} 1
item replace entity @s[scores={texture=12}] weapon.mainhand with diamond_sword{CustomModelData:4} 1
item replace entity @s[scores={texture=13}] weapon.mainhand with diamond_sword{CustomModelData:14} 1
item replace entity @s[scores={texture=14}] weapon.mainhand with diamond_sword{CustomModelData:24} 1
item replace entity @s[scores={texture=15}] weapon.mainhand with netherite_sword{CustomModelData:4} 1
item replace entity @s[scores={texture=16}] weapon.mainhand with netherite_sword{CustomModelData:14} 1
item replace entity @s[scores={texture=17}] weapon.mainhand with netherite_sword{CustomModelData:24} 1
item replace entity @s[scores={texture=18}] weapon.mainhand with netherite_sword{CustomModelData:34} 1
item replace entity @s[scores={texture=19}] weapon.mainhand with netherite_sword{CustomModelData:44} 1