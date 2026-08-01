tag @s add spell_1_cast

scoreboard players set @s last_spell 1
scoreboard players set @s spell_cd 0
execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..15] ~ ~ ~ 0.6 0.6 0.4

execute at @s run summon armor_stand ^ ^1.3 ^1 {Invulnerable:1b,Invisible:1b,Small:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["marker_tick","spell_marker","spell_marker_1"],Passengers:[{id:"minecraft:snowball",Tags:["spell_proj","spell_proj_1"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:1}}}]}
execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_1] add spell_power

tag @s remove spell_1_cast