execute at @s run summon marker ~ ~ ~ {Tags:["frostbite_anim"]}
execute as @e[type=marker,tag=frostbite_anim] run function skills:sword/frostbite_anim

execute as @s run function ragecraft4reforged:damage/damage8