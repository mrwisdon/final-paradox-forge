tag @s add l_warp_cast

execute at @s positioned ^ ^1 ^2 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^3 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^4 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^5 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^6 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^7 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^8 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^9 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^10 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^11 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^12 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^13 run function skills:axe/l_warp_2
execute at @s[tag=!l_warp_found,tag=!l_warp_wall] positioned ^ ^1 ^14 run function skills:axe/l_warp_2
tag @e[tag=r4r_ability_target,tag=l_warp_target] remove l_warp_target

execute as @s[tag=l_warp_cast,tag=!l_warp_found] run function skills:axe/l_warp_failed
execute as @s[tag=l_warp_cast,tag=l_warp_found] run function skills:axe/l_warp_trigger

scoreboard players set @s l_warp_range 0
tag @s remove l_warp_wall
tag @s remove l_warp_found
tag @s remove l_warp_cast