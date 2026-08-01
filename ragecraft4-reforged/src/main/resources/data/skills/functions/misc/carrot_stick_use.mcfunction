execute at @s[predicate=skills:axe_throw,scores={mana=20..}] run function skills:axe/axe_throw
execute at @s[predicate=skills:cold_snap,scores={mana=20..}] run function skills:axe/cold_snap_ready
execute at @s[predicate=skills:shadow_grasp,scores={mana=10..}] run function skills:axe/shadow_grasp
execute at @s[predicate=skills:combustion,scores={mana=20..}] run function skills:axe/combustion
execute at @s[predicate=skills:void_rage,scores={void_rage_time=1..}] run function skills:axe/void_rage_fail
execute at @s[predicate=skills:void_rage,scores={void_rage_time=0}] run function skills:axe/void_rage_ready
execute at @s[predicate=skills:lightning_warp,scores={mana=20..}] run function skills:axe/l_warp
execute at @s[predicate=skills:snowstorm,scores={mana=20..}] run function skills:axe/snowstorm_ready
execute at @s[predicate=skills:thunder_slam,scores={mana=10..}] run function skills:axe/thunder_slam

execute at @s[predicate=skills:vt_axe_throw,scores={mana=20..}] run function skills:axe/vt_axe_throw

execute at @s[predicate=skills:is_spell,scores={spell_cd=20..}] run function skills:spells/spell_cast

effect give @s[predicate=skills:is_spell,predicate=skills:tailwind] speed 15 1 true

scoreboard players set @s carrot_stick_use 0