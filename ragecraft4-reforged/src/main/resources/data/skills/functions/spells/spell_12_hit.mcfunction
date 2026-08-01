execute at @s run particle squid_ink ~ ~1 ~ 0.3 0.6 0.3 0.01 50 force

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={spell_cd=..4,last_spell=12}] chal_hit_spell 1
tag @s add hit_by_spell

execute at @s run summon marker ~ ~ ~ {Tags:["spell_12_anim"]}
execute as @e[type=marker,tag=spell_12_anim] run function skills:spells/spell_12_anim

#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=19}] mana 1
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=18}] mana 2
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=17}] mana 3
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=16}] mana 4
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=15}] mana 5
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=14}] mana 6
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=13}] mana 7
#execute at @s run scoreboard players add @p[scores={last_spell=12,mana=..12}] mana 8

execute at @s run effect give @p[scores={last_spell=12}] haste 10 1 true
execute at @s run effect give @p[scores={last_spell=12}] speed 10 1 true

execute at @s run damage @s 80 magic by @p[scores={spell_cd=..4,last_spell=12}]

#effect give @s[type=#minecraft:non_undead] instant_damage 1 3
#effect give @s[type=#minecraft:undead] instant_health 1 3
#execute as @s run function ragecraft4reforged:damage/damage32