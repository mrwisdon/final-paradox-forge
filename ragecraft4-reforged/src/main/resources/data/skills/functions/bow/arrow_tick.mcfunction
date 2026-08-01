#affects all arrows with arrow_tick tag

execute at @s[tag=puncture] run function skills:bow/puncture_tick
execute as @s[scores={marksman_timer=1..14}] run function skills:bow/marksman_tick
execute at @s[tag=arrow_arcanist] run particle dust 0.8 0.6 0.827 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.2 8 force

execute at @s[tag=arctic_arrow] run particle dust 0.729 0.82 0.941 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.2 8 force
execute at @s[tag=chilling_arrow] run particle dust 0.729 0.925 0.941 1 ~ ~0.2 ~ 0.1 0.1 0.1 0.2 8 force
execute at @s[tag=cursed_arrow] run particle dust 0.051 0.024 0.059 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.2 8 force
execute at @s[tag=arcane_arrow] run particle enchanted_hit ~ ~0.2 ~ 0.1 0.1 0.1 0.1 5 force
execute at @s[tag=arcane_suprem] run particle dust 0.333 0.373 0.886 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.2 8 force
execute at @s[tag=arcane_suprem] run particle enchanted_hit ~ ~0.2 ~ 0.1 0.1 0.1 0.1 5 force
execute at @s[tag=zombiemorph] run particle dust 0.471 0.8 0.8 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.1 8 force
execute as @s[tag=unstable_charge] run function skills:bow/unstable_charge_tick
execute as @s[tag=healing_crystal] run function skills:bow/healing_crystal_arrow
execute as @s[tag=power_crystal] run function skills:bow/power_crystal_arrow
execute as @s[tag=webbed_arrow] run function skills:bow/webbed_arrow_tick
execute as @s[tag=arcane_arrow] run function skills:bow/arcane_arrow_tick
execute as @s[tag=explosive_trap] run function skills:bow/exp_trap_arrow
execute as @s[tag=charged_arrow] run function skills:bow/charged_arrow_tick
execute as @s[tag=noxious_arrow] run function skills:bow/noxious_arrow_tick
execute at @s[tag=sharpshot] run particle crit ~ ~0.2 ~ 0.1 0.1 0.1 0.1 25 force

execute at @s[tag=void_powers] run particle dust 0.459 0.224 0.522 1 ~ ~0.2 ~ 0.2 0.2 0.2 0.1 8 force
execute at @s[tag=void_powers] run particle end_rod ~ ~0.2 ~ 0.0 0.0 0.0 0.01 5 force

execute as @s[tag=vt_healing_crystal] run function skills:bow/vt_healing_crystal_arrow

tag @s[nbt={inGround:1b},tag=!noxious_arrow] remove arrow_tick

scoreboard players add @s eviscerate_timer 1
kill @s[scores={eviscerate_timer=2400..}]