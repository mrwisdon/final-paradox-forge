package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RuneforgeWorkbenchBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BASE_SLOT = 0;
    public static final int PREFIX_SLOT = 1;
    public static final int UPGRADE_SLOT = 2;
    public static final int SUFFIX_SLOT = 3;
    public static final int SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != BASE_SLOT && getStackInSlot(BASE_SLOT).isDamaged()) {
                return false;
            }
            return RuneforgeItemRules.isValidForSlot(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public RuneforgeWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNEFORGE_WORKBENCH.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ragecraft4reforged.runeforge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RuneforgeMenu(containerId, playerInventory, this);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> inventory);
    }
}
