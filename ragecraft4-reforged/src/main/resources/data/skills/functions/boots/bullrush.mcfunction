execute at @s positioned ^ ^1 ^1 run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..12] ~ ~ ~ 0.1 1.2 0.1
execute at @s positioned ^ ^1 ^1 run playsound minecraft:entity.zoglin.step master @a[distance=..12] ~ ~ ~ 1 0.8 1
execute at @s positioned ^ ^1 ^1 run particle campfire_cosy_smoke ~ ~0.5 ~ 1 0.3 1 0.1 30 normal
execute at @s positioned ^ ^1 ^1 run particle crit ~ ~0.5 ~ 1.2 0.5 1.2 0.2 200 normal
execute at @s positioned ^ ^1 ^1 run particle explosion ~ ~0.5 ~ 0.1 0.1 0.1 0.2 1 normal

tag @s add bullrush_attack
execute at @s[predicate=!skills:axe] positioned ^ ^1 ^1 run execute as @e[tag=r4r_ability_target,distance=..5] run function skills:boots/bullrush_hit
execute at @s[predicate=skills:axe] positioned ^ ^1 ^1 run execute as @e[tag=r4r_ability_target,distance=..5] run function skills:boots/bullrush_hit_axe
tag @s remove bullrush_attack

scoreboard players set @s bullrush_timer 0