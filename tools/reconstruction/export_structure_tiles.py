#!/usr/bin/env python3
"""Export a bounded Java Anvil volume as tiled 1.20.1 structure NBT.

The exporter is deliberately conservative: entities and block entities are not
copied, and command/structure/jigsaw blocks are replaced with air. Boss logic
belongs in Java; the templates contain arena geometry only.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import sys
from collections import Counter
from pathlib import Path
from typing import Any

import nbtlib

sys.path.insert(0, str(Path(__file__).resolve().parent))
from region_probe import AnvilWorld, _block_name  # noqa: E402


DATA_VERSION_1_20_1 = 3465
UNSAFE_BLOCKS = {
    "minecraft:chain_command_block",
    "minecraft:command_block",
    "minecraft:jigsaw",
    "minecraft:repeating_command_block",
    "minecraft:spawner",
    "minecraft:structure_block",
}


def parse_state(state: str) -> tuple[str, dict[str, str]]:
    if "[" not in state:
        return state, {}
    name, raw_properties = state[:-1].split("[", 1)
    properties: dict[str, str] = {}
    for pair in raw_properties.split(","):
        key, value = pair.split("=", 1)
        properties[key] = value
    return name, properties


def palette_entry(state: str) -> nbtlib.Compound:
    name, properties = parse_state(state)
    entry = nbtlib.Compound({"Name": nbtlib.String(name)})
    if properties:
        entry["Properties"] = nbtlib.Compound(
            {key: nbtlib.String(value) for key, value in properties.items()}
        )
    return entry


def int_list(values: list[int]) -> Any:
    return nbtlib.List[nbtlib.Int]([nbtlib.Int(value) for value in values])


def write_tile(
    world: AnvilWorld,
    output_path: Path,
    source_min: tuple[int, int, int],
    size: tuple[int, int, int],
) -> dict[str, Any]:
    min_x, min_y, min_z = source_min
    size_x, size_y, size_z = size
    palette: list[str] = []
    palette_indices: dict[str, int] = {}
    blocks: list[nbtlib.Compound] = []
    block_counts: Counter[str] = Counter()
    sanitized: Counter[str] = Counter()

    for local_y in range(size_y):
        for local_z in range(size_z):
            for local_x in range(size_x):
                state = world.block_at(
                    min_x + local_x, min_y + local_y, min_z + local_z
                )
                name = _block_name(state)
                if name in UNSAFE_BLOCKS:
                    sanitized[name] += 1
                    state = "minecraft:air"
                    name = state
                state_index = palette_indices.get(state)
                if state_index is None:
                    state_index = len(palette)
                    palette_indices[state] = state_index
                    palette.append(state)
                block_counts[name] += 1
                blocks.append(
                    nbtlib.Compound(
                        {
                            "pos": int_list([local_x, local_y, local_z]),
                            "state": nbtlib.Int(state_index),
                        }
                    )
                )

    structure = nbtlib.Compound(
        {
            "DataVersion": nbtlib.Int(DATA_VERSION_1_20_1),
            "size": int_list([size_x, size_y, size_z]),
            "palette": nbtlib.List[nbtlib.Compound](
                [palette_entry(state) for state in palette]
            ),
            "blocks": nbtlib.List[nbtlib.Compound](blocks),
            "entities": nbtlib.List[nbtlib.Compound]([]),
        }
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    nbtlib.File(structure, gzipped=True).save(output_path)
    digest = hashlib.sha256(output_path.read_bytes()).hexdigest().upper()
    return {
        "file": output_path.name,
        "source_min": list(source_min),
        "size": list(size),
        "palette_size": len(palette),
        "blocks": size_x * size_y * size_z,
        "top_block_names": block_counts.most_common(20),
        "sanitized": dict(sanitized),
        "sha256": digest,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--world", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--name", required=True)
    parser.add_argument("--min-x", type=int, required=True)
    parser.add_argument("--min-y", type=int, required=True)
    parser.add_argument("--min-z", type=int, required=True)
    parser.add_argument("--max-x", type=int, required=True)
    parser.add_argument("--max-y", type=int, required=True)
    parser.add_argument("--max-z", type=int, required=True)
    parser.add_argument("--tile-size", type=int, default=48)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    minimum = (args.min_x, args.min_y, args.min_z)
    maximum = (args.max_x, args.max_y, args.max_z)
    if any(minimum[index] > maximum[index] for index in range(3)):
        raise SystemExit("Each minimum bound must be <= its maximum")
    if not 1 <= args.tile_size <= 48:
        raise SystemExit("--tile-size must be between 1 and 48")

    total_size = tuple(maximum[i] - minimum[i] + 1 for i in range(3))
    tile_counts = tuple(math.ceil(value / args.tile_size) for value in total_size)
    world = AnvilWorld(args.world)
    tiles: list[dict[str, Any]] = []

    for tile_y in range(tile_counts[1]):
        for tile_z in range(tile_counts[2]):
            for tile_x in range(tile_counts[0]):
                offset = (
                    tile_x * args.tile_size,
                    tile_y * args.tile_size,
                    tile_z * args.tile_size,
                )
                size = tuple(
                    min(args.tile_size, total_size[i] - offset[i])
                    for i in range(3)
                )
                source_min = tuple(minimum[i] + offset[i] for i in range(3))
                filename = f"tile_{tile_x}_{tile_y}_{tile_z}.nbt"
                tile = write_tile(world, args.output / filename, source_min, size)
                tile["index"] = [tile_x, tile_y, tile_z]
                tile["offset"] = list(offset)
                tiles.append(tile)

    manifest = {
        "name": args.name,
        "source_world": args.world.name,
        "source_level_dat_sha256": hashlib.sha256(
            (args.world / "level.dat").read_bytes()
        ).hexdigest().upper(),
        "source_bounds": {"min": list(minimum), "max": list(maximum)},
        "size": list(total_size),
        "tile_size": args.tile_size,
        "tile_counts": list(tile_counts),
        "data_version": DATA_VERSION_1_20_1,
        "policy": {
            "includes_air": True,
            "includes_entities": False,
            "includes_block_entities": False,
            "unsafe_blocks_replaced_with_air": sorted(UNSAFE_BLOCKS),
        },
        "tiles": tiles,
    }
    manifest_path = args.output / "manifest.json"
    manifest_path.write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(json.dumps(manifest, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
