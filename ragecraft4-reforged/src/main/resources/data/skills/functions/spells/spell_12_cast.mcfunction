tag @s add spell_12_cast

scoreboard players set @s last_spell 12
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.evoker.prepare_attack master @a[distance=..15] ~ ~ ~ 0.5 0.7 0.5
execute at @s run playsound minecraft:item.trident.riptide_1 master @a[distance=..15] ~ ~ ~ 0.6 0.5 0.6

execute at @s run summon armor_stand ^ ^1.3 ^1 {Invulnerable:1b,Invisible:1b,Small:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["marker_tick","spell_marker","spell_marker_12"],Passengers:[{id:"minecraft:snowball",Tags:["spell_proj","spell_proj_12"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:10}}}]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_12] add spell_power

tag @s remove spell_12_cast