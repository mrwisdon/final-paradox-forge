execute at @s run particle dust 0.796 0.671 0.941 1 ~ ~1 ~ 0.2 0.4 0.2 0.1 300 normal
execute at @s run particle electric_spark ~ ~1 ~ 0.3 0.5 0.3 0.01 200 normal
execute at @s run particle end_rod ~ ~1 ~ 0.05 0.6 0.05 0.01 25 normal
execute at @s run playsound entity.glow_squid.death master @a[distance=..20] ~ ~ ~ 0.3 1.4 0.3
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..20] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..20] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound block.fire.extinguish master @a[distance=..20] ~ ~ ~ 0.05 1.2 0.05
execute at @s run playsound entity.glow_squid.death master @a[distance=..11] ~ ~ ~ 0.3 1.4 0.3
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..11] ~ ~ ~ 0.15 1.2 0.15
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..11] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound block.fire.extinguish master @a[distance=..11] ~ ~ ~ 0.05 1.2 0.05

execute at @s run tag @e[tag=r4r_ability_target,limit=1,sort=nearest,distance=..1] add power_burst_target
execute at @s run execute as @e[tag=r4r_ability_target,nbt={HurtTime:10s},limit=1,sort=nearest,distance=..1] run function ragecraft4reforged:damage/damage26
execute at @s run execute as @e[tag=r4r_ability_target,nbt=!{HurtTime:10s},limit=1,sort=nearest,distance=..1] run damage @s 26 magic by @p[tag=power_burst_attack_2]