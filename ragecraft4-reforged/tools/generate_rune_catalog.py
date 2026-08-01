#!/usr/bin/env python3
"""Generate the RC4 rune catalog and its exact resource-pack asset closure.

The original map is the source of truth. This script reads deployed rune ItemStack
NBT from the extracted world, combines it with the crafting-station functions, and
writes deterministic mod resources. It intentionally copies only the Minecraft
item models/textures referenced by runes and forged weapons.
"""

from __future__ import annotations

import argparse
import gzip
import json
import re
import shutil
import zlib
from io import BytesIO
from pathlib import Path
from typing import Any, Iterable

import nbtlib


EXISTING_NAMES = {
    ("prefix", 106): "masterful_rune",
    ("prefix", 162): "transcendant_rune",
    ("prefix", 37): "templars_rune",
    ("prefix", 19): "bloodthirsty_rune",
    ("prefix", 39): "necromancers_rune",
    ("upgrade", 24): "rune_of_impact",
    ("upgrade", 21): "rune_of_cowardice",
    ("upgrade", 22): "rune_of_temerity",
    ("upgrade", 23): "rune_of_acceleration",
    ("upgrade", 20): "rune_of_impatience",
    ("upgrade", 25): "rune_of_toughness",
    ("upgrade", 26): "rune_of_versatility",
    ("upgrade", 27): "rune_of_inertia",
    ("upgrade", 28): "rune_of_incongruity",
    ("upgrade", 29): "rune_of_malice",
    ("suffix", 14): "rune_of_magma",
    ("suffix", 7): "rune_of_evisceration",
    ("suffix", 42): "rune_of_trapping",
    ("suffix", 5): "rune_of_dragonfire",
    ("suffix", 21): "rune_of_frost",
}

RARITIES = {0: "common", 1: "uncommon", 2: "rare", 3: "epic", 4: "legendary"}
TAG_TO_COMPATIBILITY = {
    "cr_sword": "sword",
    "cr_axe": "axe",
    "cr_bow": "bow",
    "cr_pickaxe": "pickaxe",
    "cr_helmet": "helmet",
    "cr_chestplate": "chestplate",
    "cr_leggings": "leggings",
    "cr_boots": "boots",
}
RUNE_KEYS = {"cr_enchant": "prefix", "cr_upgrade": "upgrade", "cr_modifier": "suffix"}
ABILITY_TAG_OVERRIDES = {
    # Modifier 122 is boots-only and its deployed lore describes empowered Adrenaline Rush.
    # The original crafting function accidentally writes the chestplate tag vt_agony.
    ("suffix", 122): "vt_adrenaline_rush",
}
WEAPON_ENTRYPOINTS = (
    "bow", "stone_sword", "iron_sword", "diamond_sword", "netherite_sword",
    "stone_axe", "iron_axe", "diamond_axe", "netherite_axe",
    "stone_pickaxe", "iron_pickaxe", "diamond_pickaxe", "netherite_pickaxe",
)
SKILL_VISUAL_ENTRYPOINTS = ("carrot_on_a_stick", "cyan_dye", "leather_horse_armor")


def plain(value: Any) -> Any:
    if isinstance(value, dict):
        return {str(k): plain(v) for k, v in value.items()}
    if isinstance(value, (list, tuple)):
        return [plain(v) for v in value]
    if hasattr(value, "unpack"):
        return value.unpack()
    return value


def chunks(region: Path) -> Iterable[Any]:
    data = region.read_bytes()
    for index in range(1024):
        header = data[index * 4:index * 4 + 4]
        offset = int.from_bytes(header[:3], "big") * 4096
        if not offset or offset + 5 > len(data):
            continue
        length = int.from_bytes(data[offset:offset + 4], "big")
        compression = data[offset + 4]
        payload = data[offset + 5:offset + 4 + length]
        try:
            if compression == 1:
                payload = gzip.decompress(payload)
            elif compression == 2:
                payload = zlib.decompress(payload)
            yield nbtlib.File.parse(BytesIO(payload))
        except Exception:
            continue


def compounds(value: Any) -> Iterable[dict[str, Any]]:
    if isinstance(value, dict):
        yield value
        for child in value.values():
            yield from compounds(child)
    elif isinstance(value, (list, tuple)):
        for child in value:
            yield from compounds(child)


