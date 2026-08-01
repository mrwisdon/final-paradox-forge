execute at @s run particle sneeze ~ ~0.3 ~ 0.4 0.6 0.4 0.01 8 normal
execute at @s run particle sneeze ~ ~ ~ 1.6 0.1 1.6 0.01 5 normal
execute at @s run particle totem_of_undying ~ ~0.2 ~ 0.7 0.5 0.7 0.5 2 normal
execute at @s run particle glow_squid_ink ~ ~0.2 ~ 0.3 0.3 0.3 0.01 2 normal
execute at @s run particle dust 0.22 0.475 0.282 1 ~ ~0.2 ~ 1.6 0.2 1.6 0.01 8 normal

execute if score #rc4tick tick10 matches 1 run playsound block.fire.extinguish master @a[distance=10] ~ ~ ~ 0.1 0.8 0.1
execute if score #rc4tick tick20 matches 1 run function skills:potions/potion_8_wither
execute at @s run effect give @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] slowness 8 1 true

scoreboard players add @s cd 1
kill @s[scores={cd=400..}]