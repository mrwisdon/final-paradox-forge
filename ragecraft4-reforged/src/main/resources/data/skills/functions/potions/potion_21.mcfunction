execute if score @s mana matches 10.. run scoreboard players set @s mana 20
execute unless score @s mana matches 10.. run scoreboard players add @s mana 10

function skills:potions/potion_empty_2
scoreboard players add @s potion_cd 100