def scan_deployed_runes(world: Path) -> dict[tuple[str, int], dict[str, Any]]:
    result: dict[tuple[str, int], dict[str, Any]] = {}
    for region in sorted(world.rglob("*.mca")):
        for chunk in chunks(region):
            for compound in compounds(chunk):
                tag = compound.get("tag")
                if not isinstance(tag, dict):
                    continue
                for nbt_key, category in RUNE_KEYS.items():
                    if nbt_key not in tag:
                        continue
                    source_id = int(tag[nbt_key])
                    key = (category, source_id)
                    if key in result:
                        break
                    display = tag.get("display", {})
                    result[key] = {
                        "sourceId": source_id,
                        "category": category,
                        "rarity": RARITIES[int(tag.get("cr_rarity", 0))],
                        "runePower": int(tag.get("cr_tier", 0)),
                        "compatibilities": [value for nbt, value in TAG_TO_COMPATIBILITY.items() if nbt in tag],
                        "originalItem": str(compound.get("id", "minecraft:air")),
                        "originalCustomModelData": int(tag.get("CustomModelData", 0)),
                        "nameJson": str(display.get("Name", '{"text":"Unknown Rune"}')),
                        "loreJson": [str(line) for line in display.get("Lore", [])],
                    }
                    break
    counts = {category: sum(1 for key in result if key[0] == category) for category in RUNE_KEYS.values()}
    expected = {"prefix": 160, "upgrade": 24, "suffix": 100}
    if counts != expected:
        raise RuntimeError(f"Deployed rune inventory mismatch: expected {expected}, found {counts}")
    return result


def parse_costs(ready_file: Path, score_name: str) -> dict[int, tuple[str, int]]:
    costs: dict[int, tuple[str, int]] = {}
    pattern = re.compile(
        rf"if score \${score_name} src4\.cr matches (\d+) run scoreboard players add \$(lapis|amethyst)_cost src4\.cr (\d+)"
    )
    for line in ready_file.read_text(encoding="utf-8").splitlines():
        match = pattern.search(line)
        if match:
            costs[int(match.group(1))] = (match.group(2), int(match.group(3)))
    return costs


def parse_prefix_effects(function: Path) -> tuple[dict[str, int], dict[str, int]]:
    lines = function.read_text(encoding="utf-8").splitlines()
    enchantments: dict[str, int] = {}
    stats: dict[str, int] = {}
    for index, line in enumerate(lines):
        if "execute store result score $temp src4.cr run data get" not in line:
            continue
        enchant = re.search(r'Enchantments\[\{id:"minecraft:([a-z0-9_]+)"\}\]\.lvl', line)
        stat = re.search(r"StandItem\.tag\.([a-z][a-z0-9_]*)$", line)
        amount = None
        for following in lines[index + 1:index + 5]:
            add = re.search(r"scoreboard players add \$temp src4\.cr (-?\d+)", following)
            if add:
                amount = int(add.group(1))
                break
        if amount is None:
            continue
        if enchant:
            enchantments[enchant.group(1)] = enchantments.get(enchant.group(1), 0) + amount
        elif stat and stat.group(1) not in {"custommodeldata"}:
            name = stat.group(1)
            stats[name] = stats.get(name, 0) + amount
    return enchantments, stats


def parse_upgrade_attributes(function: Path) -> list[dict[str, Any]]:
    lines = function.read_text(encoding="utf-8").splitlines()
    attributes: list[dict[str, Any]] = []
    operation = 0
    attribute_name = ""
    amount = 0
    for line in lines:
        match = re.search(r"upgrade_attribute_operation src4\.cr (-?\d+)", line)
        if match:
            operation = int(match.group(1))
        match = re.search(r'upgrade_attribute_name set value "([^"]+)"', line)
        if match:
            attribute_name = match.group(1)
        match = re.search(r"upgrade_attribute_amount src4\.cr (-?\d+)", line)
        if match:
            amount = int(match.group(1))
        if "function src4.cr:util/stack_attribute" in line and attribute_name:
            attributes.append({
                "attribute": attribute_name,
                "amount": amount / 100.0,
                "operation": "addition" if operation == 0 else "multiply_base",
            })
            attribute_name = ""
        elif "function src4.cr:util/attack_percent" in line:
            attributes.append({"attribute": "generic.attack_damage", "amount": amount / 100.0, "operation": "multiply_base"})
        elif "function src4.cr:util/attack_speed_percent" in line:
            attributes.append({"attribute": "generic.attack_speed", "amount": amount / 100.0, "operation": "multiply_base"})
    return attributes


def parse_suffix(function: Path) -> tuple[str, int, str, str]:
    text = function.read_text(encoding="utf-8")
    lines = text.splitlines()
    ability = ""
    for match in re.finditer(r"StandItem\.tag\.([a-z][a-z0-9_]*) set value 1b", text):
        ability = match.group(1)
    offset = 0
    for index, line in enumerate(lines):
        if "run data get storage src4.cr:main StandItem.tag.CustomModelData" not in line:
            continue
        for following in lines[index + 1:index + 6]:
            match = re.search(r"scoreboard players add \$temp src4\.cr (-?\d+)", following)
            if match:
                offset = int(match.group(1))
                break
    trim_material = ""
    trim_pattern = ""
    trim = re.search(r'StandItem\.tag\.Trim set value \{material: "([^"]+)", pattern: "([^"]+)"\}', text)
    if trim:
        trim_material, trim_pattern = trim.groups()
    return ability, offset, trim_material, trim_pattern


