# B5 双 Boss 战（Koyomi x Gariheuz）交接文档

> 2026-08-01 更新：本文保留为 DeepSeek 交接原文，其中若干数值和“剩余问题”已经过时。
> 当前结论与修正状态以 `b5-parity-audit-2026-08-01.md` 为准。

写给接手方（GPT / 后续开发者）。本文档总结当前实现状态、已解决问题、
剩余四个问题的已知细节与证据来源，以及接手时必须遵守的规则。

## 1. 项目与规则

- 工作区：`C:\Users\19818\Desktop\newmod`
- 主模组：`final-paradox-forge`（Forge 1.20.1，`minecraft_version=1.20.1`、
  `forge_version=47.4.20`，官方映射，Java 17）
- 子模组：`final-paradox-forge/ragecraft4-reforged`（Ragecraft IV 内容）
- 权威源：`C:\Users\19818\Desktop\newmod\.codex-tmp\fp-world-1.1.15\Final_Paradox_v1.1.15`
  （原版地图解包，DataVersion 2586，1.16.5）
- B5 权威数据包：`...\datapacks\luisb1202-functions\data\luisb1202\functions\bossfight\b5\**`
- 中文翻译（逐字复制用）：`C:\Users\19818\Desktop\newmod\_tmp_fp_lang\assets\vm\lang\zh_cn.json`
- 英文翻译：`C:\Users\19818\Desktop\newmod\.map-inspection\resources\assets\minecraft\lang\en_us.json`
- 竞技场锚点约定：玩家出生 `-1107 49 1426`，floor anchor `-1107 48 1426`；
  代码里所有坐标都是相对 anchor 的偏移（rel）。
- 强制规则：接手方必须先完整阅读
  `C:\Users\19818\.codex\skills\recreate-minecraft-map-items\SKILL.md`，
  并遵守「问题默认先只读诊断、证据优先、实现后强制自审」的流程。

## 2. 当前架构（2026-08-01 重写后）

文件（都在 `src/main/java/io/github/finalparadox/`）：

- `entity/B5EncounterData.java`：per-dimension SavedData（`finalparadox_b5_encounter`），
  持久化 fase、计数器、护盾归属、锚点、Boss UUID、中场/计时器状态。
- `entity/B5EncounterManager.java`：服务端 tick 驱动入口，从
  `arena/ArenaDeploymentEvents.onServerTick` 调用，Boss 死亡/重载不会卡死战斗。
- `entity/B5EncounterController.java`：战斗逻辑主体（约 110KB），按原版命令链移植
  fase 1→inter1→fase 2→inter2→fase 3→fase 4、h1-h7 全部攻击、run 循环、
  胜负链、对话、音乐。
- `entity/B5Particles.java`：粒子工具（当前是**简化近似**，见问题 1）。
- `entity/KoyomiBossEntity.java` / `GariBossEntity.java`：外观/属性/Bossbar/命中上报，
  Gari 用 MAX_HEALTH=1000 等价复刻原版 `Health:1000f`（1.20.1 setHealth 会钳到上限）。
- `arena/ArenaCommands.java`：`/arena deploy b5`、`/arena status`、
  `/arena reset b5`、`/arena start b5`（当前战斗入口）。
- 资源：`assets/finalparadox/lang/{zh_cn,en_us}.json` 已并入 530 个原版 B5 翻译键；
  `sounds.json` + `sounds/music/koyomi_*.ogg`（音乐）。

构建 / 同步：

```powershell
cd C:\Users\19818\Desktop\newmod\final-paradox-forge
.\gradlew.bat build
```

JAR 输出：`build\libs\finalparadox-0.1.0.jar` 与
`ragecraft4-reforged\build\libs\ragecraft4reforged-0.1.0.jar`。
客户端测试目录：`C:\Users\19818\AppData\Roaming\.minecraft\versions\1.20.1-Forge_47.4.20\mods`。
同步后必须做源/目标 SHA-256 校验。

## 3. 已解决的问题（本次会话确认）

1. **战败播放**：全灭后 `defeat` 触发——标题（`b1.derrota.1/2`）、wither 死亡音、
   dia15 对话三连、5 秒后 `respawn`（重置竞技场、召唤 idle 双 Boss、冒险模式、抗性）。
2. **音乐播放**：Koyomi 四段音乐（main intro/loop、inter intro/loop、inter final）
   + `abatir_jefe`，record 声道、原版延迟（122.55s / 112.34s / 53.23s / 51.06s / 71.49s）。
3. **竞技场建筑**：`/arena deploy b5` 部署 8 块 tile（85×62×57，已验证 300,390 格精确），
   `placeBarriers` 11 片 barrier、fleecy 箱（青色玻璃）开合、reset 清理。
4. **倒计时崩溃**：`tickDialogueQueue` 迭代中自改列表导致 ConcurrentModificationException，
   已改为「先取出到期条目再执行回调」，已修复并同步（SHA-256
   `6FBB8A26…D755C63`）。修复后游戏可越过倒计时，但**尚未做完整游戏内验证**。

