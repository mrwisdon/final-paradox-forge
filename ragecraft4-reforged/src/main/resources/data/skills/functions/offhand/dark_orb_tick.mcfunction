scoreboard players remove @s[scores={dark_orb_duration=1..}] dark_orb_duration 1
scoreboard players add @s mob_animation 1

execute at @s[scores={mob_animation=1}] positioned ~4 ~0.2 ~ run function skills:offhand/dark_orb_proj
execute at @s[scores={dmob_animation=2}] positioned ~3.8 ~0.2 ~1.1 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=3}] positioned ~3.5 ~0.2 ~1.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=4}] positioned ~3.2 ~0.2 ~2.35 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=5}] positioned ~2.8 ~0.2 ~2.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=6}] positioned ~2.35 ~0.2 ~3.2 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=7}] positioned ~1.8 ~0.2 ~3.5 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=8}] positioned ~1.1 ~0.2 ~3.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=9}] positioned ~ ~0.2 ~4 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=10}] positioned ~-1.1 ~0.2 ~3.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=11}] positioned ~-1.8 ~0.2 ~3.5 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=12}] positioned ~-2.35 ~0.2 ~3.2 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=13}] positioned ~-2.8 ~0.2 ~2.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=14}] positioned ~-3.2 ~0.2 ~2.35 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=15}] positioned ~-3.5 ~0.2 ~1.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=16}] positioned ~-3.8 ~0.2 ~1.1 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=17}] positioned ~-4 ~0.2 ~ run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=18}] positioned ~-3.8 ~0.2 ~-1.1 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=19}] positioned ~-3.5 ~0.2 ~-1.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={dmob_animation=20}] positioned ~-3.2 ~0.2 ~-2.35 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=21}] positioned ~-2.8 ~0.2 ~-2.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=22}] positioned ~-2.35 ~0.2 ~-3.2 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=23}] positioned ~-1.8 ~0.2 ~-3.5 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=24}] positioned ~-1.1 ~0.2 ~-3.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=25}] positioned ~ ~0.2 ~-4 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=26}] positioned ~1.1 ~0.2 ~-3.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=27}] positioned ~1.8 ~0.2 ~-3.5 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=28}] positioned ~2.35 ~0.2 ~-3.2 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=29}] positioned ~2.8 ~0.2 ~-2.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=30}] positioned ~3.2 ~0.2 ~-2.35 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=31}] positioned ~3.5 ~0.2 ~-1.8 run function skills:offhand/dark_orb_proj
execute at @s[scores={mob_animation=32}] positioned ~3.8 ~0.2 ~-1.1 run function skills:offhand/dark_orb_proj

scoreboard players set @s[scores={mob_animation=32..}] mob_animation 0
execute at @s[scores={dark_orb_duration=..0}] run function skills:offhand/dark_orb_end