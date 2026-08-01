scoreboard players remove @s[scores={blood_pact_cd=1..}] blood_pact_cd 1
effect give @s[scores={blood_pact_cd=2..}] strength 1 2 true
execute as @s[scores={blood_pact_cd=1}] run function skills:helmet/blood_pact_end
effect give @s[scores={blood_pact_cd=0}] weakness 1 0 true

execute at @s[scores={blood_pact_cd=2..}] run particle dust 0.345 0.039 0.039 1 ~ ~0.6 ~ 0.4 0.5 0.4 0.2 15 force
execute at @s[scores={blood_pact_cd=2..}] run particle dust 0.345 0.039 0.039 1 ~ ~ ~ 0.3 0.1 0.3 0.2 30 force