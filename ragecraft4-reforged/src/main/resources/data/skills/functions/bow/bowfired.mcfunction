execute at @s[predicate=skills:volley_1] run scoreboard players set @e[type=minecraft:arrow,tag=!volley_done,nbt={pickup:1b,inGround:0b,crit:1b},distance=..3] volley_1_delay 1
execute at @s[predicate=skills:volley_2] run scoreboard players set @e[type=minecraft:arrow,tag=!volley_done,nbt={pickup:1b,inGround:0b,crit:1b},distance=..3] volley_2_delay 1

execute at @s[predicate=skills:a_marksmanship] run function skills:bow/marksman

execute at @s[predicate=skills:trueshot] run function skills:bow/trueshot
execute at @s[predicate=skills:sharpshot_1,tag=sharpshot_1] run function skills:bow/sharpshot_1
execute at @s[predicate=skills:sharpshot_2,tag=sharpshot_2] run function skills:bow/sharpshot_2
execute at @s[predicate=skills:sharpshot_3,tag=sharpshot_3] run function skills:bow/sharpshot_3
execute at @s[predicate=skills:sharpshot_4,tag=sharpshot_4] run function skills:bow/sharpshot_3

execute at @s if score #rc4tick spec_archer matches 1.. run function skills:bow/spec_archer

execute at @s[predicate=skills:healing_crystal] run function skills:bow/healing_crystal
execute at @s[predicate=skills:power_crystal] run function skills:bow/power_crystal
execute at @s[predicate=skills:chilling_arrow] run function skills:bow/chilling_arrow
execute at @s[gamemode=survival,predicate=skills:webbed_arrow] run function skills:bow/webbed_arrow
execute at @s[predicate=skills:arcane_arrow,scores={mana=20..}] run function skills:bow/arcane_arrow
execute at @s[predicate=skills:cursed_arrow] run function skills:bow/cursed_arrow
execute at @s[predicate=skills:charged_arrow] run function skills:bow/charged_arrow
execute at @s[predicate=skills:unstable_charge] run function skills:bow/unstable_charge
execute at @s[predicate=skills:explosive_trap] run function skills:bow/exp_trap
execute at @s[predicate=skills:amplified] run function skills:bow/amplified
execute at @s[predicate=skills:dreadfire] run function skills:bow/dreadfire
execute at @s[predicate=skills:noxious_arrow] run function skills:bow/noxious_arrow
execute at @s[predicate=skills:arctic_arrow] run function skills:bow/arctic_arrow
execute at @s[predicate=skills:zombiemorph,scores={mana=10..}] run function skills:bow/zombiemorph
execute at @s[predicate=skills:void_blast] run function skills:bow/void_blast
execute at @s[predicate=skills:arcane_suprem,scores={arcane_suprem=1..8}] run function skills:bow/arcane_suprem

execute at @s[predicate=skills:vt_healing_crystal] run function skills:bow/vt_healing_crystal

tag @s remove sharpshot_1
tag @s remove sharpshot_2
tag @s remove sharpshot_3
tag @s remove sharpshot_4
scoreboard players set @s bowfired 0

execute at @s[predicate=skills:puncture] run function skills:bow/puncture
execute at @s[predicate=skills:a_arcanist,scores={arcanist_timer=1..200}] run function skills:bow/arcanist

#added this recently, make sure it does not break anything!
execute at @s run tag @e[type=minecraft:arrow,tag=!arrow_done,nbt={pickup:1b,inGround:0b},distance=..3] add arrow_done