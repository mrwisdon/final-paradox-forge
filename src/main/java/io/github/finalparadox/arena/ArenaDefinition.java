package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

public record ArenaDefinition(
        String id,
        String structurePath,
        Vec3i size,
        BlockPos sourceMinOffset,
        int tileSize,
        int tilesX,
        int tilesY,
        int tilesZ,
        Vec3i bossSpawnOffset
) {
    public ArenaDefinition {
        if (id.isBlank() || structurePath.isBlank()) throw new IllegalArgumentException("Arena id and path are required");
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) throw new IllegalArgumentException("Arena size must be positive");
        if (tileSize <= 0 || tilesX <= 0 || tilesY <= 0 || tilesZ <= 0) throw new IllegalArgumentException("Invalid arena tile grid");
        if (tilesX != divideRoundUp(size.getX(), tileSize)
                || tilesY != divideRoundUp(size.getY(), tileSize)
                || tilesZ != divideRoundUp(size.getZ(), tileSize)) {
            throw new IllegalArgumentException("Arena tile grid does not cover the declared size");
        }
    }

    public int tileCount() {
        return tilesX * tilesY * tilesZ;
    }

    public ArenaTile tile(int index) {
        if (index < 0 || index >= tileCount()) throw new IndexOutOfBoundsException("Arena tile " + index);
        int tilesPerLayer = tilesX * tilesZ;
        int tileY = index / tilesPerLayer;
        int layerIndex = index % tilesPerLayer;
        int tileZ = layerIndex / tilesX;
        int tileX = layerIndex % tilesX;

        int offsetX = tileX * tileSize;
        int offsetY = tileY * tileSize;
        int offsetZ = tileZ * tileSize;
        Vec3i expectedSize = new Vec3i(
                Math.min(tileSize, size.getX() - offsetX),
                Math.min(tileSize, size.getY() - offsetY),
                Math.min(tileSize, size.getZ() - offsetZ));
        ResourceLocation template = ResourceLocation.fromNamespaceAndPath(
                FinalParadox.MOD_ID,
                structurePath + "/tile_" + tileX + "_" + tileY + "_" + tileZ);
        return new ArenaTile(template, new BlockPos(offsetX, offsetY, offsetZ), expectedSize);
    }

    public BlockPos minimumCorner(BlockPos floorAnchor) {
        return floorAnchor.offset(sourceMinOffset);
    }

    public BlockPos maximumCorner(BlockPos floorAnchor) {
        return minimumCorner(floorAnchor).offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
    }

    public BlockPos bossSpawnBlock(BlockPos floorAnchor) {
        return floorAnchor.offset(bossSpawnOffset);
    }

    private static int divideRoundUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    public record ArenaTile(ResourceLocation template, BlockPos offset, Vec3i expectedSize) {
    }
}
