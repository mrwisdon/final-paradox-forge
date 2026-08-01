scoreboard players add @s chal_mana_spent 10

scoreboard players remove @s mana 10

execute at @s run particle glow_squid_ink ^ ^1.5 ^3 0.2 0.2 0.2 0.02 20 normal
execute at @s run particle sneeze ^ ^1.5 ^3 0.5 0.5 0.5 0.01 80 normal
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 0.7 1.2 0.7
execute at @s run playsound block.beehive.enter master @a[distance=..12] ~ ~ ~ 0.4 1 0.4 

summon armor_stand ^ ^1.5 ^3 {NoGravity:0b,Silent:1b,Invulnerable:1b,Small:1b,Invisible:1b,Tags:["marker_tick","infestation"]}