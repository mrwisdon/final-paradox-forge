execute as @s[scores={charged_arrow_time=1..}] run function skills:bow/charged_arrow_mob
execute at @s if entity @p[predicate=skills:lethargy,distance=..7] run effect give @s slowness 1 0 false
execute at @s[predicate=!mobs:invisibility,tag=!boss] if entity @p[predicate=skills:truesight,distance=..9] run function skills:chest/truesight
execute at @s[tag=truesight] unless entity @p[predicate=skills:truesight,distance=..9] run data modify entity @s Glowing set value 0b
execute at @s[type=#minecraft:can_be_on_fire,predicate=mobs:is_on_fire] if entity @p[predicate=skills:soulfire,distance=..8] run function skills:chest/soulfire
execute at @s if entity @p[predicate=skills:ice_shield,scores={absorption_amount=1..},distance=..7] run effect give @s slowness 1 1 false
execute at @s[tag=spell_8_hit1] run function skills:spells/spell_8_hit1
execute at @s[tag=spell_8_hit2] run function skills:spells/spell_8_hit2
execute at @s[tag=spell_10_hit1] run function skills:spells/spell_10_hit1
execute at @s[tag=spell_10_hit2] run function skills:spells/spell_10_hit2
execute at @s if entity @p[predicate=skills:pain_reversal,distance=..9] run function skills:helmet/pain_reversal
execute at @s[type=!blaze,tag=mob_spawn_no_fire,tag=!mob_set_on_fire,predicate=mobs:is_on_fire] run function skills:misc/mob_set_on_fire
