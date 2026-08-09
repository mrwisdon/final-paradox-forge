package io.github.finalparadox.worldgen;

import com.mojang.serialization.Codec;
import io.github.finalparadox.arena.ArenaDefinition;
import io.github.finalparadox.arena.ArenaDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * A rare, terrain-checked Overworld structure assembled from the same two
 * templates as the manual B8 arena deployment command.
 */
public final class B8ArenaStructure extends Structure {
    public static final Codec<B8ArenaStructure> CODEC =
            simpleCodec(B8ArenaStructure::new);

    private static final int MAX_RELIEF = 8;
    private static final int MAX_SURFACE_DEPTH = 3;

    public B8ArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int centerX = chunk.getMiddleBlockX();
        int centerZ = chunk.getMiddleBlockZ();
        int radius = ArenaDefinitions.B8.size().getX() / 2;

        int minimumHeight = Integer.MAX_VALUE;
        int maximumHeight = Integer.MIN_VALUE;
        int[] offsets = {-radius, 0, radius};
        for (int offsetX : offsets) {
            for (int offsetZ : offsets) {
                int x = centerX + offsetX;
                int z = centerZ + offsetZ;
                int ground = context.chunkGenerator().getFirstOccupiedHeight(
                        x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                        context.heightAccessor(), context.randomState());
                int surface = context.chunkGenerator().getFirstOccupiedHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
                if (surface - ground > MAX_SURFACE_DEPTH) {
                    return Optional.empty();
                }
                minimumHeight = Math.min(minimumHeight, ground);
                maximumHeight = Math.max(maximumHeight, ground);
            }
        }
        if (maximumHeight - minimumHeight > MAX_RELIEF) {
            return Optional.empty();
        }

        int floorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());
        ArenaDefinition definition = ArenaDefinitions.B8;
        BlockPos floorAnchor = new BlockPos(centerX, floorY, centerZ);
        BlockPos minimumCorner = definition.minimumCorner(floorAnchor);
        if (minimumCorner.getY() < context.heightAccessor().getMinBuildHeight()
                || definition.maximumCorner(floorAnchor).getY()
                >= context.heightAccessor().getMaxBuildHeight()) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(floorAnchor, builder -> {
            for (int index = 0; index < definition.tileCount(); index++) {
                ArenaDefinition.ArenaTile tile = definition.tile(index);
                builder.addPiece(new B8ArenaPiece(
                        context.structureTemplateManager(),
                        tile.template(),
                        minimumCorner.offset(tile.offset())));
            }
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.B8_ARENA.get();
    }
}
