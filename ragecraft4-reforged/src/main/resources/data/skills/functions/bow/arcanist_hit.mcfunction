execute at @s run summon marker ~ ~0.4 ~ {Tags:["arcanist_anim"]}
execute at @s run summon marker ~ ~1 ~ {Tags:["arcanist_anim"]}
execute as @e[type=marker,tag=arcanist_anim] run function skills:bow/arcanist_anim

particle dust 0.875 0.784 0.886 1 ~ ~0.8 ~ 0.3 0.6 0.3 0.1 150 force
particle enchant ~ ~0.8 ~ 0.8 1.1 0.8 0.1 150 force

playsound entity.warden.sonic_boom master @a[distance=..40] ~ ~ ~ 0.1 1.3 0.1
playsound entity.warden.sonic_boom master @a[distance=..25] ~ ~ ~ 0.1 1.3 0.1
playsound entity.warden.sonic_boom master @a[distance=..12] ~ ~ ~ 0.1 1.3 0.1
playsound block.enchantment_table.use master @a[distance=..40] ~ ~ ~ 0.3 0.8 0.3
playsound block.enchantment_table.use master @a[distance=..25] ~ ~ ~ 0.3 0.8 0.3
playsound block.enchantment_table.use master @a[distance=..12] ~ ~ ~ 0.3 0.8 0.3
playsound entity.glow_squid.hurt master @a[distance=..40] ~ ~ ~ 0.2 1.3 0.2
playsound entity.glow_squid.hurt master @a[distance=..25] ~ ~ ~ 0.2 1.3 0.2
playsound entity.glow_squid.hurt master @a[distance=..12] ~ ~ ~ 0.2 1.3 0.2

execute at @s run execute as @p[scores={arcanist_timer=500..}] run function skills:bow/arcanist_mana

execute as @s run function ragecraft4reforged:damage/damage30