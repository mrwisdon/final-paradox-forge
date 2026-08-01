#Arcane Resonance
execute at @s[predicate=skills:arcane_r,scores={mana=2..10}] run function skills:chest/arcane_r

#Sword Skills
execute at @s[predicate=skills:slash,scores={mana=20..}] run function skills:sword/slash
execute at @s[predicate=skills:virulent_plague,scores={mob_kills=0,mana=20..}] run function skills:sword/virulent
execute at @s[predicate=skills:magma_shield,scores={mana=20..}] run function skills:sword/magma_shield
execute at @s[predicate=skills:dragon_breath,scores={mana=20..}] run function skills:sword/dragon_breath
execute at @s[predicate=skills:eviscerate,scores={eviscerate_stage=1..,eviscerate_timer=1..39}] run function skills:sword/eviscerate_combo
execute at @s[predicate=skills:eviscerate,scores={eviscerate_timer=0,mana=20..}] run function skills:sword/eviscerate_1
execute at @s[predicate=skills:power_burst,scores={mana=20..}] run function skills:sword/power_burst
execute at @s[predicate=skills:astral_blades,scores={mana=20..}] run function skills:sword/astral_blades
execute at @s[predicate=skills:trinity_strikes,scores={trinity_cd=40..,mana=6..}] run function skills:sword/trinity_strikes
execute at @s[predicate=skills:slice_1] run function skills:sword/slice_1
execute at @s[predicate=skills:slice_2] run function skills:sword/slice_2
execute at @s[predicate=skills:slice_3] run function skills:sword/slice_3
execute at @s[predicate=skills:slice_4] run function skills:sword/slice_4
execute at @s[predicate=skills:slice_5] run function skills:sword/slice_5
execute at @s[predicate=skills:slice_6] run function skills:sword/slice_6

execute at @s[predicate=skills:vt_eviscerate,scores={vt_eviscerate_stage=1..,vt_eviscerate_timer=1..39}] run function skills:sword/vt_eviscerate_combo
execute at @s[predicate=skills:vt_eviscerate,scores={vt_eviscerate_timer=0,mana=20..}] run function skills:sword/vt_eviscerate_1

#Axe Skills
execute at @s[predicate=skills:cold_snap,scores={cold_snap_ready=1..}] run function skills:axe/cold_snap
execute as @s[predicate=skills:void_rage,scores={mana=3..,void_rage_time=1..}] run function skills:axe/void_rage_attack
execute at @s[predicate=skills:snowstorm,scores={snowstorm_ready=1..}] run function skills:axe/snowstorm

#crit strike
execute at @s[predicate=!skills:cannot_crit] run function skills:misc/meleehit_crit

#Other Skills
execute at @s[predicate=skills:bullrush,scores={bullrush_timer=1..}] run function skills:boots/bullrush
scoreboard players set @s[predicate=skills:duality] duality_cd 12
scoreboard players set @s[predicate=skills:assassination] assassination_cd 16
execute at @s[predicate=skills:toxicology] run function skills:offhand/toxicology
execute at @s[predicate=skills:annihilating_curse] run function skills:offhand/an_curse
execute as @s[predicate=skills:summon_fangs,scores={mana=2..}] run function skills:offhand/summon_fangs
execute at @s[predicate=skills:overcharge,scores={overcharge_time=1..}] run function skills:pickaxe/overcharge
execute at @s[predicate=skills:eternal_flame] run function skills:offhand/eternal_flame_fire

#Remove axe durability
execute as @s[nbt={SelectedItem:{id:"minecraft:carrot_on_a_stick"}},nbt=!{SelectedItem:{tag:{is_spell:1}}}] run function skills:custom_durability/hit

#Melee kill
execute at @s[advancements={skills:meleekill_x=true}] run function skills:misc/meleekill_x