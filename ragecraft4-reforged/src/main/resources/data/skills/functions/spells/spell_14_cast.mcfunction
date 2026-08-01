tag @s add cast_spell_14
scoreboard players set @s spell_cd 0

scoreboard players add @s[scores={mana=19}] mana 1
scoreboard players add @s[scores={mana=18}] mana 2
scoreboard players add @p[scores={mana=17}] mana 3
scoreboard players add @p[scores={mana=16}] mana 4
scoreboard players add @p[scores={mana=..15}] mana 5

effect give @s resistance 4 2 true
effect give @s invisibility 4 0 true

execute at @s run playsound minecraft:entity.illusioner.mirror_move master @a[distance=..15] ~ ~ ~ 1 0.8 1
execute at @s run playsound entity.shulker.teleport master @a[distance=..15] ~ ~ ~ 0.9 0.7 0.9
execute at @s run playsound minecraft:item.totem.use master @a[distance=..15] ~ ~ ~ 0.3 0.7 0.3

execute at @s positioned ^ ^1 ^ rotated as @s run summon armor_stand ~ ~ ~ {Silent:1b,NoGravity:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["spell_marker_14"]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_14] add spell_power

execute as @e[type=armor_stand,tag=spell_marker_14] at @s rotated as @p[tag=cast_spell_14] run tp ^ ^ ^

execute as @e[type=armor_stand,tag=spell_marker_14] at @s run function skills:spells/spell_14_raycast

tag @s remove cast_spell_14