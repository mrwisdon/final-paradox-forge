# Final Paradox Forge

Minecraft Forge 1.20.1 模组项目，用 Java 重新实现 Final Paradox 地图的
物品、能力、实体与遭遇战。

本仓库目前包含两个模块：

- `finalparadox`：Final Paradox 内容重制（含全新的 **B8 僵尸超级矩阵遭遇战**与**大地漫游器**）
- `ragecraft4-reforged`：Ragecraft IV 内容重制子模块

---

## 🎮 新内容游玩指南：B8 僵尸超级矩阵 & 大地漫游器

### 1. 部署竞技场（需要管理员权限）

```text
/finalparadox arena deploy b8            # 在你脚下部署 B8 竞技场
/finalparadox arena deploy b8 x y z      # 或指定锚点坐标
/finalparadox arena status               # 查看部署进度
```

部署完成后，科洛斯的回声会出现在竞技场中。**右键它对话**，在聊天框菜单里
选择"我想开始挑战"，确认后全员必须站在竞技场内，战斗进入倒计时。

管理员也可以跳过对话直接开始：

```text
/finalparadox arena start b8
```

### 2. 战斗开始：登上大地漫游器

倒计时开始时，每位玩家都会在竞技场周围获得一台 B8 战用大地漫游器。
**走到漫游器旁按住潜行（Shift）上车**。所有玩家上车后开始骑乘倒计时，
随后战斗正式开始。

> 战斗中无法下车；能量归零会熔毁并杀死骑手。

### 3. 大地漫游器操作手册

坐上任意型号的大地漫游器后，控制方式一致：

| 操作 | 按键 | 说明 |
|---|---|---|
| 前进 | 空手 | 空手时向前移动（跟随准星朝向） |
| 停止 | 手持任意物品 | 手持物品时原地停下 |
| 机炮射击 | 按住 Shift | 射击时无法转向 |
| 发射导弹 | 攻击键（左键） | TOW 线导，跟随你的准星 |
| 防御榴弹 | 右键 | 仅改进型，环绕一圈爆炸 |
| 跳跃 | 空格 | 跳 4 格高，消耗 2% 能量 |
| 下车 | 双击 Shift | 战斗中不可用 |

#### 两种型号的区别

| | B8 战用漫游器 | 改进型漫游器（自适应防御矩阵召唤） |
|---|---|---|
| 能量上限 | 100 | 25 |
| 机炮 | 每 3 tick 一发 × 7 伤害，**无限弹药**，2 格溅射半伤 | 每 5 tick 一发 × 18 伤害，2 格溅射半伤 |
| 机炮弹夹 | 无限 | 16/160，空仓 2 秒装填，每 10 秒补 32 发，可破坏刷怪笼 |
| 导弹 | 2/6（3 秒装填，10 秒补 1 发） | 同左 |
| 防御榴弹 | ❌ 无 | ✅ 右键，12 枚环绕 4.5 格爆炸（半径 3，每枚 20 真伤），15 秒冷却 |
| 特殊效果 | 战斗中锁定乘员 | 乘坐时获得伤害免疫 |
| 能量机制 | 不会自动消耗，被击中才扣 | 同左 |

导弹命中后造成 **40 点真实伤害**，3 格范围爆炸，沿途最多破坏 3 个方块。
防御榴弹只攻击敌对生物，不伤友军、不破坏方块。

### 4. B8 战斗机制

**僵尸超级矩阵**拥有 250 点生命值（红色 Boss 血条），共 5 个阶段，分别在
生命值 200 / 150 / 100 / 50 时进入下一阶段。每一阶段都会重复：

1. **模块分配损坏（H2）**：矩阵向空中射出大量发光的金色模块。在它们落地前
   用机炮全部打碎；模块落地会爆炸，对漫游器造成重创。清光全部模块后矩阵
   才会解除无敌，趁这时输出。
2. **激光折射图案（H1）**：从竞技场中心射出一道锁定某位玩家位置的光束，
   短暂停顿后沿锁定方向推进并连环爆炸，躲开光束路径。
3. **旋转爆炸区（H4）**：场地边缘出现旋转的警告标记，标记转满后会爆炸。
4. **增援波次（H3）**：一波波敌人从竞技场边缘跳入：

| 敌人 | 特点 |
|---|---|
| 机器人僵尸 | 隐形近战，成群冲向你 |
| 狙击手 | 悬浮在半空，每隔 9 秒发射一发光束弹 |
| 疯狂傀儡 | 高血量近战，附带碾压冲击波 |
| 炸弹人 | 扔出 TNT 炸弹；炸弹落地前免疫伤害，落地后可用机炮点掉（溅射可直接击碎） |

战斗内玩家获得**抗性提升 V + 生命恢复 V**，敌人不掉落物品。

**胜利**：矩阵被击破后，每位玩家都会在物品栏直接获得
**自适应防御矩阵**，竞技场播放烟花并清理。

**失败**：所有玩家死亡后回到竞技场入口，可以重新挑战。

### 5. 自适应防御矩阵（改进型漫游器）

从 B8 胜利中获得，用来召唤 **改进型大地漫游器**：

```text
右键穿戴到护腿槽 → 按 Q 丢出 → 召唤改进型漫游器并自动上车
```

- 冷却 **3 分钟**
- Boss 战期间无法使用
- 只能同时存在一台属于你的漫游器
- 改进型只有 25 点能量（被击中才扣，不会自动消耗）

### 6. 管理员 / 测试指令

```text
# 竞技场
/finalparadox arena deploy b8 [x y z]
/finalparadox arena status
/finalparadox arena start b8
/finalparadox arena reset b8

# B8 遭遇战调试
/finalparadox b8 status
/finalparadox b8 skip              # 直接胜利（同样发放奖励）
/finalparadox b8 health <0-250>
/finalparadox b8 phase <1-5>
/finalparadox b8 h1 [1|3]         # 激光光束
/finalparadox b8 h2               # 金色模块
/finalparadox b8 h4 <1-3>         # 旋转爆炸区
/finalparadox b8 add <zombie|sniper|golem|tnt>
/finalparadox b8 reset

# 大地漫游器
/finalparadox rover spawn          # B8 战用漫游器（默认）
/finalparadox rover spawn boss
/finalparadox rover spawn improved # 改进型漫游器
/finalparadox rover damage <1-100> # 测试能量扣除

# 超级矩阵模型（单独测试，非正式战斗，不掉奖励）
/finalparadox supermatrix spawn [vulnerable]
```

---

## 构建

### 环境

- JDK 17
- Windows / Linux / macOS
- 无需单独安装 Gradle，项目已包含 Gradle Wrapper

### 编译

Windows：

```powershell
.\gradlew.bat clean build
```

Linux / macOS：

```bash
./gradlew clean build
```

构建完成后，两个模块的 JAR 分别在：

- `build/libs/`
- `ragecraft4-reforged/build/libs/`

`build/`、`.gradle/`、`run/`、`run-data/` 和 Python `__pycache__/` 均为可
再生内容，不应加入源码提交包。

## Git 与大文件

音频资源（`*.ogg`）通过 Git LFS 管理。首次使用本仓库请先安装 Git LFS：

```bash
git lfs install
git lfs pull
```

提交前请确认 `git status` 中没有 `build/`、`run/`、日志或其他本机生成文件。
