tag @s add spell_3_cast

scoreboard players set @s last_spell 3
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.illusioner.cast_spell master @p ~ ~ ~ 0.7 1.2 0.7

execute at @s run summon armor_stand ^ ^1.3 ^1 {Invulnerable:1b,Invisible:1b,Small:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["marker_tick","spell_marker","spell_marker_3"],Passengers:[{id:"minecraft:snowball",Tags:["spell_proj","spell_proj_3"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:2}}}]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_3] add spell_power

tag @s remove spell_3_cast