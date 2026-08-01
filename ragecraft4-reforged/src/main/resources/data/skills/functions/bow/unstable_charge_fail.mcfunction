effect give @a[tag=unstable_charge] instant_damage 1 1 true
scoreboard players remove @a[tag=unstable_charge] mana 20
playsound entity.puffer_fish.sting master @a[tag=unstable_charge] ~ ~ ~ 0.7 1.2 0.7
tag @a remove unstable_charge
kill @s