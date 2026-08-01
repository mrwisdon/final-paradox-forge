execute at @s run damage @s 12 magic by @p[scores={spell_cd=..4,last_spell=5}]

effect give @s ragecraft4reforged:vulnerability 20 0 false
effect give @s wither 20 0 false

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={spell_cd=..4,last_spell=5}] chal_hit_spell 1
tag @s add hit_by_spell
