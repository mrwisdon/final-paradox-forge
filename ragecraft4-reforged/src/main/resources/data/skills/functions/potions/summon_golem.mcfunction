execute at @s run summon iron_golem ~ ~ ~ {Invulnerable:1b,Glowing:1b,DeathLootTable:"none",NoAI:1b,PlayerCreated:1b,Tags:["custom","summoned_golem","mob_tick"],ActiveEffects:[{Id:14,Amplifier:0b,Duration:40}]}
execute at @s run particle soul_fire_flame ~ ~1 ~ 0.8 1.5 0.8 0.01 300 force
execute at @s run particle poof ~ ~ ~ 0.6 1.1 0.6 0.01 80 force
execute at @s run playsound entity.warden.dig master @a[distance=..20] ~ ~ ~ 0.5 1.3 0.5
kill @s