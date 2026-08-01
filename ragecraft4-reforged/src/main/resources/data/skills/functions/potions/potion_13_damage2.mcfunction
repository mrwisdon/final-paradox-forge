scoreboard players set @s bottled_lightning 1
effect give @s slowness 4 6 false
execute as @s run function ragecraft4reforged:damage/damage8
execute as @s[predicate=skills:has_vulnerability] run function ragecraft4reforged:damage/damage8

execute at @s run particle dust 1 0.988 0.875 1 ~ ~1 ~ 0.3 0.5 0.3 0.3 30 normal
execute at @s run particle electric_spark ~ ~1 ~ 0.1 1 0.1 0.3 40 normal
execute at @s run particle enchanted_hit ~ ~1 ~ 0.1 1 0.1 1 30 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..15] ~ ~ ~ 0.7 1.5 0.7
execute at @s run playsound block.fire.extinguish master @a[distance=..15] ~ ~ ~ 0.4 1.5 0.4
