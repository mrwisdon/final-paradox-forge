scoreboard players add @s cd 1

execute at @s run particle dust 0.388 0.153 0.122 1 ^ ^ ^1.6 0.1 0.1 0.1 0.01 3 normal
execute at @s run particle flame ^ ^ ^1.6 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:sword/eruption_anim
kill @s[scores={cd=120..}]