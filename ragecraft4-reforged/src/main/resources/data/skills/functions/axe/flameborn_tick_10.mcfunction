execute as @e[type=#minecraft:can_be_on_fire,predicate=!mobs:is_on_fire,nbt=!{ActiveEffects:[{Id:12}]},tag=!no_target,distance=..7] run function skills:axe/flameborn_hit

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_3x2