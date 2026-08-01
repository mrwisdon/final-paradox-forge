effect give @s slowness 3 6 false
effect give @s weakness 3 10 false
damage @s[tag=!no_target] 12 player_attack by @p[tag=l_warp_cast,scores={l_warp_range=..3}]
damage @s[tag=!no_target] 16 player_attack by @p[tag=l_warp_cast,scores={l_warp_range=4..5}]
damage @s[tag=!no_target] 20 player_attack by @p[tag=l_warp_cast,scores={l_warp_range=6..7}]
damage @s[tag=!no_target] 25 player_attack by @p[tag=l_warp_cast,scores={l_warp_range=8..9}]
damage @s[tag=!no_target] 30 player_attack by @p[tag=l_warp_cast,scores={l_warp_range=10..}]
tag @s add hurt
