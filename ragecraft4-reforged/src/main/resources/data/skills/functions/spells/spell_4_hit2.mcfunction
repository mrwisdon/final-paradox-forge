execute at @s run scoreboard players add @p[tag=spell_4_cast,scores={mana=19}] mana 1
execute at @s run scoreboard players add @p[tag=spell_4_cast,scores={mana=..18}] mana 2

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[tag=spell_4_cast] chal_hit_spell 1
tag @s add hit_by_spell

execute at @s run summon marker ~ ~ ~ {Tags:["spell_4_anim"]}
execute as @e[type=marker,tag=spell_4_anim] run function skills:spells/spell_4_anim

#effect give @s[type=#minecraft:non_undead] instant_damage 1 1
#effect give @s[type=#minecraft:undead] instant_health 1 1

execute at @s run damage @s 12 magic by @p[tag=spell_4_cast]

effect give @s weakness 12 0 false

execute at @s run scoreboard players add @p[tag=spell_4_cast,distance=..20] power_drain_buff 1

execute at @s if entity @p[tag=spell_4_cast,distance=..20] run summon marker ~ ~ ~ {Tags:["marker_tick","spell_4_extra"]}

#execute at @s run particle dust 0.475 0.686 0.51 1 ~ ~1 ~ 0.4 0.8 0.4 0.5 250 force
execute at @s run particle glow_squid_ink ~ ~1 ~ 0.3 0.6 0.3 0.05 15 force

tag @s remove spell_4_target