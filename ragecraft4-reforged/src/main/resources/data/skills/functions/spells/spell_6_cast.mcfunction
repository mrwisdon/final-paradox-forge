tag @s add spell_6_cast
scoreboard players set @s last_spell 6
scoreboard players set @s spell_cd 0

execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..15] ~ ~ ~ 0.2 1.3 0.2
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..15] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..7] ~ ~ ~ 0.15 1.3 0.15
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..7] ~ ~ ~ 0.3 0.8 0.3

execute at @s positioned ^ ^1 ^2 rotated as @s run summon armor_stand ~ ~ ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_6"]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,tag=spell_marker_6] add spell_power
data modify entity @e[type=armor_stand,tag=spell_marker_6,limit=1,sort=nearest] Rotation set from entity @p[tag=spell_6_cast] Rotation
execute as @e[type=armor_stand,tag=spell_marker_6] run function skills:spells/spell_6_raycast
kill @e[type=armor_stand,tag=spell_marker_6]
execute at @s run execute as @e[tag=spell_6_target,tag=!spell_immune,distance=..15] run function skills:spells/spell_6_hit
execute at @s run execute as @e[tag=spell_6_target2,tag=!spell_immune,distance=..15] run function skills:spells/spell_6_hit2
execute at @s if entity @e[tag=spell_6_target,tag=spell_immune,distance=..15] run function skills:spells/spell_4_immune
execute at @s if entity @e[tag=spell_6_target2,tag=spell_immune,distance=..15] run function skills:spells/spell_4_immune

effect give @s[scores={wind_blast_buff=1}] speed 3 1 true
effect give @s[scores={wind_blast_buff=2}] speed 6 1 true
effect give @s[scores={wind_blast_buff=3}] speed 9 1 true
effect give @s[scores={wind_blast_buff=4}] speed 12 1 true
effect give @s[scores={wind_blast_buff=5..}] speed 15 1 true
scoreboard players set @s wind_blast_buff 0

tag @e[tag=spell_6_target] remove spell_6_target
tag @e[tag=spell_6_target2] remove spell_6_target2

tag @s remove spell_6_cast