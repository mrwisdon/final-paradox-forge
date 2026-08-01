effect give @s[type=#minecraft:non_undead] instant_damage 1 1
effect give @s[type=#minecraft:undead] instant_health 1 1
execute as @s run function ragecraft4reforged:damage/damage6

execute at @s run playsound minecraft:entity.wither.hurt master @a[distance=..20] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound minecraft:entity.wither.hurt master @a[distance=..13] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.wither.hurt master @a[distance=..7] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..13] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..7] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..20] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:item.totem.use master @a[distance=..13] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..7] ~ ~ ~ 0.1 0.8 0.1

execute at @s run particle dust 0.643 0.416 0.827 1 ~ ~0.7 ~ 0.6 0.8 0.6 0.1 250 normal
execute at @s run particle reverse_portal ~ ~0.7 ~ 0.6 0.8 0.6 0.1 300 normal
execute at @s run particle squid_ink ~ ~0.7 ~ 0.5 0.5 0.5 0.01 25 normal

scoreboard players set @s an_curse 0