scoreboard players add @s an_curse 1

execute at @s[scores={an_curse=2..20}] run particle dust 0.176 0.098 0.239 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 3 normal
execute at @s[scores={an_curse=21..40}] run particle dust 0.275 0.153 0.373 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 4 normal
execute at @s[scores={an_curse=41..60}] run particle dust 0.353 0.2 0.478 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 5 normal
execute at @s[scores={an_curse=61..80}] run particle dust 0.463 0.259 0.627 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 7 normal
execute at @s[scores={an_curse=81..100}] run particle dust 0.569 0.318 0.773 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 10 normal
execute at @s[scores={an_curse=51..75}] run particle large_smoke ~ ~0.7 ~ 0.2 0.4 0.2 0.01 1 normal
execute at @s[scores={an_curse=76..100}] run particle large_smoke ~ ~0.7 ~ 0.3 0.5 0.3 0.01 2 normal

execute at @s[scores={an_curse=100..}] run function skills:offhand/an_curse_trigger