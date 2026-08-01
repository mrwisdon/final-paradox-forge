advancement revoke @s only skills:soul_reaper_5

execute at @s[predicate=skills:soul_reaper] run function skills:helmet/soul_reaper

execute at @s[predicate=skills:vt_dark_orb,scores={dark_orb_duration=..0}] run function skills:offhand/dark_orb
execute at @s[predicate=skills:vt_dark_orb,scores={dark_orb_duration=1..}] run function skills:offhand/dark_orb_refresh