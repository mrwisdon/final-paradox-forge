execute at @s[nbt={ActiveEffects:[{Id:2}]}] run effect give @p[predicate=skills:pain_reversal,distance=..12] speed 1 0 true
execute at @s[predicate=skills:has_vulnerability] run effect give @p[predicate=skills:pain_reversal,distance=..12] resistance 1 0 true
execute at @s[nbt={ActiveEffects:[{Id:18}]}] run effect give @p[predicate=skills:pain_reversal,distance=..12] strength 1 0 true
execute at @s[nbt={ActiveEffects:[{Id:20}]}] run effect give @p[predicate=skills:pain_reversal,distance=..12,nbt=!{ActiveEffects:[{Id:10}]}] regeneration 3 0 true
execute at @s[predicate=skills:has_flammability] run effect give @p[predicate=skills:pain_reversal,distance=..12] fire_resistance 1 0 true
