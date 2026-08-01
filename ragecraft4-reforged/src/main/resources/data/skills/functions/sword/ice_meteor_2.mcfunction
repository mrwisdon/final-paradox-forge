scoreboard players add @s chal_mana_spent 10

scoreboard players set @s ice_meteor_cd 27
tag @s add ice_meteor_attack

scoreboard players remove @s mana 10

summon snowball ~ ~5 ~ {Tags:["ice_meteor"],Motion:[0.0,0.0,0.0],Passengers:[{id:"minecraft:armor_stand",NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,Tags:["marker_tick","spell_marker","ice_meteor"]}],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:7}}}

particle dust 0.667 0.792 0.867 1 ~ ~0.2 ~ 1.3 1 1.3 0.1 120 normal
particle block blue_ice ~ ~0.2 ~ 1.6 0.3 1.6 0.5 50 normal

execute at @s run playsound entity.illusioner.prepare_mirror master @s ~ ~ ~ 0.7 1.5 0.7