# B8 H2 金块模块动画 - ChatGPT 交接项

> 记录日期：2026-08-02

写给接手方（ChatGPT / 后续开发者）。这是**已记录的遗留问题**，不是已完成
声明：H2 坠落金块模块的投掷/下落动画目前不够理想，视觉优化留给 ChatGPT
处理。接手方必须先完整阅读工作区 `AGENTS.md` 和
`recreate-minecraft-map-items` 技能的 `SKILL.md`，并遵守「问题默认先做
只读诊断、证据优先、实现后强制自审、构建成功不代表视觉验证通过」的流程。

## 1. 问题描述（用户原话）

> 金块的投掷动画不够理想，这部分留给 chatgpt 做。

现状是功能可用、流程闭环（生成→下落→旋转→击碎/落地爆炸→开盾），但金块
模块的视觉/动画表现未达理想标准，需要对照原版优化。

## 2. 当前实现（2026-08-02，M4-H2）

文件（都在 `src/main/java/io/github/finalparadox/`）：

- `entity/B8H2ModuleEntity.java`：轻量实体。每 tick 下落 `0.036` 格、
  旋转 `+3` 度、在 +2 高度发金色 `dust` 粒子；落地阈值 = 锚点 y - 1.5。
- `client/B8H2ModuleRenderer.java`：用 `BlockRenderDispatcher.renderSingleBlock`
  绘制金块，方块中心放在实体原点 +1.0（占 1.0..2.0），按同步 yaw 旋转。
- `entity/B8EncounterController.java`：H2 状态机（候选点、每 tick 生成、
  落地爆炸 5 点载具伤害、子弹 1.5 格击碎、清空后 2 秒开盾、切阶段清理）。

已记录的近似（都有待优化）：

1. 原版 `Glowing:1b` 描边效果未复刻，只用了每 tick 尘埃粒子模拟"发光"。
2. 金块方块渲染中心在 +1.0，不是原版盔甲架头部物品（head bone ≈ +1.5）
   的精确位置，落点/命中采样（+1.4/+1.7）与视觉可能错位。
3. 下落是直线匀速（0.036/tick），没有原版召唤瞬间的起手/出现感，整体观感
   被用户评价为"投掷动画不够理想"。
4. 落地爆炸的墨鱼墨环是 60 个固定环位粒子，与原版 `boom.mcfunction` 的
   64 个精确偏移（3.0 半径、0.5 速度）不完全一致。

## 3. 权威证据（优化时对照）

- 生成：`datapacks\luisb1202-functions\data\luisb1202\functions\bossfight\b8\h2\gen.mcfunction`
  （`summon armor_stand -3828 84.5 1412 {Team:yellow,Glowing:1b,ArmorItems:[...gold_block...],
  Small:0b,Marker:1b,...}`，随后按概率 `tp ~ ~N ~` 抬升 0/2/4/5/6）。
- 每 tick：`h2/run.mcfunction`（`tp @s ~ ~-0.036 ~ ~3 ~` +
  `particle dust 1 0.933 0 1.5 ~ ~2 ~ 0.2 0.2 0.2 0 1 force`）。
- 击碎：`h2/romper.mcfunction`（+1.4 采样、explosion/cloud/gold_block item
  粒子、`entity.ender_eye.death`）。
- 落地：`h2/boom.mcfunction`（+1.7 采样、64 个 squid_ink 精确偏移、
  explosion、`entity.generic.explode`，随后 `danar_montura` 5 点伤害）。
- 原版视觉基准：原版地图世界内（`.codex-tmp/fp-world-1.1.15/...`）实际运行
  H2 时的金块外观、下落轨迹、发光效果截图/录屏对比。

## 4. 接手要求

- 先只读诊断：对照原版证据列出当前实现与源视觉的差异点、根因、影响文件与
  拟议修正，得到用户确认后再改。
- 优化时优先复用现有实体/渲染结构，不要退回用大量联网盔甲架照搬。
- 完成后必须做强制自审，并注明哪些项目仍需游戏内观察；不得仅凭"构建成功"
  宣称视觉通过。
