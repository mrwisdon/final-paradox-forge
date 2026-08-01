playsound block.beehive.exit master @s ~ ~ ~ 1 1.2 1
particle heart ^ ^1 ^2 0.3 0.3 0.3 0.1 3 normal
effect give @s instant_health 1 0 false
scoreboard players add @s[scores={mana=..18}] mana 2
scoreboard players add @s[scores={mana=19}] mana 1

advancement revoke @s only skills:meleekill_unholy