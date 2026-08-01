tag @s add spell_5_cast

scoreboard players set @s last_spell 5
scoreboard players set @s spell_cd 0
execute at @s run playsound entity.fox.spit master @a[distance=..10] ~ ~ ~ 1 0.7 1
execute at @s run playsound block.beehive.enter master @a[distance=..10] ~ ~ ~ 0.3 1.3 0.3
execute at @s run playsound entity.zombie.infect master @a[distance=..10] ~ ~ ~ 0.2 1 0.2

execute at @s run summon armor_stand ^0.3 ^1.1 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx1","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx1","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^-0.3 ^1.1 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx2","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx2","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^ ^0.9 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx3","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx3","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^ ^1.3 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx4","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx4","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^0.15 ^1.2 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx5","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx5","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^-0.15 ^1.0 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx6","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx6","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}
execute at @s run summon armor_stand ^-0.1 ^1.3 ^1 {Silent:1b,Invulnerable:1b,Invisible:1b,Marker:1b,NoGravity:1b,HasVisualFire:0b,Tags:["xx7","marker_tick","spell_marker","spell_marker_5"],Passengers:[{id:"minecraft:snowball",Tags:["xx7","spell_proj","spell_proj_5"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:9}}}]}

execute at @s[predicate=skills:spell_power] run tag @e[type=armor_stand,limit=1,sort=nearest,tag=spell_marker_5] add spell_power

tag @s remove spell_5_cast