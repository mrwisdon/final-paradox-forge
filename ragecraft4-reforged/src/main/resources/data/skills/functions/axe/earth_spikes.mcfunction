scoreboard players add @s chal_mana_spent 20

scoreboard players set @s delayed_att 40

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

summon marker ^ ^1 ^4 {NoGravity:1b,Silent:1b,Invulnerable:1b,Tags:["marker_tick","earth_spikes","earth_spikes_a"]}
summon marker ^ ^1 ^6 {NoGravity:1b,Silent:1b,Invulnerable:1b,Tags:["marker_tick","earth_spikes","earth_spikes_b"]}
summon marker ^ ^1 ^8 {NoGravity:1b,Silent:1b,Invulnerable:1b,Tags:["marker_tick","earth_spikes","earth_spikes_c"]}
summon marker ^ ^1 ^10 {NoGravity:1b,Silent:1b,Invulnerable:1b,Tags:["marker_tick","earth_spikes","earth_spikes_d"]}
summon marker ^ ^1 ^12 {NoGravity:1b,Silent:1b,Invulnerable:1b,Tags:["marker_tick","earth_spikes","earth_spikes_e"]}

execute at @s run tag @e[tag=r4r_ability_target,tag=!no_target,distance=..25,tag=earth_spikes_hit] remove earth_spikes_hit