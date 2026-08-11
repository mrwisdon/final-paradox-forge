package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaSlots;
import io.github.finalparadox.block.ArenaSandboxBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FinalParadox.MOD_ID);

    public static final RegistryObject<Block> APIGLO_ARENA_SANDBOX = sandbox(
            ArenaSlots.B1.sandboxId(), ArenaDefinitions.B1.id());
    public static final RegistryObject<Block> THAR_KROO_ARENA_SANDBOX = sandbox(
            ArenaSlots.B2.sandboxId(), ArenaDefinitions.B2.id());
    public static final RegistryObject<Block> KOYOMI_GARIHEUZ_ARENA_SANDBOX = sandbox(
            ArenaSlots.B5.sandboxId(), ArenaDefinitions.B5.id());
    public static final RegistryObject<Block> ZOMBIE_SUPERMATRIX_ARENA_SANDBOX = sandbox(
            ArenaSlots.B8.sandboxId(), ArenaDefinitions.B8.id());
    public static final RegistryObject<Block> MARAWTHAR_ARENA_SANDBOX = sandbox(
            ArenaSlots.MARAWTHAR.sandboxId(), ArenaDefinitions.MARAWTHAR.id());

    private ModBlocks() {
    }

    private static RegistryObject<Block> sandbox(String registryName, String arenaId) {
        return REGISTER.register(registryName, () -> new ArenaSandboxBlock(
                arenaId,
                BlockBehaviour.Properties.copy(Blocks.SPRUCE_PLANKS)
                        .strength(2.0F, 3.0F)
                        .noOcclusion()));
    }
}
