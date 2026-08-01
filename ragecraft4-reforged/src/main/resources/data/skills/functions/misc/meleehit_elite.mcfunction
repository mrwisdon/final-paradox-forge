execute as @s[predicate=skills:energize,scores={mana=20..}] run function skills:sword/energize
execute at @s[predicate=skills:decapitate_1] run function skills:axe/decapitate_1
execute at @s[predicate=skills:decapitate_2] run function skills:axe/decapitate_2
execute at @s[predicate=skills:decapitate_3] run function skills:axe/decapitate_3

advancement revoke @s only skills:meleehit_elite