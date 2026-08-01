playsound entity.player.levelup master @s ~ ~ ~ 1 0.7 1
playsound entity.villager.trade master @s ~ ~ ~ 1 1 1

tellraw @a [{"translate":"wtem.empty"}]
tellraw @a [{"translate":"skills.functions.chest.agony_explain.2","color":"white","bold":true,"italic":false},{"translate":"skills.functions.chest.agony_explain.3","color":"white","bold":false,"italic":false}]
tellraw @a [{"translate":"skills.functions.chest.agony_explain.4","color":"white","bold":true,"italic":false},{"translate":"skills.functions.chest.agony_explain.5","color":"white","bold":false,"italic":false}]