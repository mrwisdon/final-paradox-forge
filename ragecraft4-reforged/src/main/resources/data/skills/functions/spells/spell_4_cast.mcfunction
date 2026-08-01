tag @s add spell_4_cast

scoreboard players set @s last_spell 4
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.illusioner.cast_spell master @a[distance=..15] ~ ~ ~ 0.5 1.2 0.5
execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..15] ~ ~ ~ 0.2 0.7 0.2

execute at @s run particle sneeze ^ ^1 ^2 0.5 0.5 0.5 0.1 15 force
execute at @s run particle spit ^ ^1 ^2 0.1 0.1 0.1 0.1 2 force

execute at @s run summon armor_stand ^2 ^1.3 ^3 {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_4","spell_marker_4_1"]}
execute at @s run summon armor_stand ^-2 ^1.3 ^3 {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_4","spell_marker_4_2"]}
execute as @s[predicate=skills:spell_power] run tag @e[type=armor_stand,tag=spell_4_marker] add spell_power
data modify entity @e[type=armor_stand,tag=spell_marker_4_1,limit=1,sort=nearest] Rotation set from entity @p[tag=spell_4_cast] Rotation
data modify entity @e[type=armor_stand,tag=spell_marker_4_2,limit=1,sort=nearest] Rotation set from entity @p[tag=spell_4_cast] Rotation
execute as @e[type=armor_stand,tag=spell_marker_4] run function skills:spells/spell_4_raycast
kill @e[type=armor_stand,tag=spell_marker_4]
execute at @s run execute as @e[tag=!spell_immune,tag=spell_4_target,distance=..17] run function skills:spells/spell_4_hit
execute at @s run execute as @e[tag=!spell_immune,tag=spell_4_target2,distance=..17] run function skills:spells/spell_4_hit2
execute at @s if entity @e[tag=spell_4_target,tag=spell_immune,distance=..17] run function skills:spells/spell_4_immune
execute at @s if entity @e[tag=spell_4_target2,tag=spell_immune,distance=..17] run function skills:spells/spell_4_immune

effect give @s[scores={power_drain_buff=1}] strength 3 1 true
effect give @s[scores={power_drain_buff=2}] strength 6 1 true
effect give @s[scores={power_drain_buff=3}] strength 9 1 true
effect give @s[scores={power_drain_buff=4..}] strength 12 1 true
scoreboard players set @s power_drain_buff 0

tag @e[tag=spell_4_target] remove spell_4_target
tag @e[tag=spell_4_target2] remove spell_4_target2

tag @s remove spell_4_cast