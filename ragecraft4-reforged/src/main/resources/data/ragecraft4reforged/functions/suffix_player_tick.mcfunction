tag @s[predicate=skills:no_mana_regen] add no_mana_regen
tag @s[predicate=skills:mana_frenzy] add no_mana_regen
execute at @s[predicate=skills:arcane_momentum] run function skills:chest/arcane_mom_tick
scoreboard players remove @s[scores={potion_cd=1..}] potion_cd 1
execute as @s[scores={carrot_stick_use=1..}] run function skills:misc/carrot_stick_use
execute at @s[scores={mob_kills=1..}] run function skills:misc/mob_kill
scoreboard players set @s[scores={meleehit=100000..}] meleehit 0
scoreboard players set @s[scores={meleehit=1..}] meleehit 100000
advancement revoke @s[tag=meleekill_x_got] only skills:meleekill_x
tag @s remove meleekill_x_got
tag @s[advancements={skills:meleekill_x=true}] add meleekill_x_got
scoreboard players add @s[scores={jump=1..}] jump_time 1
execute at @s[scores={jump=1..}] unless block ~ ~-0.01 ~ #minecraft:jump_through run function ragecraft4reforged:suffix_jump_end
scoreboard players set @s[scores={sprint_distance_2=1..,sprint_distance=0,jump=0}] sprint_distance_2 0
execute as @s[scores={sprint_distance=1..}] run function ragecraft4reforged:suffix_sprinting
execute as @s[scores={damage_taken=1..}] run function ragecraft4reforged:suffix_damage_taken
scoreboard players remove @s[scores={cold_snap_ready=1..}] cold_snap_ready 1
scoreboard players remove @s[scores={snowstorm_ready=1..}] snowstorm_ready 1
execute at @s[scores={void_rage_time=1..}] run function skills:axe/void_rage_tick
scoreboard players add @s[scores={trinity_cd=..40}] trinity_cd 1
execute as @s[scores={bowfired=1..}] run function skills:bow/bowfired
scoreboard players set @s[advancements={skills:bow_drawtime=false}] bow_drawtime 0
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_1
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_2
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_3
advancement revoke @s only skills:bow_drawtime
execute as @s[scores={spawner_mined=1..}] run function skills:pickaxe/spawner_mined
effect give @s[predicate=skills:tenacity] resistance 1 0 true
execute as @s[predicate=skills:guardian_angel,scores={health=..6,guardian_cd=6000..}] run function skills:chest/guardian_angel
execute as @s[predicate=skills:guardian_angel,scores={guardian_cd=..5999}] run function skills:chest/guardian_cd
execute as @s[predicate=skills:ice_shield] run function skills:chest/ice_shield_tick
effect give @s[predicate=skills:leap_slam] jump_boost 1 3 true
scoreboard players remove @s[scores={bullrush_timer=1..}] bullrush_timer 1
execute at @s[predicate=skills:noxious_trail] run function skills:boots/noxious_trail
execute at @s[predicate=skills:flamewalker] run function skills:boots/flamewalker
execute at @s[scores={flameborn_duration=1..}] run function skills:axe/flameborn_tick
execute as @s[scores={acid_spray_cd=1..}] run function skills:spells/spell_8_tick
execute as @s[scores={frost_ray_cd=1..}] run function skills:spells/spell_10_tick
execute as @s[predicate=skills:black_magic] run function skills:helmet/black_magic
execute as @s[predicate=skills:rapid_decay] run function skills:helmet/rapid_decay
execute at @s[scores={blight_orb_duration=1..}] run function skills:offhand/blight_orb_tick
execute at @s[scores={dark_orb_duration=1..}] run function skills:offhand/dark_orb_tick
execute as @s[predicate=skills:shadow_spikes,scores={health=..10,shadow_spikes_cd=1200..}] run function skills:offhand/shadow_spikes
execute as @s[predicate=skills:shadow_spikes,scores={shadow_spikes_cd=..1199}] run function skills:offhand/shadow_spikes_cd
execute as @s[predicate=skills:supercharged,scores={mana=10..}] run function skills:offhand/supercharged
scoreboard players remove @s[scores={eviscerate_timer=1..}] eviscerate_timer 1
execute at @s[scores={eviscerate_timer=0,eviscerate_stage=1..}] run function skills:sword/eviscerate_recoup
scoreboard players remove @s[scores={vt_eviscerate_timer=1..}] vt_eviscerate_timer 1
execute at @s[scores={vt_eviscerate_timer=0,vt_eviscerate_stage=1..}] run function skills:sword/vt_eviscerate_recoup
execute at @s[predicate=skills:frostbite] run function skills:sword/frostbite_tick
execute at @s[predicate=skills:overcharge,scores={overcharge_time=1..}] run particle electric_spark ^-0.5 ^1 ^1 0.5 0.5 0.5 0.01 2 normal
execute as @s[scores={bladestorm_time=1..}] run function skills:sword/bladestorm_tick
execute at @s[scores={arcanist_timer=1..}] run function skills:bow/arcanist_player
execute at @s[scores={arcane_mom=1..}] run function skills:chest/arcane_mom_buffed
execute as @s[predicate=skills:last_stand,scores={health=..12}] run function skills:offhand/last_stand
execute as @s[predicate=skills:elrichs_curse,scores={health=..10}] run function skills:helmet/elrichs_curse
scoreboard players remove @s[scores={delayed_att=1..}] delayed_att 1
execute if score #r4r tick10 matches 10 run function ragecraft4reforged:suffix_player_tick10
execute if score #r4r tick20 matches 20 run function ragecraft4reforged:suffix_player_tick20
execute if score #r4r tick40 matches 40 run function ragecraft4reforged:suffix_player_tick40
execute if score #r4r tick60 matches 60 run function ragecraft4reforged:suffix_player_tick60
tag @s remove no_mana_regen
