tag @s add runebinding_attack

scoreboard players set @s power_burst_buffs 0

scoreboard players add @s[nbt={ActiveEffects:[{Id:1}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:3}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:5}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:8}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:10}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:11}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:12}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:13}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:14}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:22}]}] power_burst_buffs 1

execute at @s[scores={power_burst_buffs=1..}] if score #rc4tick tick10 matches 1 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=2..}] if score #rc4tick tick10 matches 2 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=3..}] if score #rc4tick tick10 matches 3 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=4..}] if score #rc4tick tick10 matches 4 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=5..}] if score #rc4tick tick10 matches 5 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=6..}] if score #rc4tick tick10 matches 6 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=7..}] if score #rc4tick tick10 matches 7 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=8..}] if score #rc4tick tick10 matches 8 run function skills:leggings/runebinding_trig
execute at @s[scores={power_burst_buffs=9..}] if score #rc4tick tick10 matches 9 run function skills:leggings/runebinding_trig

scoreboard players set @s power_burst_buffs 0

tag @s remove runebinding_attack
