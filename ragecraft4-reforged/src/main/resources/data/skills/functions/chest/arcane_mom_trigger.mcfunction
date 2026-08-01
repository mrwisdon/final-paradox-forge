scoreboard players set @s arcane_mom 1800

playsound entity.generic.explode master @a[distance=..15] ~ ~ ~ 0.2 1 0.2
playsound entity.generic.explode master @a[distance=..8] ~ ~ ~ 0.1 1 0.1
playsound entity.warden.sonic_boom master @a[distance=..15] ~ ~ ~ 0.2 1.5 0.2
playsound entity.warden.sonic_boom master @a[distance=..8] ~ ~ ~ 0.2 1.5 0.2
playsound entity.illusioner.prepare_mirror master @a[distance=..15] ~ ~ ~ 0.6 1.3 0.6
playsound entity.illusioner.prepare_mirror master @a[distance=..8] ~ ~ ~ 0.6 1.3 0.6

particle warped_spore ~ ~0.5 ~ 1 0.5 1 0.03 800 normal
particle dust 0.278 0.149 0.561 1 ~ ~0.5 ~ 0.8 0.8 0.8 0.1 500 normal
particle squid_ink ~ ~0.3 ~ 1.4 0.7 1.4 0.05 400 normal
particle explosion ~ ~0.1 ~ 1.6 0.1 1.6 0.05 30 normal
particle campfire_cosy_smoke ~ ~0.1 ~ 1.1 0.3 1.1 0.03 30 normal

execute at @s run execute as @e[tag=r4r_ability_target,distance=..8] at @s run damage @s 20 magic by @p[distance=..10,scores={arcane_mom=1800}]