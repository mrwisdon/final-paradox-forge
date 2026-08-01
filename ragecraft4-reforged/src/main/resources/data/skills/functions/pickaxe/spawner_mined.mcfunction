execute as @s[predicate=skills:mana_mining] run function skills:pickaxe/mana_mining
effect give @s[predicate=skills:momentum] haste 12 1 true
execute at @s[predicate=skills:overcharge] run function skills:pickaxe/overcharge_mined
execute as @s[predicate=skills:blast_mining] run function skills:pickaxe/blast_mining
execute as @s[predicate=skills:celestial_reward] run function skills:pickaxe/celestial_reward

scoreboard players set @s spawner_mined 0

scoreboard players add @s chal_spawner_mined 1
