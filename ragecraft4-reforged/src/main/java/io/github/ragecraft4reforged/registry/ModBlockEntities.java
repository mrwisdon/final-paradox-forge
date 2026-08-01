package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.runeforge.RuneforgeWorkbenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<BlockEntityType<RuneforgeWorkbenchBlockEntity>> RUNEFORGE_WORKBENCH =
            REGISTER.register("runeforge_workbench", () -> BlockEntityType.Builder.of(
                    RuneforgeWorkbenchBlockEntity::new,
                    ModBlocks.RUNEFORGE_WORKBENCH.get()).build(null));

    private ModBlockEntities() {
    }
}
