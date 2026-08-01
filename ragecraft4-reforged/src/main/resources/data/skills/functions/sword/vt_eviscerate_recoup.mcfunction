execute at @s[scores={vt_eviscerate_stage=1}] run function skills:sword/vt_eviscerate_recoup_1
execute at @s[scores={vt_eviscerate_stage=2}] run function skills:sword/vt_eviscerate_recoup_2

scoreboard players set @s[scores={vt_eviscerate_timer=0,vt_eviscerate_stage=1..}] vt_eviscerate_stage 0