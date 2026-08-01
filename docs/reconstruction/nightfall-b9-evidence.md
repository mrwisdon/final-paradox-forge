# 夜幕技能复现证据表

目标版本：Final Paradox v1.1.15，Forge 1.20.1。

## 原版物品语义

原版通关奖励位于 `items/espada_conquistador/item.mcfunction`。它是
`iron_nugget`（`CustomModelData:1202009`），中文名称为“夜幕，塔尔·克罗的世界末日”。
原版 Lore 明确说明它已经失去全部力量、不能再作为武器使用。因此，本模组中的主动技能
不是奖励物品原有功能，而是把Maraw‘Thar B9 战斗中的攻击模型重新绑定到可用剑上的扩展。

## 右键：Maraw‘Thar三连斩

主要来源：

- `bossfight/b9/h1/combo1/run.mcfunction`
- `bossfight/b9/h1/combo1/particulas/run.mcfunction`
- `bossfight/b9/h1/combo1/particulas/golpe_1..3/pack1..3.mcfunction`
- `bossfight/b9/h1/combo1/particulas/trail_normal.mcfunction`
- `bossfight/b9/parry/hit/ini_espada.mcfunction`
- `bossfight/b9/parry/hit/dano.mcfunction`

已复现：

- 总流程 38 tick。
- 第 8、18、26 tick 沿锁定朝向各前进 1 格。
- 第 9、19、29 tick 使用原版三叉戟投掷音效参数。
- 第 12–14、20–22、30–32 tick 绘制三次攻击的九个原始粒子帧。
- 九个帧共 354 个局部定位/旋转样本；由
  `tools/reconstruction/generate_nightfall_slash_frames.ps1` 直接从原版函数生成。
- 每个样本沿 0.9–2.9 格绘制黑红尘埃，并在 3 格末端绘制 end rod。
- 命中球心与半径沿用原版：局部前方 2 格、下移 1 格，半径 2.5。
- 普通目标伤害量对应原版瞬间伤害 IV：48；移除抗性并附加 3 秒缓慢 II。
- 按用户要求增加物品版追踪：技能持续期间每 tick 重新选择 30 格内最近的有效敌人，
  三次突进和三组剑弧随目标更新朝向；无目标时保留施法朝向。

## 潜行右键：原始黑火剑气

主要来源：

- `bossfight/b9/h1/combo5/run.mcfunction`
- `bossfight/b9/h1/combo5/trail/animacion_warn_azul.mcfunction`
- `bossfight/b9/h1/combo5/trail/run_animacion_warn_azul.mcfunction`
- `bossfight/b9/h1/combo5/trail/gen.mcfunction`
- `bossfight/b9/h1/combo5/trail/paso.mcfunction`
- `bossfight/b9/h1/combo5/trail/paso_trozo.mcfunction`
- `bossfight/b9/h1/combo5/trail/particulas_azules.mcfunction`
- `bossfight/b9/h1/combo5/trail/particulas.mcfunction`

已复现：

- 第 2 tick 创建原版 62 点、半径 3 的球形预警，随机删除 40 点。
- 13 个可见 tick 内，每 tick 按原版执行两次 0.15 格朝使用者眼睛的运动和两次粒子绘制，
  包括越过目标点后的微小往返。
- 第 23 tick 释放并播放三组原版音效；第 25 tick 使用者前进 0.5 格。
- 轨迹从使用者后方 4 格、上方 0.7 格开始。
- 每 tick 六个 0.35 格子步，即 2.1 格/t；持续 15 tick。
- 每个子步绘制原版八点青色剑弧；每 tick 绘制原版 24 点、半径 2.5 的 end rod 环。
- 命中半径 3，普通目标伤害量对应原版瞬间伤害 IV：48。
- 对单个目标沿用原版 4 tick 命中冷却；目标若持续跟随剑气，冷却后可再次命中。

## 疾跑右键：征服者激光

主要来源：

