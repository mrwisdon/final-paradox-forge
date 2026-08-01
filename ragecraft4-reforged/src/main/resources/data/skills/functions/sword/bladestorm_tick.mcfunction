execute at @s run particle dust 0.38 0.027 0.086 1 ~ ~0.7 ~ 1.2 0.3 1.2 0.1 8 normal
execute at @s run particle sweep_attack ~ ~0.7 ~ 1.6 0.3 1.6 0.1 3 normal
execute at @s run playsound minecraft:entity.puffer_fish.blow_out master @a[distance=..16] ~ ~ ~ 0.03 0.5 0.03
execute at @s if score #rc4tick tick10 matches 2 run playsound minecraft:item.trident.throw master @a[distance=..16] ~ ~ ~ 0.2 0.6 0.2
execute at @s if score #rc4tick tick10 matches 7 run playsound minecraft:item.trident.throw master @a[distance=..16] ~ ~ ~ 0.15 0.8 0.15

scoreboard players remove @s bladestorm_time 1
scoreboard players set @s mana 0

scoreboard players add @s bladestorm_rot 1

execute at @s[scores={bladestorm_rot=1}] positioned ~-1.5 ~ ~ run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=1}] positioned ~1.5 ~ ~ run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=1}] positioned ~ ~ ~-1.5 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=1}] positioned ~ ~ ~1.5 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=2}] positioned ~-1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=2}] positioned ~1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=2}] positioned ~0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=2}] positioned ~-0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=3}] positioned ~-1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=3}] positioned ~1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=3}] positioned ~0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=3}] positioned ~-0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=4}] positioned ~-1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=4}] positioned ~1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=4}] positioned ~0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=4}] positioned ~-0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=5}] positioned ~-1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=5}] positioned ~1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=5}] positioned ~0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=5}] positioned ~-0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_2

execute at @s[scores={bladestorm_rot=6}] positioned ~-0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=6}] positioned ~0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=6}] positioned ~1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=6}] positioned ~-1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=7}] positioned ~-0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=7}] positioned ~0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=7}] positioned ~1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=7}] positioned ~-1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=8}] positioned ~-0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=8}] positioned ~0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=8}] positioned ~-1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=8}] positioned ~1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=9}] positioned ~-0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=9}] positioned ~0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=9}] positioned ~1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=9}] positioned ~-1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=10}] positioned ~ ~ ~-1.5 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=10}] positioned ~ ~ ~1.5 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=10}] positioned ~1.5 ~ ~ run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=10}] positioned ~-1.5 ~ ~ run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=11}] positioned ~0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=11}] positioned ~-0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=11}] positioned ~1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=11}] positioned ~-1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=12}] positioned ~0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=12}] positioned ~-0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=12}] positioned ~1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=12}] positioned ~-1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=13}] positioned ~0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=13}] positioned ~-0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=13}] positioned ~1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=13}] positioned ~-1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=14}] positioned ~0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=14}] positioned ~-0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=14}] positioned ~1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=14}] positioned ~-1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=15}] positioned ~1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=15}] positioned ~-1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=15}] positioned ~0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=15}] positioned ~-0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=16}] positioned ~1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=16}] positioned ~-1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=16}] positioned ~0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=16}] positioned ~-0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=17}] positioned ~1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=17}] positioned ~-1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=17}] positioned ~0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=17}] positioned ~-0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=18}] positioned ~1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=18}] positioned ~-1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=18}] positioned ~0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=18}] positioned ~-0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=19}] positioned ~1.5 ~ ~ run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=19}] positioned ~-1.5 ~ ~ run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=19}] positioned ~ ~ ~1.5 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=19}] positioned ~ ~ ~-1.5 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=20}] positioned ~1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=20}] positioned ~-1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=20}] positioned ~-0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=20}] positioned ~0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=21}] positioned ~1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=21}] positioned ~-1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=21}] positioned ~-0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=21}] positioned ~0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=22}] positioned ~1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=22}] positioned ~-1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=22}] positioned ~-0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=22}] positioned ~0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=23}] positioned ~1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=23}] positioned ~-1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=23}] positioned ~-0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=23}] positioned ~0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=24}] positioned ~0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=24}] positioned ~-0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=24}] positioned ~-1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=24}] positioned ~1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=25}] positioned ~0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=25}] positioned ~-0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=25}] positioned ~-1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=25}] positioned ~1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=26}] positioned ~0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=26}] positioned ~-0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=26}] positioned ~-1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=26}] positioned ~1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=27}] positioned ~0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=27}] positioned ~-0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=27}] positioned ~-1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=27}] positioned ~1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=28}] positioned ~ ~ ~1.5 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=28}] positioned ~ ~ ~-1.5 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=28}] positioned ~-1.5 ~ ~ run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=28}] positioned ~1.5 ~ ~ run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=29}] positioned ~-0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=29}] positioned ~0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=29}] positioned ~-1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=29}] positioned ~1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=30}] positioned ~-0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=30}] positioned ~0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=30}] positioned ~-1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=30}] positioned ~1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=31}] positioned ~-0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=31}] positioned ~0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=31}] positioned ~-1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=31}] positioned ~1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=32}] positioned ~-0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=32}] positioned ~0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=32}] positioned ~-1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=32}] positioned ~1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=33}] positioned ~-1.13 ~ ~0.98 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=33}] positioned ~1.13 ~ ~-0.98 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=33}] positioned ~0.98 ~ ~-1.13 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=33}] positioned ~0.98 ~ ~1.13 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=34}] positioned ~-1.26 ~ ~0.8 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=34}] positioned ~1.26 ~ ~-0.8 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=34}] positioned ~-0.8 ~ ~-1.26 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=34}] positioned ~0.8 ~ ~1.26 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=35}] positioned ~-1.38 ~ ~0.59 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=35}] positioned ~1.38 ~ ~-0.59 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=35}] positioned ~-0.59 ~ ~-1.38 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=35}] positioned ~0.59 ~ ~1.38 run function skills:sword/bladestorm_anim_4

execute at @s[scores={bladestorm_rot=36}] positioned ~-1.46 ~ ~0.33 run function skills:sword/bladestorm_anim_1
execute at @s[scores={bladestorm_rot=36}] positioned ~1.46 ~ ~-0.33 run function skills:sword/bladestorm_anim_2
execute at @s[scores={bladestorm_rot=36}] positioned ~-0.33 ~ ~-1.46 run function skills:sword/bladestorm_anim_3
execute at @s[scores={bladestorm_rot=36}] positioned ~0.33 ~ ~1.46 run function skills:sword/bladestorm_anim_4

scoreboard players set @s[scores={bladestorm_rot=36..}] bladestorm_rot 0

execute at @s[scores={bladestorm_time=0}] run function skills:sword/bladestorm_end