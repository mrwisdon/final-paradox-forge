execute at @s run particle block crimson_fungus ~ ~ ~ 1.7 1 1.7 0.01 600 normal
execute at @s run particle sneeze ~ ~ ~ 1.7 1 1.7 0.05 150 normal
execute at @s run particle dust 0.345 0.690 0.118 1 ~ ~ ~ 1.3 0.8 1.3 0.01 600 normal
execute at @s run particle explosion ~ ~0.3 ~ 0.1 0.1 0.1 0.01 1 normal

execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 1 0.8 1
execute at @s run playsound block.beehive.enter master @a[distance=..18] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound entity.puffer_fish.sting master @a[distance=..18] ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..18] ~ ~ ~ 0.2 0.7 0.2

execute at @s run fill ~ ~ ~ ~ ~ ~ air replace crimson_fungus

kill @s