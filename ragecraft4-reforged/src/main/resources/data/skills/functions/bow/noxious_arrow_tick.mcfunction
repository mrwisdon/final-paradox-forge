execute at @s[nbt={inGround:0b}] run particle dust 0.094 0.125 0.09 1 ~ ~0.3 ~ 0.1 0.1 0.1 0.2 8 force

scoreboard players add @s[nbt={inGround:1b}] cd 1
execute at @s[nbt={inGround:1b}] run particle totem_of_undying ~ ~0.3 ~ 0.5 0.3 0.5 0.5 2 normal
execute at @s[nbt={inGround:1b}] run particle dust 0.094 0.125 0.09 1 ~ ~0.3 ~ 0.8 0.4 0.8 0.2 12 normal

execute at @s[scores={cd=1}] run function skills:bow/noxious_arrow_gr_0

kill @s[scores={cd=200..}]