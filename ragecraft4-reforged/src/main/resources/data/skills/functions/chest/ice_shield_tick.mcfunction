effect give @s[scores={ice_shield_cd=1799}] absorption 92 1 true
scoreboard players add @s[scores={ice_shield_cd=..1799}] ice_shield_cd 1
scoreboard players set @s[scores={ice_shield_cd=1800..}] ice_shield_cd 0
execute as @s[tag=!ice_shield_broken,scores={absorption_amount=..0}] run function skills:chest/ice_shield_break
execute at @s[scores={absorption_amount=1..}] run particle snowflake ~ ~ ~ 2 0.5 2 0.01 1 normal
tag @s[scores={ice_shield_cd=0}] remove ice_shield_broken