def parse_affix_name(function: Path, category: str) -> str:
    if category not in {"prefix", "suffix"}:
        return ""
    message_index = 1 if category == "prefix" else 2
    pattern = re.compile(rf"front_text\.messages\[{message_index}\] set value '(.*)'$")
    for line in function.read_text(encoding="utf-8").splitlines():
        match = pattern.search(line)
        if match:
            return match.group(1)
    return ""


def parse_forged_lore(function: Path, category: str) -> list[str]:
    if category not in {"upgrade", "suffix"}:
        return []
    section = "PreEnchantLore" if category == "upgrade" else "PostEnchantLore"
    pattern = re.compile(rf"{section} append value '(.*)'$")
    return [
        match.group(1)
        for line in function.read_text(encoding="utf-8").splitlines()
        if (match := pattern.search(line))
    ]


def model_overrides(model_file: Path) -> dict[int, str]:
    root = json.loads(model_file.read_text(encoding="utf-8"))
    return {
        int(override["predicate"]["custom_model_data"]): override["model"]
        for override in root.get("overrides", [])
        if "custom_model_data" in override.get("predicate", {})
    }


def registry_name(category: str, source_id: int) -> str:
    return EXISTING_NAMES.get((category, source_id), f"{category}_{source_id:03d}")


def resolve_model_file(assets: Path, reference: str) -> Path | None:
    namespace, path = reference.split(":", 1) if ":" in reference else ("minecraft", reference)
    candidate = assets / namespace / "models" / f"{path}.json"
    return candidate if candidate.is_file() else None


def copy_asset_closure(original_assets: Path, output_assets: Path, model_roots: Iterable[str]) -> None:
    pending = list(model_roots)
    visited: set[str] = set()
    while pending:
        reference = pending.pop()
        if reference in visited or reference.startswith("builtin/") or reference.startswith("builtin:"):
            continue
        visited.add(reference)
        source = resolve_model_file(original_assets, reference)
        if source is None:
            continue
        relative = source.relative_to(original_assets)
        target = output_assets / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        model = json.loads(source.read_text(encoding="utf-8"))
        parent = model.get("parent")
        if parent:
            pending.append(parent)
        pending.extend(override["model"] for override in model.get("overrides", []) if "model" in override)
        for texture in model.get("textures", {}).values():
            if not isinstance(texture, str) or texture.startswith("#"):
                continue
            namespace, path = texture.split(":", 1) if ":" in texture else ("minecraft", texture)
            texture_source = original_assets / namespace / "textures" / f"{path}.png"
            if texture_source.is_file():
                texture_target = output_assets / texture_source.relative_to(original_assets)
                texture_target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(texture_source, texture_target)
                metadata = texture_source.with_suffix(".png.mcmeta")
                if metadata.is_file():
                    shutil.copy2(metadata, texture_target.with_suffix(".png.mcmeta"))


