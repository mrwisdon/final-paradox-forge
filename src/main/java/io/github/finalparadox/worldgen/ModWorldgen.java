package io.github.finalparadox.worldgen;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModWorldgen {
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, FinalParadox.MOD_ID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, FinalParadox.MOD_ID);

    public static final RegistryObject<StructureType<MarawTharArenaStructure>> MARAWTHAR_ARENA =
            STRUCTURE_TYPES.register("marawthar_arena",
                    () -> () -> MarawTharArenaStructure.CODEC);

    public static final RegistryObject<StructurePieceType> MARAWTHAR_ARENA_PIECE =
            STRUCTURE_PIECES.register("marawthar_arena",
                    () -> (StructurePieceType.StructureTemplateType) MarawTharArenaPiece::new);

    public static final RegistryObject<StructureType<B8ArenaStructure>> B8_ARENA =
            STRUCTURE_TYPES.register("b8_arena",
                    () -> () -> B8ArenaStructure.CODEC);

    public static final RegistryObject<StructurePieceType> B8_ARENA_PIECE =
            STRUCTURE_PIECES.register("b8_arena",
                    () -> (StructurePieceType.StructureTemplateType) B8ArenaPiece::new);

    public static final RegistryObject<StructureType<B5ArenaStructure>> B5_ARENA =
            STRUCTURE_TYPES.register("b5_arena",
                    () -> () -> B5ArenaStructure.CODEC);

    public static final RegistryObject<StructurePieceType> B5_ARENA_PIECE =
            STRUCTURE_PIECES.register("b5_arena",
                    () -> (StructurePieceType.StructureTemplateType) B5ArenaPiece::new);

    public static final RegistryObject<StructureType<B1ArenaStructure>> B1_ARENA =
            STRUCTURE_TYPES.register("b1_arena",
                    () -> () -> B1ArenaStructure.CODEC);

    public static final RegistryObject<StructurePieceType> B1_ARENA_PIECE =
            STRUCTURE_PIECES.register("b1_arena",
                    () -> (StructurePieceType.StructureTemplateType) B1ArenaPiece::new);

    public static final RegistryObject<StructureType<B2ArenaStructure>> B2_ARENA =
            STRUCTURE_TYPES.register("b2_arena",
                    () -> () -> B2ArenaStructure.CODEC);

    public static final RegistryObject<StructurePieceType> B2_ARENA_PIECE =
            STRUCTURE_PIECES.register("b2_arena",
                    () -> (StructurePieceType.StructureTemplateType) B2ArenaPiece::new);

    private ModWorldgen() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }
}
