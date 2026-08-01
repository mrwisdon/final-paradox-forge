execute at @s run setblock ~ ~ ~ crimson_fungus replace
execute at @s run particle glow_squid_ink ~ ~ ~ 0.5 0.3 0.5 0.01 30 normal
execute at @s run playsound block.beehive.exit master @a[distance=..15] ~ ~ ~ 0.6 0.9 0.6

tag @s add fungus_placed