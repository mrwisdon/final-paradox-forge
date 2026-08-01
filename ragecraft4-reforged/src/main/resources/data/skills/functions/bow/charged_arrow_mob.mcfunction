scoreboard players set @s[tag=!charged_arrow_hit] charged_arrow_time 6
scoreboard players remove @s[scores={charged_arrow_time=1..}] charged_arrow_time 1
execute at @s[scores={charged_arrow_time=0}] run function skills:bow/charged_arrow_hit2