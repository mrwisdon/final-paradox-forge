#!/usr/bin/env python3
"""Read-only probe for Minecraft 1.16+ Anvil worlds.

The tool scans a bounded cuboid directly from region files and emits:

* a JSON report with block counts, occupied extents, and per-Y occupancy;
* a top-down PNG using the highest non-air block in the requested Y range.

It intentionally does not load the world through Minecraft and never writes to
the source world directory.
"""

from __future__ import annotations

import argparse
import gzip
import hashlib
import io
import json
import math
import struct
import zlib
from collections import Counter, deque
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

import nbtlib
from PIL import Image


AIR_BLOCKS = {
    "minecraft:air",
    "minecraft:cave_air",
    "minecraft:void_air",
}


def _plain(value: Any) -> Any:
    """Convert nbtlib tags to ordinary Python values."""
    if isinstance(value, dict):
        return {str(key): _plain(item) for key, item in value.items()}
    if isinstance(value, (list, tuple)):
        return [_plain(item) for item in value]
    return getattr(value, "unpack", lambda: value)()


def _palette_key(entry: Any) -> str:
    name = str(entry["Name"])
    properties = entry.get("Properties")
    if not properties:
        return name
    parts = [f"{key}={properties[key]}" for key in sorted(properties)]
    return f"{name}[{','.join(parts)}]"


def _block_name(block_state: str) -> str:
    return block_state.split("[", 1)[0]


@dataclass(frozen=True)
class Section:
    palette: tuple[str, ...]
    indices: tuple[int, ...] | None

    def block_at(self, x: int, y: int, z: int) -> str:
        if self.indices is None:
            return self.palette[0]
        index = (y << 8) | (z << 4) | x
        palette_index = self.indices[index]
        if palette_index >= len(self.palette):
            return "minecraft:air"
        return self.palette[palette_index]


