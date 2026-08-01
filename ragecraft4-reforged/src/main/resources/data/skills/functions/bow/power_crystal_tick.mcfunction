execute at @s run tp @s ~ ~ ~ ~5 ~

execute at @s run effect give @p[distance=..5] resistance 1 0 true
execute at @s run effect give @p[distance=..5] strength 1 1 false

execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^ ^ ^1.5 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^1.5 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^ ^ ^-1.5 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^-1.5 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^ ^ ^4.5 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^4.5 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^ ^ ^-4.5 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.451 0.122 0.478 1 ^-4.5 ^ ^ 0 0 0 0.01 1 normal

execute at @s run particle witch ~ ~1 ~ 0.8 0.4 0.8 0.01 1 normal
execute at @s run particle dust 0.459 0.071 0.459 1 ~ ~1 ~ 0.1 0.4 0.1 0.1 3 normal