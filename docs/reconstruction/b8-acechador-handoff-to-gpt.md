# B8 敌方大地漫游器（acechador）交接文档

状态：**留给 ChatGPT 实现**（2026-08-02 用户决定）。当前 mod 中
`B8EncounterController.acechadorRound()` 是占位符（只播音效+提示文案），
`acechadorExists()` 等待一个从未生成的 `14_acechador_core` 标签。
本文档是已提取的权威证据，接手时无需重新解包数据包。

## 权威源

- 数据包：
  `C:\Users\19818\Desktop\newmod\.codex-tmp\fp-dlc-1.1.15-b\extracted\datapacks\luisb1202-functions\data\luisb1202\functions\carga_lanas\14_verde\el_acechador\**`（89 个函数）
- B8 挂载点：`bossfight/b8/h3/acechador/pos.mcfunction`（16 点半径 10 + 中心，
  随机选 1 个位置执行 `el_acechador/gen`）
- 每 tick 驱动：`bossfight/b8/run.mcfunction` 调 `el_acechador/main`；
  清理：`bossfight/b8/reset` 和 `h3/reset` 调 `el_acechador/reset`

## 生成结构（每只，全部盔甲架/羊驼拼装）

- `14_acechador_core`：根盔甲架（NoGravity、Invisible、Small:0/Marker:0）
- `14_acechador_hitbox`：羊驼（单人 200 HP / 多人 300 HP，NoAI，`hostile` 标签，
  DeathLootTable empty，boss 存在时 Invulnerable=0；这就是可受伤本体）
- 车体 4 面 as1-4（smooth_stone_slab 头、45° 倾角）+ 8 条腿 as5-12
  （90°/135°，上下两层），位置偏移见 `caja/gen` 与 `set_pos`
- 双炮管 `14_acechador_canon`（netherite_block 头，其中 1 个带
  `canon_punta`，`rayo_laser` 分数控制伸缩，`recu_pos` 每次前移 0.43 格）
- `14_acechador_name` 名称牌（`caja.gen.1` 译名）、`14_acechador_vida` 血条名牌
- `patas/gen`：4 条腿核心 + 逐帧动画 `patas/animacion/frame/{5,10,15,20}`
- 出场：`gen` 在头顶 20 格生成 → `tirarse/ini` 收腿下坠落地（danom4=20）
- 实例化：`14_id` 分数区分多只；`instance_run` 用
  `14_id_aux -= 14_id` 临时隔离当前实例

## 每 tick 行为（main → run → instance_run）

- 无玩家在 65 格内 → `explotar`（自毁爆炸，无掉落）
- `run_disparar`（未冷却）：
  - 腿动画帧
  - `colocar_cabeza`：炮管对准最近非旁观玩家（`facing entity @p`），
    `Pose.Head[0]` 跟随核心 Rotation[1]
  - `danom=3..` 且玩家在 20 格内 → 机枪 `caja/ametrallar/gen_bala`
- `run_apagada`（过热/被 DDD 手榴弹打停）：冒烟/气泡，danom2/danom3 递减，
  归零后 `encender` 重新点亮

## 攻击 1：机枪（caja/ametrallar）

- 每 3 tick 一发；弹丸每 tick `^ ^ ^1` 前进 1 格，end_rod + dust(0.231) 粒子
- 命中玩家（下方 1 格内 0.5 格）：瞬间伤害 II + 1 tick 抗性
- 命中方块：非 `#luisb1202:indestructible` 累计 7 次破坏（带短暂标记实体）
- 射速节奏由 `danom2` 控制：-20 启动，5/tick 累加，180-195 降速，
  200+（单机 350）→ `apagar`（冷却，`msg.1` 提示 DDD 手榴弹可破免疫）
- 冷却中 `apagada` 状态每 tick danom2-=2、danom3-=1，归零重新点火

## 攻击 2：导弹（misil）

