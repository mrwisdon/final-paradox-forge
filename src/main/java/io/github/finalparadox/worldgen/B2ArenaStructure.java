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
 * A flat, terrain-checked arena structure assembled from the same templates
 * as the B2 Thar Kroo arena.
 */
public final class B2ArenaStructure extends Structure {
    public static final Codec<B2ArenaStructure> CODEC =
            simpleCodec(B2ArenaStructure::new);

    private static final int MAX_RELIEF = 8;
    private static final int MAX_SURFACE_DEPTH = 3;

    public B2ArenaStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int centerX = chunk.getMiddleBlockX();
        int centerZ = chunk.getMiddleBlockZ();
        int radius = ArenaDefinitions.B2.size().getX() / 2;

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

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());
        // The B2 export spans Y 48..64 around the source anchor Y 53.5. In the
        // flat arena dimension the surface is at Y 64, so lower the floor anchor.
        int floorY = surfaceY - 11;
        ArenaDefinition definition = ArenaDefinitions.B2;
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
                builder.addPiece(new B2ArenaPiece(
                        context.structureTemplateManager(),
                        tile.template(),
                        minimumCorner.offset(tile.offset())));
            }
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.B2_ARENA.get();
    }
}