def copy_suffix_runtime(map_root: Path, resources: Path) -> None:
    original_data = map_root / "datapacks" / "rc4" / "data"
    output_data = resources / "data"
    for namespace in ("skills", "custom_damage"):
        shutil.copytree(original_data / namespace, output_data / namespace, dirs_exist_ok=True)

    # A few suffix visuals and predicates intentionally live outside the skills namespace.
    area_target = output_data / "area" / "functions" / "green_crystal"
    area_target.mkdir(parents=True, exist_ok=True)
    for name in ("melt_2x1.mcfunction", "melt_3x2.mcfunction", "melt_4x3.mcfunction", "melt_small.mcfunction"):
        shutil.copy2(original_data / "area" / "functions" / "green_crystal" / name, area_target / name)

    external_predicates = {
        "general": ("biome_meadow", "biome_plains", "biome_snowy_taiga"),
        "mobs": ("is_on_fire", "invisibility"),
    }
    for namespace, names in external_predicates.items():
        target = output_data / namespace / "predicates"
        target.mkdir(parents=True, exist_ok=True)
        for name in names:
            shutil.copy2(original_data / namespace / "predicates" / f"{name}.json", target / f"{name}.json")

    minecraft_tags = (
        "blocks/icebound.json", "blocks/jump_through.json", "blocks/nonsolid.json", "blocks/nonsolid_nosnow.json", "blocks/snow_air.json",
        "damage_type/non_melee.json", "damage_type/non_melee_for_kill.json", "damage_type/trident.json",
        "entity_types/arthropod.json", "entity_types/can_be_on_fire.json", "entity_types/hostile.json",
        "entity_types/hostile_full_ice.json", "entity_types/non_undead.json", "entity_types/undead.json",
    )
    for relative in minecraft_tags:
        source = original_data / "minecraft" / "tags" / relative
        target = output_data / "minecraft" / "tags" / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)

    runtime = output_data / "ragecraft4reforged" / "functions"
    runtime.mkdir(parents=True, exist_ok=True)
    setup_lines = [
        line for line in (original_data / "general" / "functions" / "setup.mcfunction").read_text(encoding="utf-8").splitlines()
        if line.startswith("scoreboard objectives add ")
    ]
    setup_lines += [
        "scoreboard objectives add r4r_rune_power dummy",
        "scoreboard objectives add src4.cr dummy",
        "scoreboard players set #r4r tick10 0",
        "scoreboard players set #r4r tick20 0",
        "scoreboard players set #r4r tick40 0",
        "scoreboard players set #r4r tick60 0",
    ]
    (runtime / "suffix_load.mcfunction").write_text("\n".join(setup_lines) + "\n", encoding="utf-8")

    init_source = original_data / "events" / "functions" / "map_start" / "set_player_scores.mcfunction"
    init_lines = [
        line for line in init_source.read_text(encoding="utf-8").splitlines()
        if line.startswith("scoreboard players ") or line.startswith("advancement revoke ")
    ]
    init_lines.append("tag @s add r4r_suffix_init")
    init_lines.insert(0, "scoreboard players set @s r4r_rune_power 0")
    (runtime / "suffix_init_player.mcfunction").write_text("\n".join(init_lines) + "\n", encoding="utf-8")

    player_tick = """tag @s[predicate=skills:no_mana_regen] add no_mana_regen
tag @s[predicate=skills:mana_frenzy] add no_mana_regen
scoreboard players remove @s[scores={potion_cd=1..}] potion_cd 1
execute as @s[scores={carrot_stick_use=1..}] run function skills:misc/carrot_stick_use
execute at @s[scores={mob_kills=1..}] run function skills:misc/mob_kill
scoreboard players set @s[scores={meleehit=100000..}] meleehit 0
scoreboard players set @s[scores={meleehit=1..}] meleehit 100000
advancement revoke @s[tag=meleekill_x_got] only skills:meleekill_x
tag @s remove meleekill_x_got
tag @s[advancements={skills:meleekill_x=true}] add meleekill_x_got
scoreboard players add @s[scores={jump=1..}] jump_time 1
execute at @s[scores={jump=1..}] unless block ~ ~-0.01 ~ #minecraft:jump_through run function ragecraft4reforged:suffix_jump_end
scoreboard players set @s[scores={sprint_distance_2=1..,sprint_distance=0,jump=0}] sprint_distance_2 0
execute as @s[scores={sprint_distance=1..}] run function ragecraft4reforged:suffix_sprinting
execute as @s[scores={damage_taken=1..}] run function ragecraft4reforged:suffix_damage_taken
scoreboard players remove @s[scores={cold_snap_ready=1..}] cold_snap_ready 1
scoreboard players remove @s[scores={snowstorm_ready=1..}] snowstorm_ready 1
execute at @s[scores={void_rage_time=1..}] run function skills:axe/void_rage_tick
scoreboard players add @s[scores={trinity_cd=..40}] trinity_cd 1
execute as @s[scores={bowfired=1..}] run function skills:bow/bowfired
scoreboard players set @s[advancements={skills:bow_drawtime=false}] bow_drawtime 0
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_1
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_2
tag @s[advancements={skills:bow_drawtime=false}] remove sharpshot_3
advancement revoke @s only skills:bow_drawtime
execute as @s[scores={spawner_mined=1..}] run function skills:pickaxe/spawner_mined
effect give @s[predicate=skills:tenacity] resistance 1 0 true
execute as @s[predicate=skills:guardian_angel,scores={health=..6,guardian_cd=6000..}] run function skills:chest/guardian_angel
execute as @s[predicate=skills:guardian_angel,scores={guardian_cd=..5999}] run function skills:chest/guardian_cd
execute as @s[predicate=skills:ice_shield] run function skills:chest/ice_shield_tick
effect give @s[predicate=skills:leap_slam] jump_boost 1 3 true
scoreboard players remove @s[scores={bullrush_timer=1..}] bullrush_timer 1
execute at @s[predicate=skills:noxious_trail] run function skills:boots/noxious_trail
execute at @s[predicate=skills:flamewalker] run function skills:boots/flamewalker
execute at @s[scores={flameborn_duration=1..}] run function skills:axe/flameborn_tick
execute as @s[predicate=skills:black_magic] run function skills:helmet/black_magic
execute as @s[predicate=skills:rapid_decay] run function skills:helmet/rapid_decay
scoreboard players remove @s[scores={eviscerate_timer=1..}] eviscerate_timer 1
execute at @s[scores={eviscerate_timer=0,eviscerate_stage=1..}] run function skills:sword/eviscerate_recoup
scoreboard players remove @s[scores={vt_eviscerate_timer=1..}] vt_eviscerate_timer 1
execute at @s[scores={vt_eviscerate_timer=0,vt_eviscerate_stage=1..}] run function skills:sword/vt_eviscerate_recoup
execute at @s[predicate=skills:frostbite] run function skills:sword/frostbite_tick
execute at @s[predicate=skills:overcharge,scores={overcharge_time=1..}] run particle electric_spark ^-0.5 ^1 ^1 0.5 0.5 0.5 0.01 2 normal
execute as @s[scores={bladestorm_time=1..}] run function skills:sword/bladestorm_tick
execute at @s[scores={arcanist_timer=1..}] run function skills:bow/arcanist_player
execute at @s[scores={arcane_mom=1..}] run function skills:chest/arcane_mom_buffed
scoreboard players remove @s[scores={delayed_att=1..}] delayed_att 1
execute if score #r4r tick10 matches 10 run function ragecraft4reforged:suffix_player_tick10
execute if score #r4r tick20 matches 20 run function ragecraft4reforged:suffix_player_tick20
execute if score #r4r tick40 matches 40 run function ragecraft4reforged:suffix_player_tick40
execute if score #r4r tick60 matches 60 run function ragecraft4reforged:suffix_player_tick60
tag @s remove no_mana_regen
"""
    (runtime / "suffix_player_tick.mcfunction").write_text(player_tick, encoding="utf-8")

    periodic = {
        10: """effect give @s[predicate=skills:arcane_celerity,scores={mana=20..}] speed 1 0 true
effect give @s[predicate=skills:acrobatics] jump_boost 1 2 true
execute as @s[predicate=skills:ghost_form] run function skills:chest/ghost_form_tick
execute as @s[predicate=skills:duality] run function skills:boots/duality
execute as @s[predicate=skills:assassination] run function skills:helmet/assassination
execute as @s[predicate=skills:blood_pact] run function skills:helmet/blood_pact_tick
scoreboard players add @s[scores={spell_echo_cd=..10}] spell_echo_cd 1
execute at @s[scores={spell_echo_cd=8}] run function skills:helmet/spell_echo_trigger
execute at @s[predicate=skills:headhunter] run function skills:helmet/headhunter
""",
        20: """scoreboard players add @s[tag=!no_mana_regen,predicate=skills:divine_rej,scores={mana=..11,no_mana_regen=..0}] mana 1
scoreboard players add @s[tag=!no_mana_regen,scores={mana=..19,no_mana_regen=..0}] mana 1
scoreboard players add @s[scores={arcane_suprem=1..}] arcane_suprem 1
scoreboard players add @s[scores={spell_cd=..19}] spell_cd 1
scoreboard players add @s[predicate=skills:spellslinger,scores={spell_cd=..19}] spell_cd 1
scoreboard players add @s[predicate=skills:spell_power,scores={spell_cd=..19}] spell_cd 1
scoreboard players remove @s[scores={evocation_cd=1..}] evocation_cd 1
execute as @s[scores={spell_cd=20..}] run function skills:spells/spell_refill
execute as @s[scores={spell_cd=..19}] run function skills:spells/spell_empty
scoreboard players remove @s[scores={no_mana_regen=1..}] no_mana_regen 1
scoreboard players remove @s[scores={overcharge_time=1..}] overcharge_time 1
""",
        40: """scoreboard players add @s[tag=!no_mana_regen,predicate=skills:magic_affinity,scores={mana=..19,no_mana_regen=..0}] mana 1
scoreboard players add @s[tag=!no_mana_regen,predicate=skills:intellect_2,scores={mana=..19,no_mana_regen=..0}] mana 1
effect give @s[predicate=skills:divine_rej,scores={health=..15}] regeneration 3 1 true
""",
        60: """scoreboard players add @s[predicate=!skills:no_mana_regen,predicate=skills:intellect_1,scores={mana=..19,no_mana_regen=..0}] mana 1
effect give @s[predicate=skills:nature_blessing] regeneration 4 0 true
""",
    }
    for ticks, text in periodic.items():
        (runtime / f"suffix_player_tick{ticks}.mcfunction").write_text(text, encoding="utf-8")

    (runtime / "suffix_jump_end.mcfunction").write_text(
        "execute as @s[predicate=skills:xin_blessing] run function skills:chest/xin_blessing\n"
        "execute as @s[predicate=skills:leap_slam,scores={jump_time=18..}] run function skills:boots/leap_slam\n"
        "effect clear @s[predicate=skills:xin_blessing] strength\nscoreboard players set @s jump 0\nscoreboard players set @s jump_time 0\n",
        encoding="utf-8")
    (runtime / "suffix_sprinting.mcfunction").write_text(
        "scoreboard players add @s sprint_distance_2 1\nscoreboard players set @s[predicate=skills:bullrush,scores={sprint_distance_2=9..}] bullrush_timer 4\nscoreboard players set @s sprint_distance 0\n",
        encoding="utf-8")
    (runtime / "suffix_damage_taken.mcfunction").write_text(
        "execute as @s[predicate=skills:ghost_form] run function skills:chest/ghost_form\n"
        "execute as @s[predicate=skills:evocation,scores={evocation_cd=0}] run function skills:boots/evocation\n"
        "scoreboard players set @s damage_taken 0\n", encoding="utf-8")

    general = original_data / "general" / "functions"
    marker_sources = ("marker_tick.mcfunction", "marker_tick_a.mcfunction", "marker_tick_m.mcfunction", "marker_tick_s.mcfunction")
    for source_name in marker_sources:
        lines = [line for line in (general / source_name).read_text(encoding="utf-8").splitlines() if "function skills:" in line]
        (runtime / f"suffix_{source_name}").write_text("\n".join(lines) + "\n", encoding="utf-8")

    mob_tick = original_data / "mobs" / "functions" / "mob_tick.mcfunction"
    mob_lines = [line for line in mob_tick.read_text(encoding="utf-8").splitlines() if "function skills:" in line]
    (runtime / "suffix_mob_tick.mcfunction").write_text("\n".join(mob_lines) + "\n", encoding="utf-8")
    for period in (10, 20):
        source = original_data / "mobs" / "functions" / f"tick_everymob_{period}.mcfunction"
        lines = [line for line in source.read_text(encoding="utf-8").splitlines() if "skills:" in line and not line.lstrip().startswith("#")]
        (runtime / f"suffix_mob_tick{period}.mcfunction").write_text("\n".join(lines) + "\n", encoding="utf-8")

    tick = """execute as @a[tag=!r4r_suffix_init] run function ragecraft4reforged:suffix_init_player
scoreboard players add #r4r tick10 1
scoreboard players add #r4r tick20 1
scoreboard players add #r4r tick40 1
scoreboard players add #r4r tick60 1
execute as @a at @s run function ragecraft4reforged:suffix_player_tick
execute as @e[tag=marker_tick] at @s run function ragecraft4reforged:suffix_marker_tick
execute as @e[tag=marker_tick,type=armor_stand] at @s run function ragecraft4reforged:suffix_marker_tick_a
execute as @e[tag=marker_tick,type=marker] at @s run function ragecraft4reforged:suffix_marker_tick_m
execute as @e[tag=marker_tick,type=snowball] at @s run function ragecraft4reforged:suffix_marker_tick_s
execute as @e[type=arrow,tag=arrow_tick] at @s run function skills:bow/arrow_tick
execute as @e[type=arrow,scores={volley_1_delay=1..}] at @s run function skills:bow/volley_1_delay
execute as @e[type=arrow,scores={volley_2_delay=1..}] at @s run function skills:bow/volley_2_delay
execute as @e[type=#minecraft:hostile] at @s run function ragecraft4reforged:suffix_mob_tick
execute if score #r4r tick10 matches 10 as @e[type=#minecraft:hostile] at @s run function ragecraft4reforged:suffix_mob_tick10
execute if score #r4r tick20 matches 20 as @e[type=#minecraft:hostile] at @s run function ragecraft4reforged:suffix_mob_tick20
execute if score #r4r tick10 matches 10.. run scoreboard players set #r4r tick10 0
execute if score #r4r tick20 matches 20.. run scoreboard players set #r4r tick20 0
execute if score #r4r tick40 matches 40.. run scoreboard players set #r4r tick40 0
execute if score #r4r tick60 matches 60.. run scoreboard players set #r4r tick60 0
"""
    (runtime / "suffix_tick.mcfunction").write_text(tick, encoding="utf-8")

    for name, value in (("load", "ragecraft4reforged:suffix_load"), ("tick", "ragecraft4reforged:suffix_tick")):
        tag = output_data / "minecraft" / "tags" / "functions" / f"{name}.json"
        tag.parent.mkdir(parents=True, exist_ok=True)
        tag.write_text(json.dumps({"replace": False, "values": [value]}, indent=2) + "\n", encoding="utf-8")

    for name in ("axe_throw_texture_1.mcfunction", "axe_throw_texture_2.mcfunction"):
        (output_data / "skills" / "functions" / "axe" / name).write_text(
            "item replace entity @s weapon.mainhand from entity @p[distance=..5,limit=1,sort=nearest] weapon.mainhand\n",
            encoding="utf-8")

    # Standalone compatibility repairs for stale or map-global references in the source pack.
    replacements = {
        output_data / "skills" / "functions" / "axe" / "l_warp_check.mcfunction": {
            "skills:melee/l_warp": "skills:axe/l_warp",
        },
        output_data / "skills" / "functions" / "sword" / "bladestorm_tick.mcfunction": {
            "skills:sword/bladstorm_anim_": "skills:sword/bladestorm_anim_",
        },
        output_data / "skills" / "functions" / "leggings" / "runebinding_trig.mcfunction": {
            "$emerald_lvl src4.cr": "@s r4r_rune_power",
        },
    }
    for path, values in replacements.items():
        text = path.read_text(encoding="utf-8")
        for before, after in values.items():
            text = text.replace(before, after)
        path.write_text(text, encoding="utf-8")

    spawner_mined = output_data / "skills" / "functions" / "pickaxe" / "spawner_mined.mcfunction"
    spawner_mined.write_text("\n".join(
        line for line in spawner_mined.read_text(encoding="utf-8").splitlines()
        if "advancement grant" not in line or "challenges:explorer/" not in line
    ) + "\n", encoding="utf-8")

    damage_13 = output_data / "custom_damage" / "functions" / "damage13.mcfunction"
    damage_13.write_text(
        (output_data / "custom_damage" / "functions" / "damage12.mcfunction")
        .read_text(encoding="utf-8")
        .replace("1199", "1299")
        .replace("1200", "1300"),
        encoding="utf-8",
    )
    (output_data / "skills" / "functions" / "misc" / "meleekill.mcfunction").write_text(
        "function skills:misc/meleekill_x\n", encoding="utf-8")
    (output_data / "skills" / "functions" / "bow" / "arcane_arrow_tick.mcfunction").write_text(
        "# Arcane Arrow's damage and particles are applied by arcane_arrow_2 and arrow_tick.\n", encoding="utf-8")
    for name in (
        "amplified.mcfunction", "amplified_marker.mcfunction",
        "amplified_melee.mcfunction",
        "amplified_volley_1a.mcfunction", "amplified_volley_1b.mcfunction",
        "amplified_volley_2a.mcfunction", "amplified_volley_2b.mcfunction",
        "amplified_volley_2c.mcfunction", "amplified_volley_2d.mcfunction",
    ):
        (output_data / "skills" / "functions" / "bow" / name).write_text(
            "# The released RC4 pack references this removed Amplified helper; no deployed suffix uses it.\n",
            encoding="utf-8",
        )
    slow_6 = output_data / "skills" / "advancements" / "slow_6_hit.json"
    slow_6.write_text(
        (output_data / "skills" / "advancements" / "slow_5_hit.json")
        .read_text(encoding="utf-8")
        .replace('"amplifier": 4', '"amplifier": 5')
        .replace("cold_ice_hit_5", "cold_ice_hit_6"),
        encoding="utf-8",
    )


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    project = script_dir.parent
    workspace = project.parents[1]
    parser = argparse.ArgumentParser()
    parser.add_argument("--map", type=Path, default=workspace / ".map-inspection" / "ragecraft-iv")
    parser.add_argument("--world", type=Path, default=workspace / ".map-inspection" / "ragecraft-iv-world")
    args = parser.parse_args()

    resources = project / "src" / "main" / "resources"
    original_assets = args.map / "resources" / "assets"
    crafting = args.map / "datapacks" / "src4.crafting" / "data" / "src4.cr" / "functions" / "crafting_station"
    runes = scan_deployed_runes(args.world)
    costs = {
        "prefix": parse_costs(crafting / "enchants" / "ready.mcfunction", "enchant"),
        "upgrade": parse_costs(crafting / "upgrades" / "ready.mcfunction", "upgrade"),
        "suffix": parse_costs(crafting / "modifiers" / "ready.mcfunction", "modifier"),
    }
    icon_models = {
        "minecraft:purple_dye": model_overrides(original_assets / "minecraft" / "models" / "item" / "purple_dye.json"),
        "minecraft:cyan_dye": model_overrides(original_assets / "minecraft" / "models" / "item" / "cyan_dye.json"),
    }

    catalog: list[dict[str, Any]] = []
    icon_roots: set[str] = set()
    for key in sorted(runes, key=lambda item: ({"prefix": 0, "upgrade": 1, "suffix": 2}[item[0]], item[1])):
        category, source_id = key
        entry = runes[key]
        currency, cost = costs[category].get(source_id, ("none", 0))
        entry["registryName"] = registry_name(category, source_id)
        entry["currency"] = currency
        entry["cost"] = cost
        entry["enchantments"] = {}
        entry["stats"] = {}
        entry["attributes"] = []
        entry["abilityTag"] = ""
        entry["modelOffset"] = 0
        entry["trimMaterial"] = ""
        entry["trimPattern"] = ""
        function = crafting / {"prefix": "enchants", "upgrade": "upgrades", "suffix": "modifiers"}[category] / f"{source_id}.mcfunction"
        entry["affixNameJson"] = parse_affix_name(function, category)
        entry["forgedLoreJson"] = parse_forged_lore(function, category)
        if category == "prefix":
            entry["enchantments"], entry["stats"] = parse_prefix_effects(function)
        elif category == "upgrade":
            entry["attributes"] = parse_upgrade_attributes(function)
        else:
            entry["abilityTag"], entry["modelOffset"], entry["trimMaterial"], entry["trimPattern"] = parse_suffix(function)
            entry["abilityTag"] = ABILITY_TAG_OVERRIDES.get(key, entry["abilityTag"])
        icon = icon_models[entry["originalItem"]].get(entry["originalCustomModelData"])
        if not icon:
            raise RuntimeError(f"Missing icon model for {key}: CMD {entry['originalCustomModelData']}")
        entry["iconModel"] = icon
        icon_roots.add(icon)
        catalog.append(entry)

    data_dir = resources / "data" / "ragecraft4reforged"
    data_dir.mkdir(parents=True, exist_ok=True)
    (data_dir / "runes.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    model_dir = resources / "assets" / "ragecraft4reforged" / "models" / "item"
    model_dir.mkdir(parents=True, exist_ok=True)
    for entry in catalog:
        (model_dir / f"{entry['registryName']}.json").write_text(
            json.dumps({"parent": entry["iconModel"]}, indent=2) + "\n", encoding="utf-8"
        )

    tag_dir = data_dir / "tags" / "items" / "runes"
    tag_dir.mkdir(parents=True, exist_ok=True)
    for category in ("prefix", "upgrade", "suffix"):
        values = [f"ragecraft4reforged:{entry['registryName']}" for entry in catalog if entry["category"] == category]
        (tag_dir / f"{category}.json").write_text(json.dumps({"replace": False, "values": values}, indent=2) + "\n", encoding="utf-8")

    # The original translation namespace and custom fonts are part of the item identity.
    shutil.copytree(original_assets / "rc4", resources / "assets" / "rc4", dirs_exist_ok=True)
    copy_suffix_runtime(args.map, resources)

    sounds_json = original_assets / "minecraft" / "sounds.json"
    if sounds_json.is_file():
        sound_target = resources / "assets" / "minecraft" / "sounds.json"
        sound_target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(sounds_json, sound_target)
    sounds = original_assets / "minecraft" / "sounds"
    if sounds.is_dir():
        shutil.copytree(sounds, resources / "assets" / "minecraft" / "sounds", dirs_exist_ok=True)

    weapon_roots: set[str] = set()
    minecraft_models = original_assets / "minecraft" / "models" / "item"
    for entrypoint in WEAPON_ENTRYPOINTS + SKILL_VISUAL_ENTRYPOINTS:
        source = minecraft_models / f"{entrypoint}.json"
        if source.is_file():
            target = resources / "assets" / "minecraft" / "models" / "item" / source.name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
            model = json.loads(source.read_text(encoding="utf-8"))
            weapon_roots.update(override["model"] for override in model.get("overrides", []) if "model" in override)
    entrypoint_roots = {f"minecraft:item/{entrypoint}" for entrypoint in WEAPON_ENTRYPOINTS + SKILL_VISUAL_ENTRYPOINTS}
    copy_asset_closure(original_assets, resources / "assets", icon_roots | weapon_roots | entrypoint_roots)

    # Parse every generated JSON now, so malformed data never reaches Gradle.
    for path in resources.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8-sig"))
    print(f"Generated {len(catalog)} runes: 160 prefix, 24 upgrade, 100 suffix")
    print(f"Copied {len(icon_roots)} rune icon roots and {len(weapon_roots)} forged-weapon model roots")


if __name__ == "__main__":
    main()
