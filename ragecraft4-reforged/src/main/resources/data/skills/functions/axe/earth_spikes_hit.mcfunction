effect give @s slowness 2 6 true
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 8 player_attack by @p[scores={delayed_att=1..}]
tag @s add hurt

tag @s add earth_spikes_hit