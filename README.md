# Final Paradox Forge

Minecraft Forge 1.20.1 多工程项目，包含：

- `finalparadox`：Final Paradox 地图物品、能力、实体与遭遇战的 Forge 重制。
- `ragecraft4-reforged`：Ragecraft IV 内容重制子模组。

## 开发环境

- JDK 17
- Windows、Linux 或 macOS
- 无需单独安装 Gradle，工程已包含 Gradle Wrapper

## 构建

Windows：

```powershell
.\gradlew.bat clean build
```

Linux / macOS：

```bash
./gradlew clean build
```

构建完成后，两个模组的 JAR 分别位于：

- `build/libs/`
- `ragecraft4-reforged/build/libs/`

`build/`、`.gradle/`、`run/`、`run-data/` 和 Python `__pycache__/` 均为可再生成内容，不应加入源码交付包。

如需在构建后把 `finalparadox` JAR 自动复制到本机测试实例，可在用户级 Gradle 配置或命令行中设置可选属性 `local_mods_dir`；不要将个人绝对路径提交到工程配置。

## Git 与大文件

音频资源（`*.ogg`）通过 Git LFS 管理。首次使用本仓库前请安装 Git LFS，并执行：

```bash
git lfs install
git lfs pull
```

提交前请确认 `git status` 中没有 `build/`、`run/`、日志或其他本机生成文件。
