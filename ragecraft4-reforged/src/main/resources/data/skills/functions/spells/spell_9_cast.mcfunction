tag @s add spell_9_cast

scoreboard players add @s[scores={mana=19}] mana 1
scoreboard players add @s[scores={mana=18}] mana 2
scoreboard players add @p[scores={mana=17}] mana 3
scoreboard players add @p[scores={mana=16}] mana 4
scoreboard players add @p[scores={mana=15}] mana 5
scoreboard players add @p[scores={mana=..14}] mana 6

scoreboard players set @s last_spell 9
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.illusioner.cast_spell master @s ~ ~ ~ 0.6 1.2 0.6

execute at @s[predicate=!skills:spell_power] run summon armor_stand ^ ^1.3 ^1 {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_9","spell_marker_9_1"]}
execute at @s[predicate=skills:spell_power] run summon armor_stand ^ ^1.3 ^1 {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_9","spell_marker_9_2"]}
data modify entity @e[type=armor_stand,tag=spell_marker_9,limit=1,sort=nearest] Rotation set from entity @p[tag=spell_9_cast] Rotation
execute as @e[type=armor_stand,tag=spell_marker_9] at @s run function skills:spells/spell_9_raycast
execute at @e[type=armor_stand,tag=spell_marker_9_1] run summon marker ~ ~ ~ {Tags:["marker_tick","spell_marker","spell_center_9","spell_center_9_1"]}
execute at @e[type=armor_stand,tag=spell_marker_9_2] run summon marker ~ ~ ~ {Tags:["marker_tick","spell_marker","spell_center_9","spell_center_9_2"]}
kill @e[type=armor_stand,tag=spell_marker_9]

tag @s remove spell_9_cast