tag @s add spell_7_cast

scoreboard players set @s last_spell 7
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.evoker.cast_spell master @p ~ ~ ~ 0.5 0.9 0.5
execute at @s run playsound minecraft:entity.shulker.shoot master @p ~ ~ ~ 0.6 0.8 0.6

execute at @s run summon armor_stand ^ ^1.3 ^1 {Invulnerable:1b,Invisible:1b,Small:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["marker_tick","spell_marker","spell_marker_7"],Passengers:[{id:"minecraft:snowball",Tags:["spell_proj","spell_proj_7"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:5}}}]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_7] add spell_power

tag @s remove spell_7_cast