- `bossfight/b9/h9/ini_warn.mcfunction`
- `bossfight/b9/h9/anclar_vision.mcfunction`
- `bossfight/b9/h9/run_warn.mcfunction`
- `bossfight/b9/h9/ini_laser.mcfunction`
- `bossfight/b9/h9/ini2.mcfunction`
- `bossfight/b9/h9/gen.mcfunction`
- `bossfight/b9/h9/pos_laser.mcfunction`
- `bossfight/b9/h9/run.mcfunction`
- `bossfight/b9/h9/hit.mcfunction`
- `bossfight/b9/h9/end.mcfunction`
- `bossfight/b9/boss/inmortal/animacion_cast/run_left.mcfunction`
- `bossfight/b9/boss/inmortal/animacion_cast/run_right.mcfunction`

已复现：

- 水平追踪 50 tick，左右各 0.5 格绘制 23 组、间距 0.9 格的灵魂火导轨。
- 锁定时首次预警，12 tick 后第二次预警，再过 12 tick 发射。
- 光束由 38 个方块段组成，段间距 0.6 格，总长度 22.8 格。
- 原版 1–8 分段相位、白色脉冲、近端 beacon 声与 flash 触发规则。
- 光束持续 80 tick：先保持 5 tick，再按目标所在侧旋转 70 tick，
  每 tick 2.2 度，最后保持 5 tick。
- 判定逐段使用半径 1.1，并对目标脚点的 0、-2、-4、-6、-8 高度采样。
- 每个目标每次释放只命中一次；伤害量对应原版瞬间伤害 V：96。
- 结束时为 38 段逐段生成大烟雾并播放熄火声。

## 玩家化适配

- 原版选择一个玩家作为 Boss 目标；物品版选择使用者视线方向内的有效敌对生物，
  找不到目标时使用当前水平朝向。
- 原版只伤害非旁观玩家；物品版允许伤害非友军生物和模组 Boss，并排除使用者、
  友军、创造/旁观玩家与盔甲架。
- 原版 Boss 通过盔甲架与姿态函数表演；物品版锁定玩家位置、触发挥手动画，
  并用同步的自定义实体绘制 38 段激光。
- 三连斩的第三人称剑动画直接采用
  `bossfight/b9/h1/combo1/gen_espada_frame.mcfunction` 的 29 个剑核心局部位置和
  Rotation。原版各帧的 `b4_espada_cd` 保持量合计正好 38 tick；视觉帧与现有
  12–14、20–22、30–32 tick 粒子和命中帧对齐。攻击期间普通手持模型隐藏，
  七部件复合剑改在玩家局部世界坐标中逐帧插值绘制。所有帧统一补回普通尺寸
  盔甲架头盔渲染自带的 1.5 格模型基准高度。
- 玩家身体骨骼没有照搬原版盔甲架的 Head/Body/Arm/Leg Pose；本次只精确迁移
  用户指定的武器模型运动，第三人称身体姿态和最终屏幕观感仍需实机核对。
- 普通三连斩的“每 tick 追踪 30 格内最近敌人”是本次用户指定的物品行为，
  不属于原版固定朝向的 `combo1` 函数。
- 非 `MONSTER` 分类的模组敌人可加入
  `finalparadox:nightfall_targets` 实体类型标签参与自动追踪。
- 三连斩/黑火剑气/征服者激光当前冷却为 4/6/12 秒；由夜幕之剑连段伤害直接完成的每次击杀会返还 1 秒当前剩余冷却，多杀可叠加。原版 Boss 函数自身没有这些玩家冷却，这仍是玩家物品化后的防重入与击杀奖励层。
- 玩家化操作不再依赖潜行、疾跑或滞空状态：默认 `Z/X/C/V/B` 分别直接选择三连斩、黑火剑气、征服者激光、Maraw‘Thar激光连段和链刃天坠，且可在控制设置中重绑。右键保留为三连斩兼容入口。按键包只携带技能编号，服务端会重新验证主手或副手确实持有夜幕之剑。
