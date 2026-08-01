execute at @s[predicate=skills:flameborn,scores={flameborn_duration=..0,mana=20..}] run function skills:axe/flameborn
execute at @s[predicate=skills:flameborn,scores={flameborn_duration=1..}] run function skills:axe/flameborn_refresh

execute as @s[predicate=skills:mana_frenzy] run function skills:helmet/mana_frenzy

effect give @s[predicate=skills:granite_skin] resistance 3 0 true
effect give @s[predicate=skills:adrenaline_rush] speed 6 0 true
effect give @s[predicate=skills:adrenaline_rush] haste 6 0 true
effect give @s[predicate=skills:vt_adrenaline_rush] speed 8 1 true
effect give @s[predicate=skills:vt_adrenaline_rush] haste 8 1 true

advancement revoke @s only skills:anykill