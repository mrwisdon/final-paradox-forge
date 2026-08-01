execute at @s[scores={mana=..19}] run particle enchanted_hit ^ ^1.4 ^2 0.6 0.6 0.6 0.05 40 normal
execute at @s[scores={mana=..19}] run particle dust 0.094 0.471 0.851 1 ^ ^1.4 ^2 0.3 0.3 0.3 0.05 50 normal
playsound block.beehive.exit master @s[scores={mana=..19}] ~ ~ ~ 1 1.2 1

scoreboard players add @s[scores={mana=19}] mana 1
scoreboard players add @s[scores={mana=18}] mana 2
scoreboard players add @s[scores={mana=17}] mana 3
scoreboard players add @s[scores={mana=..16}] mana 4