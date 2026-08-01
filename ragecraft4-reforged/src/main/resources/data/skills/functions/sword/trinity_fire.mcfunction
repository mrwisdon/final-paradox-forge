execute at @s run playsound minecraft:entity.illusioner.cast_spell master @s ~ ~ ~ 0.6 1.2 0.6

summon snowball ~ ~5 ~ {Tags:["trinity_fire"],Motion:[0.0,0.0,0.0],Passengers:[{id:"minecraft:armor_stand",NoGravity:1b,Silent:1b,Invulnerable:1b,Small:1b,Marker:1b,Invisible:1b,Tags:["marker_tick","trinity_fire"]}],Item:{id:"minecraft:snowball",Count:1b,tag:{CustomModelData:11}}}

item modify entity @s[predicate=skills:model_1] weapon.mainhand skills:model_5
item modify entity @s[predicate=skills:model_2] weapon.mainhand skills:model_5
item modify entity @s[predicate=skills:model_11] weapon.mainhand skills:model_15
item modify entity @s[predicate=skills:model_12] weapon.mainhand skills:model_15
item modify entity @s[predicate=skills:model_21] weapon.mainhand skills:model_25
item modify entity @s[predicate=skills:model_22] weapon.mainhand skills:model_25
item modify entity @s[predicate=skills:model_31] weapon.mainhand skills:model_35
item modify entity @s[predicate=skills:model_32] weapon.mainhand skills:model_35
item modify entity @s[predicate=skills:model_41] weapon.mainhand skills:model_45
item modify entity @s[predicate=skills:model_42] weapon.mainhand skills:model_45
item modify entity @s[predicate=skills:model_51] weapon.mainhand skills:model_55
item modify entity @s[predicate=skills:model_52] weapon.mainhand skills:model_55