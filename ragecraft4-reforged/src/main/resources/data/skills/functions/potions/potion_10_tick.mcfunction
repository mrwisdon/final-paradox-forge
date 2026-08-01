execute at @s run particle lava ~ ~0.1 ~ 1.4 0.6 1.4 0.01 1 normal
execute at @s run particle falling_lava ~ ~0.1 ~ 1.6 0.1 1.6 0.01 6 normal
execute at @s run particle dust 0.733 0.376 0.082 1 ~ ~0.1 ~ 1.6 0.2 1.6 0.01 6 normal

execute at @s if score #rc4tick tick10 matches 5 run playsound block.campfire.crackle master @a[distance=..15] ~ ~ ~ 0.25 0.7 0.25
execute at @s if score #rc4tick tick10 matches 5 run playsound block.campfire.crackle master @a[distance=..11] ~ ~ ~ 0.15 0.7 0.15
execute at @s if score #rc4tick tick10 matches 5 run playsound block.campfire.crackle master @a[distance=..7] ~ ~ ~ 0.1 0.7 0.1

execute at @s run execute as @e[type=#minecraft:can_be_on_fire,tag=!spell_immune,distance=..4] run function skills:misc/setonfire_5s

scoreboard players add @s cd 1
kill @s[scores={cd=400..}]