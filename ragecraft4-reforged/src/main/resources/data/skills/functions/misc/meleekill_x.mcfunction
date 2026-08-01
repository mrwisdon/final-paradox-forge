effect give @s[predicate=skills:rampage] strength 3 0 true
effect give @s[predicate=skills:bloodthirst] strength 4 1 true
effect give @s[predicate=skills:bloodthirst] speed 4 0 true

execute as @s[predicate=skills:mana_leech_1] run function skills:sword/mana_leech_1
execute as @s[predicate=skills:mana_leech_2] run function skills:sword/mana_leech_2
execute as @s[predicate=skills:mana_leech_3] run function skills:sword/mana_leech_3
effect give @s[predicate=skills:life_leech_1] regeneration 2 1 true
effect give @s[predicate=skills:life_leech_2] regeneration 4 1 true

execute at @s[predicate=skills:malady] if entity @s[predicate=skills:axe] run function skills:chest/wicked_shield

execute as @s[predicate=skills:a_arson] run function skills:leggings/arson
execute as @s[predicate=skills:a_healing] run function skills:leggings/healing
execute at @s[predicate=skills:a_runebinding] run function skills:leggings/runebinding
execute as @s[predicate=skills:a_thunder] run function skills:leggings/thunder
execute as @s[predicate=skills:a_witchcraft] run function skills:leggings/witchcraft
execute as @s[predicate=skills:a_faith] run function skills:leggings/faith

scoreboard players add @s[scores={brawler_timer=1..,mana=..19}] mana 1

execute at @s[predicate=skills:infestation,scores={mana=10..},advancements={skills:witherkill_melee=true}] run function skills:sword/infestation_kill
execute at @s[predicate=skills:blight_orb,scores={blight_orb_duration=..0},advancements={skills:witherkill_melee=true}] run function skills:offhand/blight_orb
execute at @s[predicate=skills:blight_orb,scores={blight_orb_duration=1..},advancements={skills:witherkill_melee=true}] run function skills:offhand/blight_orb_refresh

advancement revoke @s only skills:meleekill_x
advancement revoke @s only skills:witherkill_melee