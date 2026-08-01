scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 10
tag @s add shadow_cast

execute at @s positioned ^ ^1 ^2 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^3 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^4 run function skillsaxe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^5 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^6 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^7 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^8 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^9 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^10 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^11 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^12 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^13 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^14 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^15 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^16 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^17 run function skills:axe/shadow_grasp_2
execute at @s[tag=!shadow_found,tag=!shadow_wall] positioned ^ ^1 ^18 run function skills:axe/shadow_grasp_2

execute as @s[tag=!shadow_found] run function skills:axe/shadow_grasp_failed
execute at @s[tag=shadow_found] run function skills:axe/shadow_grasp_trigger

tag @s remove shadow_wall
tag @s remove shadow_found
tag @s remove shadow_cast