- 过热后 `unless boss` 时发射；40 格内随机选玩家（multi 可多枚、单机仅 1 枚）
- 发射：目标标题提示 `misil.ini2.1` + trident.return 音 + explosion/cloud/lava 粒子
- 飞行：`instance_run` 逐 tick 转向锁定玩家（`facing entity @p[scores=14_misil_id=0] eyes`），
  步进 0.1/0.16/0.04/0.04/0.2（按 danom 加速）；尾焰粒子 12 帧循环
- 命中玩家（前方 0.8 格、下方 1 格内 1.4 格）：`boom`——72 向 crit 粒子 +
  explosion/end_rod/flame/large_smoke，瞬间伤害 IV，全体玩家（id_lana=14）
  虚弱+挖掘疲劳 10 秒，`misil.msg_hit.1` 提示
- 撞方块：破坏（`setblock air destroy`），3 次后爆炸；追丢（>40 格）消失

## 移动：自写步进寻路（pathfinding）

- `aux1` 每 10 tick 决策：直走（paso）/ 左绕（g_neg，aux1=10）/ 右绕（g_pos，aux1=20）
- `check_valid_pos`：脚下 3 格悬空 → aux2=1（下坠 tirarse）；
  前方 3 格高墙 → aux2=2（跳跃 saltar，收腿 +3 格跳）
- 玩家 10 格内 `switch_jugador_cerca` 转向绕行；`zombie_guia` 生成无敌僵尸引导者
  代跑路径（follow_range 100），acechador 每 tick tp 跟随
- 跳跃/下坠：`saltar/ini`、`tirarse/ini`（收腿/伸腿动画 + firework.launch 音）

## 死亡（morir）/ 自毁（explotar）

- 爆炸音 + explosion/large_smoke/lava/flame/smooth_stone_slab×200 粒子 + 36 向大烟雾环
- 无 boss：掉落 `items/megamatriz_perneras/item_forja_drop_1`
  （minecart 底材、RepairCost 999999、Unbreakable、`forja:1`、`terracechador_1:1` 标签、
   定制名/词条，HideFlags 127）并生成友好坐骑 `el_montura/gen`
  （= mod 的 `TerrastalkerRoverEntity.spawnEncounter`），1 秒后 `msg_subir.1` 提示
- `14_acechador_count >= 2` → 矩阵血量压到 30
- `barravida`：受击时（HurtTime=9 检测）按血量百分比改名牌
  （`bossfight.b4.h2.barravida.1-10`，❤▌条）

## 翻译键（zh/en 已从 VM 汉化与原版 resources.zip 提取，未写入 mod lang）

- `luisb1202.functions.carga_lanas.14_verde.el_acechador.caja.gen.1`
  → §c§l大地漫游器 / §c§lTerrastalker
- `...el_acechador.msg.1`（冷却提示，DDD 手榴弹）
- `...el_acechador.msg_subir.1`（上坐骑提示）
- `...el_acechador.misil.ini2.1` / `...misil.msg_hit.1`
- 血条复用 `bossfight.b4.h2.barravida.1-10`（mod lang 已有 B8 段，需核对）

## 建议实现方式

- 新增服务端权威实体（如 `HostileTerrastalkerEntity`），`ModEntities` 注册 + 渲染器；
  ronda 3/4 生成 1 只、ronda 5/6 生成 2 只（原版 `fase/5/ronda3-6`，
  第二只用 ronda5 复数文案）
- 状态机逐项还原：出场下坠 → 步进寻路（直行/绕行/跳跃/下坠）→ 机枪 →
  过热冷却 → 导弹 → 血条 → 65 格自毁 → 死亡掉落+坐骑+矩阵血 30
- `acechadorExists` 改查新实体；`h3Cleanup`/`endEncounter` 清理接入
- 渲染方案二选一：复合渲染器（推荐，技能要求）或原版盔甲架拼装
