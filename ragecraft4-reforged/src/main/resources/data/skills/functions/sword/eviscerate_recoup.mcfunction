execute at @s[scores={eviscerate_stage=1}] run function skills:sword/eviscerate_recoup_1
execute at @s[scores={eviscerate_stage=2}] run function skills:sword/eviscerate_recoup_2

scoreboard players set @s[scores={eviscerate_timer=0,eviscerate_stage=1..}] eviscerate_stage 0