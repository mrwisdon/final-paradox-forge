scoreboard players set @s dark_orb_duration 240
playsound entity.glow_squid.death master @s ~ ~ ~ 0.6 1.2 0.6
playsound block.beehive.enter master @s ~ ~ ~ 1 0.8 1
playsound block.beehive.enter master @s ~ ~ ~ 1 0.8 1
execute at @s run playsound block.frogspawn.place master @s ~ ~ ~ 0.8 0.8 0.8

execute at @s unless entity @e[type=armor_stand,tag=dark_orb_proj,distance=..8] run summon armor_stand ~ ~0.2 ~3 {NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Invisible:1b,Tags:["marker_tick","dark_orb_proj"],Passengers:[{id:"minecraft:snowball",Tags:["dark_orb_proj"],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:5}}}]}
scoreboard players set @s dark_orb_animation 0