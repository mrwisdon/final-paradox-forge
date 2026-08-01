scoreboard players remove @s[scores={mana=4..}] mana 4
scoreboard players add @s chal_mana_spent 4

execute at @s run playsound block.glass.break master @a[distance=..12] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..12] ~ ~ ~ 0.5 1.2 0.5
execute at @s run playsound entity.glow_squid.squirt master @a[distance=..12] ~ ~ ~ 0.2 1.5 0.2

execute at @s run particle dust 0.816 0.89 1 2 ^ ^1.3 ^2.5 0.5 0.5 0.5 0.5 40 normal
execute at @s run particle block blue_ice ^ ^1.3 ^2.5 0.4 0.4 0.4 0.5 15 normal
execute at @s run particle item_snowball ^ ^1.3 ^2.5 0.4 0.4 0.4 0.2 15 normal

execute at @s run execute as @e[tag=r4r_ability_target,nbt={HurtTime:10s},nbt={ActiveEffects:[{Id:2}]},distance=..6] at @s run function skills:sword/frostbite_hit