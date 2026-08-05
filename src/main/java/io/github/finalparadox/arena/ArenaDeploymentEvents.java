package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.entity.B8EncounterManager;
import io.github.finalparadox.worldgen.ModWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArenaDeploymentEvents {
    private ArenaDeploymentEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (StructureStart start : event.getChunk().getAllStarts().values()) {
            if (!start.isValid()) continue;
            BoundingBox box = start.getBoundingBox();
            if (start.getStructure().type() == ModWorldgen.B1_ARENA.get()) {
                // minimumCorner = floorAnchor + (-44, -2, -55), so reverse it.
                BlockPos anchor = new BlockPos(box.minX() + 44, box.minY() + 2, box.minZ() + 55);
                adoptIfIdle(level, ArenaDefinitions.B1, anchor);
                return;
            }
            if (start.getStructure().type() == ModWorldgen.B8_ARENA.get()) {
                // minimumCorner = floorAnchor + (-23, -9, -24), so reverse it.
                BlockPos anchor = new BlockPos(box.minX() + 23, box.minY() + 9, box.minZ() + 24);
                adoptIfIdle(level, ArenaDefinitions.B8, anchor);
                return;
            }
            if (start.getStructure().type() == ModWorldgen.B5_ARENA.get()) {
                // minimumCorner = floorAnchor + (-64, -6, -28), so reverse it.
                BlockPos anchor = new BlockPos(box.minX() + 64, box.minY() + 6, box.minZ() + 28);
                adoptIfIdle(level, ArenaDefinitions.B5, anchor);
                return;
            }
            if (start.getStructure().type() == ModWorldgen.MARAWTHAR_ARENA.get()) {
                // minimumCorner = floorAnchor + (-64, -5, -64), so reverse it.
                BlockPos anchor = new BlockPos(box.minX() + 64, box.minY() + 5, box.minZ() + 64);
                adoptIfIdle(level, ArenaDefinitions.MARAWTHAR, anchor);
                return;
            }
        }
    }

    private static void adoptIfIdle(
            ServerLevel level,
            ArenaDefinition definition,
            BlockPos anchor
    ) {
        ArenaDeploymentData data = ArenaDeploymentData.get(level, definition);
        if (data.state() == ArenaDeploymentData.DeploymentState.IDLE) {
            data.adoptWorldgen(definition, anchor);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ArenaDeploymentManager.tick(level);
            B5EncounterManager.tick(level);
            B8EncounterManager.tick(level);
        }
    }
}
