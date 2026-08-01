package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.runeforge.RuneforgeWorkbenchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<Block> RUNEFORGE_WORKBENCH = REGISTER.register("runeforge_workbench", () ->
            new RuneforgeWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.SMITHING_TABLE)));

    private ModBlocks() {
    }
}