## 4. 剩余问题 1：所有粒子效果都需要重写

现状：`B5Particles.java` 里全是**简化近似**，与原版逐条 `particle` 命令不一致。
原版粒子几何都是预计算的三角函数坐标（每个函数几百行），要按原文件逐条移植。

已知需要重写的清单（源文件在 `bossfight/b5/**`）：

| 系统 | 原版 | 现状 |
|---|---|---|
| h1 护盾环 | `h1/particles/index` + `preindex/postindex` + `s1..s5`：16 帧扩展动画、24 点竖直 end_rod 环（随 yaw 旋转、向下拖尾）、帧计数 `b5_h1_escudo_t` | `shieldRing()` 单环近似 |
| h1 反弹 | `h1/rebotar.mcfunction`：48 点 crit 扇 + explosion + end_rod + 3 音效 | `critFan()` 近似（少音效） |
| h1 信任 | `h1/confianza/colorines`：头顶名字 9 色轮换 + crit 粒子 + `run_colorines` 循环 | 只有 crit 粒子，无名字/颜色轮换 |
| h2 炸弹 | `h2/particles/index` + `s1..s4`（12 帧环）、`particulas_golpe`（end_rod 20+24 点）、`particulas_boom`（约 11KB：双 explosion + 64 点 campfire 烟环 + end_gateway/portal 音效） | `h2Golpe/h2Boom` 近似 |
| h3 爆炸 | `h3/boom_vsfx.mcfunction`：explosion + cloud + campfire 烟 + dark_prismarine/andesite 64 点扇形碎片 | `h3Boom` 近似（30/48 点） |
| h4 追迹 | `h4/do_trail`（紫/绿 dust 按玩家颜色 + squid_ink）、`h4/particulas_tp`（24 方向 squid_ink 爆 + fox.teleport 音） | 近似（dust 全广播、squid 1 点） |
| h4 反应 | `h4/fase_ilusion/reaccion.mcfunction`：48 点 totem 扇 + 音效 | `totemFan()` 近似 |
| h4 毒池 | `fase_ilusion/run` + `pozas/run`：dust 0.251/1/0.063 绿、0.78/0.063/1 紫，按队伍过滤 | 近似、全广播 |
| h5 弹道 | `h5/instance_run` + `h5/particle/1..13`（13 帧翅膀拖尾，`^ ^ ^-0.8` 旋转系） | `h5Wing()` 13 帧近似 |
| h5 命中 | `h5/boom.mcfunction`：48 点 crit + explosion + end_rod 14 + totem 14 + 双 dust 球 + 3 音效 | 近似 |

原则：按技能要求「把原地图的粒子逐条转成 Java 调用」，先做 h1/h2/h3/h5 这四个
玩家每局必看的，再补 h4。注意 1.20.1 `sendParticles` 的 count/dx/dy/dz/speed
语义与 1.16 粒子命令一致：`count=0` 表示单粒子带速度，Java 用 `count=1` +
速度参数模拟；每 tick 循环由控制器驱动（原版用 `schedule ... 1t`）。

## 5. 剩余问题 2：许多 Boss 技能没有合法触发

含义：技能触发条件没有严格按原版「计分板阈值 + 护盾归属」来实现或验证。
当前控制器里计数器逻辑在，但**没有游戏内确认过每个技能真的会按原版节奏触发**。
接手方需要逐条核对并补游戏内验证：

原版触发条件（`fase/*/run.mcfunction`）：

| 技能 | 触发 | 原版计数起点 |
|---|---|---|
| h5 三连发（Koyomi 投枪） | `fase/1/run`：Koyomi 持盾时 `h5+1`，`h5>=35` 触发 | fase1 初始 25；fase2/3 初始 14 |
| h3 三叉戟雨 | `fase/2/run`、`fase/3/run`：Koyomi 持盾时 `h3+1`，`h3>=35` | fase1 初始 5；fase2/3 初始 28 |
| h7 龙枪连发 | Gari 持盾时 `h7+1`，`h7>=14` | 0 |
| h2 水炸弹 | `h1/switch` 把盾交给 Gari 后 5 秒（inter 期间不排） | - |
| h4 幻象 | 中场时间线：inter1 的 46.5s / inter2 的 15s 起追迹 | - |

关键易错点（已按原版修正，需游戏内确认）：

- 阶段 1 开局护盾在 **Gari**（`summon` 后 `h1/switch` 的结果），`b5_shield_change=1`，
  `h5=25`——所以 fase1 一开始 h5 不增长，直到 Koyomi 血量 ≤68% 触发第一次换盾。
- 换盾条件：fase1 `boss_vida<=68 && b5_shield_change==1`；fase2
  `boss_vida2<=37 && change==0`；fase3 `boss_vida2<=6 && change==0`。
