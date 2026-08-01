execute as @a[tag=!r4r_suffix_init] run function ragecraft4reforged:suffix_init_player
scoreboard players add #r4r tick10 1
scoreboard players add #r4r tick20 1
scoreboard players add #r4r tick40 1
scoreboard players add #r4r tick60 1
scoreboard players operation #rc4tick tick10 = #r4r tick10
scoreboard players operation #rc4tick tick20 = #r4r tick20
scoreboard players operation #rc4tick tick40 = #r4r tick40
scoreboard players operation #rc4tick tick60 = #r4r tick60
execute as @a at @s run function ragecraft4reforged:suffix_player_tick
execute as @e[tag=marker_tick] at @s run function ragecraft4reforged:suffix_marker_tick
execute as @e[tag=marker_tick,type=armor_stand] at @s run function ragecraft4reforged:suffix_marker_tick_a
execute as @e[tag=marker_tick,type=marker] at @s run function ragecraft4reforged:suffix_marker_tick_m
execute as @e[tag=marker_tick,type=snowball] at @s run function ragecraft4reforged:suffix_marker_tick_s
execute as @e[type=arrow,tag=arrow_tick] at @s run function skills:bow/arrow_tick
execute as @e[type=arrow,scores={volley_1_delay=1..}] at @s run function skills:bow/volley_1_delay
execute as @e[type=arrow,scores={volley_2_delay=1..}] at @s run function skills:bow/volley_2_delay
execute as @e[tag=r4r_ability_target] at @s run function ragecraft4reforged:suffix_mob_tick
execute if score #r4r tick10 matches 10 as @e[tag=r4r_ability_target] at @s run function ragecraft4reforged:suffix_mob_tick10
execute if score #r4r tick20 matches 20 as @e[tag=r4r_ability_target] at @s run function ragecraft4reforged:suffix_mob_tick20
execute if score #r4r tick10 matches 10.. run scoreboard players set #r4r tick10 0
execute if score #r4r tick20 matches 20.. run scoreboard players set #r4r tick20 0
execute if score #r4r tick40 matches 40.. run scoreboard players set #r4r tick40 0
execute if score #r4r tick60 matches 60.. run scoreboard players set #r4r tick60 0