class AnvilWorld:
    """Minimal cached reader for legacy and modern Java chunk NBT layouts."""

    def __init__(self, world_dir: Path) -> None:
        self.world_dir = world_dir
        self.region_dir = world_dir / "region"
        if not (world_dir / "level.dat").is_file():
            raise FileNotFoundError(f"level.dat not found under {world_dir}")
        if not self.region_dir.is_dir():
            raise FileNotFoundError(f"region directory not found under {world_dir}")
        self._chunks: dict[tuple[int, int], dict[int, Section]] = {}

    def _read_chunk_nbt(self, chunk_x: int, chunk_z: int) -> Any | None:
        region_x = chunk_x // 32
        region_z = chunk_z // 32
        region_path = self.region_dir / f"r.{region_x}.{region_z}.mca"
        if not region_path.is_file():
            return None

        local_x = chunk_x & 31
        local_z = chunk_z & 31
        location_index = local_x + local_z * 32

        with region_path.open("rb") as handle:
            handle.seek(location_index * 4)
            location = handle.read(4)
            if len(location) != 4:
                return None
            sector_offset = int.from_bytes(location[:3], "big")
            sector_count = location[3]
            if sector_offset == 0 or sector_count == 0:
                return None

            handle.seek(sector_offset * 4096)
            length_bytes = handle.read(4)
            if len(length_bytes) != 4:
                return None
            length = int.from_bytes(length_bytes, "big")
            if length <= 1 or length > sector_count * 4096 - 4:
                raise ValueError(f"Invalid chunk length {length} in {region_path}")
            compression = handle.read(1)
            payload = handle.read(length - 1)

        if compression == b"\x01":
            raw = gzip.decompress(payload)
        elif compression == b"\x02":
            raw = zlib.decompress(payload)
        elif compression == b"\x03":
            raw = payload
        else:
            raise ValueError(
                f"Unsupported compression type {compression!r} in {region_path}"
            )
        return nbtlib.File.parse(io.BytesIO(raw))

    @staticmethod
    def _decode_section(section: Any) -> Section | None:
        modern_states = section.get("block_states")
        if modern_states is not None:
            palette_tag = modern_states.get("palette")
            states = modern_states.get("data")
        else:
            palette_tag = section.get("Palette")
            states = section.get("BlockStates")
        if palette_tag is None or len(palette_tag) == 0:
            return None
        palette = tuple(_palette_key(entry) for entry in palette_tag)
        if len(palette) == 1:
            return Section(palette=palette, indices=None)

        if states is None or len(states) == 0:
            return None
        bits = max(4, math.ceil(math.log2(len(palette))))
        values_per_long = 64 // bits
        mask = (1 << bits) - 1
        indices: list[int] = []
        for packed in states:
            unsigned = int(packed) & ((1 << 64) - 1)
            for slot in range(values_per_long):
                indices.append((unsigned >> (slot * bits)) & mask)
                if len(indices) == 4096:
                    return Section(palette=palette, indices=tuple(indices))
        if len(indices) < 4096:
            raise ValueError(
                f"BlockStates decoded to {len(indices)} entries; expected 4096"
            )
        return Section(palette=palette, indices=tuple(indices[:4096]))

    def _load_chunk(self, chunk_x: int, chunk_z: int) -> dict[int, Section]:
        cache_key = (chunk_x, chunk_z)
        cached = self._chunks.get(cache_key)
        if cached is not None:
            return cached

        root = self._read_chunk_nbt(chunk_x, chunk_z)
        sections: dict[int, Section] = {}
        if root is not None:
            level = root.get("Level", root)
            section_tags = level.get("sections", level.get("Sections", []))
            for section_tag in section_tags:
                section = self._decode_section(section_tag)
                if section is not None:
                    sections[int(section_tag["Y"])] = section
        self._chunks[cache_key] = sections
        return sections

    def block_at(self, x: int, y: int, z: int) -> str:
        sections = self._load_chunk(x // 16, z // 16)
        section = sections.get(y // 16)
        if section is None:
            return "minecraft:air"
        return section.block_at(x & 15, y & 15, z & 15)


def _family_color(block_state: str) -> tuple[int, int, int]:
    """Stable, readable colors for arena-boundary inspection."""
    name = _block_name(block_state)
    explicit = {
        "minecraft:barrier": (255, 0, 255),
        "minecraft:bedrock": (40, 40, 40),
        "minecraft:gold_block": (250, 210, 40),
        "minecraft:lava": (255, 70, 10),
        "minecraft:water": (35, 95, 220),
        "minecraft:glass": (190, 230, 240),
        "minecraft:obsidian": (45, 25, 65),
        "minecraft:crying_obsidian": (75, 35, 120),
    }
    if name in explicit:
        return explicit[name]
    if "nether" in name or "crimson" in name:
        return (125, 42, 38)
    if "blackstone" in name or "basalt" in name:
        return (58, 55, 60)
    if "quartz" in name:
        return (225, 218, 205)
    if "stone" in name or "andesite" in name:
        return (125, 125, 125)
    if "wood" in name or "planks" in name or "log" in name:
        return (130, 88, 48)
    if "leaves" in name or "grass" in name or "moss" in name:
        return (65, 130, 55)
    digest = hashlib.sha256(name.encode("utf-8")).digest()
    return tuple(65 + channel % 150 for channel in digest[:3])


def _inclusive_range(low: int, high: int) -> Iterable[int]:
    return range(low, high + 1)


def flood_air(
    world: AnvilWorld,
    bounds: tuple[int, int, int, int, int, int],
    seed: tuple[int, int, int],
) -> tuple[dict[str, Any], set[int]]:
    """Find the air volume connected to a known player/boss position."""
    min_x, min_y, min_z, max_x, max_y, max_z = bounds
    seed_x, seed_y, seed_z = seed
    if not (
        min_x <= seed_x <= max_x
        and min_y <= seed_y <= max_y
        and min_z <= seed_z <= max_z
    ):
        raise ValueError(f"Air seed {seed} falls outside scan bounds")
    seed_state = world.block_at(seed_x, seed_y, seed_z)
    if _block_name(seed_state) not in AIR_BLOCKS:
        raise ValueError(f"Air seed {seed} is {seed_state}, not air")

    width = max_x - min_x + 1
    depth = max_z - min_z + 1

    def encode(x: int, y: int, z: int) -> int:
        return ((y - min_y) * depth + (z - min_z)) * width + (x - min_x)

    def decode(value: int) -> tuple[int, int, int]:
        x_offset = value % width
        rest = value // width
        z_offset = rest % depth
        y_offset = rest // depth
        return min_x + x_offset, min_y + y_offset, min_z + z_offset

    start = encode(*seed)
    visited = {start}
    queue = deque([start])
    occupied_min = [seed_x, seed_y, seed_z]
    occupied_max = [seed_x, seed_y, seed_z]
    by_y: Counter[int] = Counter()
    touched: set[str] = set()

    while queue:
        encoded = queue.popleft()
        x, y, z = decode(encoded)
        by_y[y] += 1
        occupied_min[0] = min(occupied_min[0], x)
        occupied_min[1] = min(occupied_min[1], y)
        occupied_min[2] = min(occupied_min[2], z)
        occupied_max[0] = max(occupied_max[0], x)
        occupied_max[1] = max(occupied_max[1], y)
        occupied_max[2] = max(occupied_max[2], z)
        if x == min_x:
            touched.add("min_x")
        if x == max_x:
            touched.add("max_x")
        if y == min_y:
            touched.add("min_y")
        if y == max_y:
            touched.add("max_y")
        if z == min_z:
            touched.add("min_z")
        if z == max_z:
            touched.add("max_z")

        for next_x, next_y, next_z in (
            (x - 1, y, z),
            (x + 1, y, z),
            (x, y - 1, z),
            (x, y + 1, z),
            (x, y, z - 1),
            (x, y, z + 1),
        ):
            if not (
                min_x <= next_x <= max_x
                and min_y <= next_y <= max_y
                and min_z <= next_z <= max_z
            ):
                continue
            next_encoded = encode(next_x, next_y, next_z)
            if next_encoded in visited:
                continue
            if _block_name(world.block_at(next_x, next_y, next_z)) not in AIR_BLOCKS:
                continue
            visited.add(next_encoded)
            queue.append(next_encoded)

    report = {
        "seed": list(seed),
        "seed_state": seed_state,
        "block_count": len(visited),
        "extents": {"min": occupied_min, "max": occupied_max},
        "touches_scan_bounds": sorted(touched),
        "air_by_y": [[y, by_y[y]] for y in sorted(by_y)],
    }
    return report, visited


def scan_box(
    world: AnvilWorld,
    bounds: tuple[int, int, int, int, int, int],
    png_path: Path,
    scale: int,
    slice_y: int | None,
    highlighted_air: set[int] | None = None,
) -> dict[str, Any]:
    min_x, min_y, min_z, max_x, max_y, max_z = bounds
    width = max_x - min_x + 1
    depth = max_z - min_z + 1
    image = Image.new("RGB", (width, depth), (8, 8, 12))
    pixels = image.load()

    state_counts: Counter[str] = Counter()
    name_counts: Counter[str] = Counter()
    occupancy_y: Counter[int] = Counter()
    occupied_min: list[int] | None = None
    occupied_max: list[int] | None = None
    surface_y: list[int] = []

    for z in _inclusive_range(min_z, max_z):
        for x in _inclusive_range(min_x, max_x):
            top_state: str | None = None
            top_y: int | None = None
            slice_state: str | None = None
            for y in _inclusive_range(min_y, max_y):
                state = world.block_at(x, y, z)
                name = _block_name(state)
                if y == slice_y:
                    slice_state = state
                if name in AIR_BLOCKS:
                    continue
                state_counts[state] += 1
                name_counts[name] += 1
                occupancy_y[y] += 1
                if occupied_min is None:
                    occupied_min = [x, y, z]
                    occupied_max = [x, y, z]
                else:
                    occupied_min[0] = min(occupied_min[0], x)
                    occupied_min[1] = min(occupied_min[1], y)
                    occupied_min[2] = min(occupied_min[2], z)
                    occupied_max[0] = max(occupied_max[0], x)
                    occupied_max[1] = max(occupied_max[1], y)
                    occupied_max[2] = max(occupied_max[2], z)
                top_state = state
                top_y = y
            render_state = slice_state if slice_y is not None else top_state
            if slice_y is not None and highlighted_air is not None:
                highlighted_index = (
                    ((slice_y - min_y) * depth + (z - min_z)) * width + (x - min_x)
                )
                if highlighted_index in highlighted_air:
                    pixels[x - min_x, z - min_z] = (0, 210, 255)
                    if top_y is not None:
                        surface_y.append(top_y)
                    continue
            if render_state is not None and _block_name(render_state) not in AIR_BLOCKS:
                base = _family_color(render_state)
                render_y = slice_y if slice_y is not None else top_y
                assert render_y is not None
                height_factor = 0.65 + 0.35 * (top_y - min_y) / max(1, max_y - min_y)
                if slice_y is not None:
                    height_factor = 1.0
                pixels[x - min_x, z - min_z] = tuple(
                    min(255, round(channel * height_factor)) for channel in base
                )
            if top_y is not None:
                surface_y.append(top_y)

    if scale > 1:
        image = image.resize((width * scale, depth * scale), Image.Resampling.NEAREST)
    png_path.parent.mkdir(parents=True, exist_ok=True)
    image.save(png_path)

    total_volume = width * (max_y - min_y + 1) * depth
    occupied = sum(name_counts.values())
    return {
        "bounds": {
            "min": [min_x, min_y, min_z],
            "max": [max_x, max_y, max_z],
            "size": [width, max_y - min_y + 1, depth],
        },
        "volume": total_volume,
        "non_air_blocks": occupied,
        "fill_ratio": occupied / total_volume if total_volume else 0.0,
        "occupied_extents": (
            {"min": occupied_min, "max": occupied_max}
            if occupied_min is not None
            else None
        ),
        "surface_y": (
            {
                "min": min(surface_y),
                "max": max(surface_y),
                "mean": sum(surface_y) / len(surface_y),
            }
            if surface_y
            else None
        ),
        "top_block_names": name_counts.most_common(40),
        "top_block_states": state_counts.most_common(40),
        "non_air_by_y": [[y, occupancy_y[y]] for y in sorted(occupancy_y)],
        "render_mode": "top" if slice_y is None else f"slice_y={slice_y}",
        "render": str(png_path.resolve()),
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--world", type=Path, required=True)
    parser.add_argument("--min-x", type=int, required=True)
    parser.add_argument("--min-y", type=int, required=True)
    parser.add_argument("--min-z", type=int, required=True)
    parser.add_argument("--max-x", type=int, required=True)
    parser.add_argument("--max-y", type=int, required=True)
    parser.add_argument("--max-z", type=int, required=True)
    parser.add_argument("--png", type=Path, required=True)
    parser.add_argument("--json", type=Path, required=True)
    parser.add_argument("--scale", type=int, default=4)
    parser.add_argument(
        "--slice-y",
        type=int,
        help="Render exactly this Y level instead of the highest non-air block",
    )
    parser.add_argument(
        "--air-seed",
        type=int,
        nargs=3,
        metavar=("X", "Y", "Z"),
        help="Flood-fill connected air from this position and add it to the report",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    bounds = (
        args.min_x,
        args.min_y,
        args.min_z,
        args.max_x,
        args.max_y,
        args.max_z,
    )
    if any(bounds[index] > bounds[index + 3] for index in range(3)):
        raise SystemExit("Each minimum bound must be <= its maximum")
    if args.scale < 1:
        raise SystemExit("--scale must be at least 1")
    if args.slice_y is not None and not args.min_y <= args.slice_y <= args.max_y:
        raise SystemExit("--slice-y must fall inside the requested Y bounds")

    world = AnvilWorld(args.world)
    air_report: dict[str, Any] | None = None
    highlighted_air: set[int] | None = None
    if args.air_seed is not None:
        air_report, highlighted_air = flood_air(
            world, bounds, tuple(args.air_seed)
        )
    report = scan_box(
        world,
        bounds,
        args.png,
        args.scale,
        args.slice_y,
        highlighted_air,
    )
    if air_report is not None:
        report["connected_air"] = air_report
    report["world"] = str(args.world.resolve())
    args.json.parent.mkdir(parents=True, exist_ok=True)
    args.json.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