- 进入 fase2/3 时 `h5=14, h3=28`，Gari 血量被设为绝对 680/370。
- 复活：`summon_dead_koyo/gari` = 移除+固定点重召唤、0.02 HP、无敌、
  无限缓慢/虚弱；Koyomi 复活后无盾；Gari 复活后盾给 Gari。

## 6. 剩余问题 3：中场时期的召唤没有正常实现

含义：中场（inter1/inter2）的「召唤」链路与原版不一致，需要按原版实现/验证。
原版链路（当前实现只有近似）：

1. `fase/inter1/ini`（或 inter2）：Koyomi 传送 `-1088 64 1426`、无敌+NoAI、
   盾交给 Gari；**真正的 Gari 继续留在场地上战斗**（不是被召唤消失）。
2. 5 秒：`h4/fase_ilusion/ini_veneno` 生成 3 个毒池 + veneno_as 旋转标记 +
   队伍染色（单人：随机玩家紫色，最远池变绿）。
3. inter1 46.5s / inter2 15s：`h4/ini` → `genpos`：18 个固定候选点随机留 11 个，
   **从 Gari 当前位置生成 11 条追迹**（每 tick 向目标移动），原 Gari 消失；
   追迹到达后逐一 `summon_gari` 生成幻象。
4. `end_parte1`：1 真 + 5 紫 + 5 绿幻象，血 100，75 秒侧边栏倒计时，提示 5s。
5. 打死真幻象 → `exito`：在原位重召唤真 Gari → `fase/2/ini`（或 3）。
6. 打死假幻象 → `boom`：爆炸 + 虚弱 + instant_damage I（首杀）/V（后续）。
7. 决赛 `fase/4/tp_end`：`reset` + `summon_iddle`（双 Boss 在出生点重生，
   Gari 持盾、无敌+NoAI、墨汁粒子）→ `dia_end` 对话链 → Gari 掉徽章 →
   Koyomi 退场 → 破 fleecy 箱 → victory。

当前实现问题点：追迹粒子/生成时序是近似；毒池只有单人布局且全员紫色；
队伍颜色显示未实现；`summon_iddle` 只在 tp_end/respawn 用。接手方应优先核对
「中场开始 → 追迹 → 幻象生成」的实体存在性和时序。

## 7. 剩余问题 4：需要做正常的右键触发 Boss 战状态机

现状：战斗只能通过命令 `/arena start b5` 开始（含 6 秒倒计时）。用户要求
改成「右键触发」的正式状态机。建议设计：

```text
IDLE（竞技场就绪，无 Boss）
  └─ 玩家对触发器右键（例如原版的雕像/祭坛方块、或自定义交互实体）
       └─ COUNTDOWN（6 秒，3/2/1 + 音乐 + 屏障，Boss 无敌+NoAI）
            └─ FASE_1 → INTER_1 → FASE_2 → INTER_2 → FASE_3 → FASE_4
                 ├─ VICTORY（掉落徽章、破箱、清理、回 IDLE）
                 └─ DEFEAT（5 秒后 respawn idle 双 Boss，回 IDLE）
```

接入点建议：

- `PlayerInteractEvent.RightClickBlock` 或右键自定义交互实体
  （`InteractEvent.EntityInteract`）触发 `B5EncounterManager.beginCountdown(...)`。
- 触发器位置/类型需要先和用户确认（原版 B5 入场是玩家走到某个点 + 对话，
  没有「右键」；右键方案是本模组自己的适配，需要明确触发物）。
- 状态机里需要防重入：IDLE 时才能触发；战斗中右键提示「已在战斗中」；
  Boss 存活时 `/arena reset` 被拦截（现有逻辑）。
- 可参考现有 `ArenaDeploymentData.state()`（IDLE/DEPLOYING/READY/ERROR）的
  状态枚举风格，把战斗状态也放进持久化数据，保证重载后状态不丢。

## 8. 接手后建议顺序

1. 先跑一遍现有实现：`/arena deploy b5` → `/arena start b5`，
   确认倒计时/阶段 1 能过（崩溃已修）。
2. 按问题 4 搭右键状态机（用户明确要的入口）。
3. 按问题 2 逐条核对技能触发，缺的补上（对照 `fase/*/run.mcfunction`）。
4. 按问题 3 修中场召唤链路（追迹/幻象/summon_iddle 时序）。
5. 最后按问题 1 重写粒子（先 h1/h2/h3/h5，再 h4）。
6. 每步：编译 → 游戏内截图验证 → 对照原版命令链自审 → 更新本文档。

## 9. 已知残留差异（本次未做，接手时注意）

- 多人毒池（`recu_ini_veneno`）未移植，单人中可用但全员紫色。
- 玩家队伍颜色（veneno_verde/morado 名字变色、sidebar DeathCount）未实现。
- 地图剧情集成（`ciane_boss` 计分板、胜利后 spawnpoint/羊毛 clone/庆祝序列）未移植。
- 对话用原版翻译键逐字显示，但原版「换行占位」动画未复刻。
