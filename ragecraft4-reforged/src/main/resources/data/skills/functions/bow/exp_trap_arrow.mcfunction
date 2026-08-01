execute at @s run particle flame ~ ~0.3 ~ 0.1 0.1 0.1 0.01 10 force
execute at @s run particle dust 0.929 0.765 0.224 1 ~ ~0.3 ~ 0.1 0.1 0.1 0.1 5 force

execute at @s[nbt={inGround:1b}] run function skills:bow/exp_trap_place