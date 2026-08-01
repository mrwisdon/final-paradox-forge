#see function skills:axe/jump_hit

scoreboard players add @s chal_mana_spent 8

scoreboard players remove @s mana 8

tag @s add devour_attack

execute at @s run playsound block.beehive.enter master @a[distance=..12] ~ ~ ~ 0.4 1 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 0.6 1.2 0.6

execute at @s run particle dust 0.345 0.039 0.039 1 ^ ^1 ^2.5 0.3 0.6 0.3 0.2 150 normal

execute at @s[scores={mob_kills=1..}] run function skills:axe/devour_kill
execute at @s positioned ^ ^1 ^2.5 run execute as @e[tag=r4r_ability_target,nbt={HurtTime:10s},tag=!no_target,distance=..4,limit=1,sort=nearest] run function skills:axe/devour_hit

tag @s remove devour_attack