execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage32
execute as @s[tag=!no_target,nbt=!{HurtTime:10s}] run function skills:sword/astral_hit_2

tag @s add hurt

execute at @s run playsound item.trident.hit master @a[distance=..25] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..25] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound block.anvil.place master @a[distance=..25] ~ ~ ~ 0.1 1.4 0.1
execute at @s run playsound item.trident.hit master @a[distance=..15] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..15] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound block.anvil.place master @a[distance=..15] ~ ~ ~ 0.1 1.4 0.1
execute at @s run playsound item.trident.hit master @a[distance=..8] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..8] ~ ~ ~ 0.05 1.2 0.05
execute at @s run playsound block.anvil.place master @a[distance=..8] ~ ~ ~ 0.1 1.4 0.1

execute at @s run particle crit ~ ~0.7 ~ 0.3 0.5 0.3 0.05 80 normal
execute at @s run particle sweep_attack ~ ~0.7 ~ 0.3 0.3 0.3 0.2 3 normal

tag @s add astral_done