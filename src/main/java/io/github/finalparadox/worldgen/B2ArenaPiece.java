package io.github.finalparadox.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class B2ArenaPiece extends TemplateStructurePiece {
    public B2ArenaPiece(
            StructureTemplateManager templateManager,
            ResourceLocation template,
            BlockPos position
    ) {
        super(ModWorldgen.B2_ARENA_PIECE.get(), 0, templateManager,
                template, template.toString(), placementSettings(), position);
    }

    public B2ArenaPiece(
            StructureTemplateManager templateManager,
            CompoundTag tag
    ) {
        super(ModWorldgen.B2_ARENA_PIECE.get(), tag, templateManager,
                ignored -> placementSettings());
    }

    private static StructurePlaceSettings placementSettings() {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(true)
                .setKeepLiquids(false);
    }

    @Override
    protected void handleDataMarker(
            String marker,
            BlockPos position,
            ServerLevelAccessor level,
            RandomSource random,
            BoundingBox bounds
    ) {
        // The imported arena templates intentionally contain no data markers.
    }
}
