package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.registry.ModBlocks;
import io.github.ragecraft4reforged.registry.ModItems;
import io.github.ragecraft4reforged.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Comparator;
import java.util.List;

public final class RuneforgeMenu extends AbstractContainerMenu {
    public static final int BUTTON_FORGE = 0;
    public static final int BUTTON_UNLOCK_POWER = 1;
    public static final int BUTTON_PREFIX_START = 100;
    public static final int BUTTON_SUFFIX_START = 1000;

    private static final int WORKBENCH_SLOT_COUNT = RuneforgeWorkbenchBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = WORKBENCH_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_SLOT_END = PLAYER_HOTBAR_START + 9;

    private final RuneforgeWorkbenchBlockEntity workbench;
    private final ContainerLevelAccess access;
    private final Inventory playerInventory;
    private int selectedPrefixSourceId = -1;
    private int selectedSuffixSourceId = -1;
    private int syncedRunePower;

    public RuneforgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getWorkbench(playerInventory, data.readBlockPos()));
    }

    public RuneforgeMenu(int containerId, Inventory playerInventory, RuneforgeWorkbenchBlockEntity workbench) {
        super(ModMenus.RUNEFORGE.get(), containerId);
        this.workbench = workbench;
        this.playerInventory = playerInventory;
        this.access = ContainerLevelAccess.create(workbench.getLevel(), workbench.getBlockPos());

        addSlot(new SlotItemHandler(workbench.getInventory(), RuneforgeWorkbenchBlockEntity.BASE_SLOT, 72, 82));
        addRuneSlot(RuneforgeWorkbenchBlockEntity.PREFIX_SLOT, 30, 82);
        addRuneSlot(RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT, 72, 38);
        addRuneSlot(RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT, 114, 82);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 47 + column * 18, 149 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 47 + column * 18, 207));
        }

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return playerInventory.player instanceof ServerPlayer
                        ? RuneProgression.get(playerInventory.player) : syncedRunePower;
            }

            @Override
            public void set(int value) {
                syncedRunePower = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return selectedPrefixSourceId;
            }

            @Override
            public void set(int value) {
                selectedPrefixSourceId = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return selectedSuffixSourceId;
            }

            @Override
            public void set(int value) {
                selectedSuffixSourceId = value;
            }
        });
    }

    private void addRuneSlot(int slot, int x, int y) {
        addSlot(new SlotItemHandler(workbench.getInventory(), slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (getBaseItem().isDamaged() || !super.mayPlace(stack)) {
                    return false;
                }
                if (stack.getItem() instanceof RuneItem rune && !getBaseItem().isEmpty()) {
                    return rune.isCompatible(getBaseItem()) && !RuneforgeData.has(getBaseItem(), rune.getCategory());
                }
                return true;
            }
        });
    }

    private static RuneforgeWorkbenchBlockEntity getWorkbench(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RuneforgeWorkbenchBlockEntity workbench) {
            return workbench;
        }
        throw new IllegalStateException("Runeforge menu opened without a runeforge workbench at " + pos);
    }

    public ItemStack getBaseItem() {
        return workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT);
    }

    public ItemStack getSuffixSlotItem() {
        return workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT);
    }

    public ItemStack getPrefixSlotItem() {
        return workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.PREFIX_SLOT);
    }

    public int getRunePower() {
        return playerInventory.player instanceof ServerPlayer
                ? RuneProgression.get(playerInventory.player) : syncedRunePower;
    }

    public List<RuneDefinition> getSuffixChoices() {
        ItemStack sigilStack = getSuffixSlotItem();
        ItemStack base = getBaseItem();
        if (!(sigilStack.getItem() instanceof SuffixSigilItem sigil)
                || base.isEmpty() || base.isDamaged() || RuneforgeData.has(base, RuneCategory.SUFFIX)) {
            return List.of();
        }
        return RuneCatalog.all().stream()
                .filter(definition -> definition.category() == RuneCategory.SUFFIX)
                .filter(definition -> SuffixSchool.fromDefinition(definition) == sigil.school())
                .filter(definition -> definition.matches(base))
                .sorted(Comparator.comparingInt(RuneDefinition::runePower)
                        .thenComparingInt(RuneDefinition::sourceId))
                .toList();
    }

    public List<RuneDefinition> getPrefixChoices() {
        ItemStack sigilStack = getPrefixSlotItem();
        ItemStack base = getBaseItem();
        if (!(sigilStack.getItem() instanceof PrefixSigilItem sigil)
                || base.isEmpty() || base.isDamaged() || !sigil.tool().matches(base)
                || RuneforgeData.has(base, RuneCategory.PREFIX)) {
            return List.of();
        }
        return RuneCatalog.all().stream()
                .filter(definition -> definition.category() == RuneCategory.PREFIX)
                .filter(definition -> definition.matches(base))
                .sorted(Comparator.comparingInt(RuneDefinition::runePower)
                        .thenComparingInt(RuneDefinition::sourceId))
                .toList();
    }

    public ItemStack getPrefixChoiceStack(int index) {
        List<RuneDefinition> choices = getPrefixChoices();
        if (index < 0 || index >= choices.size()) {
            return ItemStack.EMPTY;
        }
        return ModItems.runeItem(choices.get(index).registryName()).getDefaultInstance();
    }

    public ItemStack getSuffixChoiceStack(int index) {
        List<RuneDefinition> choices = getSuffixChoices();
        if (index < 0 || index >= choices.size()) {
            return ItemStack.EMPTY;
        }
        return ModItems.runeItem(choices.get(index).registryName()).getDefaultInstance();
    }

    public boolean isSuffixChoiceUnlocked(int index) {
        List<RuneDefinition> choices = getSuffixChoices();
        return index >= 0 && index < choices.size()
                && choices.get(index).runePower() <= getRunePower();
    }

    public boolean isSuffixChoiceSelected(int index) {
        List<RuneDefinition> choices = getSuffixChoices();
        return index >= 0 && index < choices.size()
                && choices.get(index).sourceId() == selectedSuffixSourceId;
    }

    public boolean isPrefixChoiceUnlocked(int index) {
        List<RuneDefinition> choices = getPrefixChoices();
        return index >= 0 && index < choices.size()
                && choices.get(index).runePower() <= getRunePower();
    }

    public boolean isPrefixChoiceSelected(int index) {
        List<RuneDefinition> choices = getPrefixChoices();
        return index >= 0 && index < choices.size()
                && choices.get(index).sourceId() == selectedPrefixSourceId;
    }

    public RuneDefinition getSelectedPrefixDefinition() {
        return getPrefixChoices().stream()
                .filter(definition -> definition.sourceId() == selectedPrefixSourceId)
                .filter(definition -> definition.runePower() <= getRunePower())
                .findFirst()
                .orElse(null);
    }

    public RuneDefinition getSelectedSuffixDefinition() {
        return getSuffixChoices().stream()
                .filter(definition -> definition.sourceId() == selectedSuffixSourceId)
                .filter(definition -> definition.runePower() <= getRunePower())
                .findFirst()
                .orElse(null);
    }

    private RuneItem getSelectedSuffixRune() {
        RuneDefinition definition = getSelectedSuffixDefinition();
        if (definition == null) {
            return null;
        }
        return (RuneItem) ModItems.runeItem(definition.registryName());
    }

    private RuneItem getSelectedPrefixRune() {
        RuneDefinition definition = getSelectedPrefixDefinition();
        if (definition == null) {
            return null;
        }
        return (RuneItem) ModItems.runeItem(definition.registryName());
    }

    public boolean hasAnyRune() {
        return workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.PREFIX_SLOT)
                        .getItem() instanceof RuneItem
                || workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT)
                        .getItem() instanceof RuneItem
                || workbench.getInventory().getStackInSlot(RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT)
                        .getItem() instanceof RuneItem
                || getSelectedPrefixDefinition() != null
                || getSelectedSuffixDefinition() != null;
    }

    public ItemStack getPreviewItem() {
        if (getBaseItem().isDamaged()) {
            return RuneforgeRepairService.preview(getBaseItem());
        }
        return RuneforgeService.preview(getBaseItem(), workbench.getInventory(),
                getSelectedPrefixRune(), getSelectedSuffixRune());
    }

    public RuneforgeRepairService.Cost getRepairCost() {
        return RuneforgeRepairService.cost(getBaseItem());
    }

    public int getCost(RuneCurrency currency) {
        return RuneforgeService.cost(workbench.getInventory(), currency,
                getSelectedPrefixRune(), getSelectedSuffixRune());
    }

    public int getRequiredPower() {
        return RuneforgeService.requiredPower(workbench.getInventory(),
                getSelectedPrefixRune(), getSelectedSuffixRune());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        if (buttonId == BUTTON_UNLOCK_POWER) {
            RuneProgression.UnlockResult result = RuneProgression.unlockNext(serverPlayer);
            player.displayClientMessage(RuneProgression.resultMessage(result, RuneProgression.get(player)), true);
            broadcastChanges();
            return true;
        }

        if (buttonId >= BUTTON_SUFFIX_START) {
            int choiceIndex = buttonId - BUTTON_SUFFIX_START;
            List<RuneDefinition> choices = getSuffixChoices();
            if (choiceIndex < 0 || choiceIndex >= choices.size()) {
                return false;
            }
            RuneDefinition selected = choices.get(choiceIndex);
            if (selected.runePower() > RuneProgression.get(player)) {
                player.displayClientMessage(Component.translatable(
                        "message.ragecraft4reforged.rune_power.locked", selected.runePower()), true);
                return false;
            }
            selectedSuffixSourceId = selected.sourceId();
            player.displayClientMessage(Component.translatable(
                    "message.ragecraft4reforged.sigil.selected",
                    ModItems.runeItem(selected.registryName()).getDescription()), true);
            broadcastChanges();
            return true;
        }

        if (buttonId >= BUTTON_PREFIX_START) {
            int choiceIndex = buttonId - BUTTON_PREFIX_START;
            List<RuneDefinition> choices = getPrefixChoices();
            if (choiceIndex < 0 || choiceIndex >= choices.size()) {
                return false;
            }
            RuneDefinition selected = choices.get(choiceIndex);
            if (selected.runePower() > RuneProgression.get(player)) {
                player.displayClientMessage(Component.translatable(
                        "message.ragecraft4reforged.rune_power.locked", selected.runePower()), true);
                return false;
            }
            selectedPrefixSourceId = selected.sourceId();
            player.displayClientMessage(Component.translatable(
                    "message.ragecraft4reforged.sigil.selected",
                    ModItems.runeItem(selected.registryName()).getDescription()), true);
            broadcastChanges();
            return true;
        }

        if (buttonId != BUTTON_FORGE || getBaseItem().isEmpty()) {
            return false;
        }
        if (getBaseItem().isDamaged()) {
            RuneforgeRepairService.Result result = RuneforgeRepairService.repair(serverPlayer, workbench);
            player.displayClientMessage(result.message(), true);
            broadcastChanges();
        } else {
            RuneforgeService.Result result = RuneforgeService.forge(
                    serverPlayer, workbench, getSelectedPrefixRune(), getSelectedSuffixRune());
            player.displayClientMessage(result.message(), true);
            broadcastChanges();
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (playerInventory.player instanceof ServerPlayer && selectedPrefixSourceId != -1
                && getSelectedPrefixDefinition() == null) {
            selectedPrefixSourceId = -1;
        }
        if (playerInventory.player instanceof ServerPlayer && selectedSuffixSourceId != -1
                && getSelectedSuffixDefinition() == null) {
            selectedSuffixSourceId = -1;
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < WORKBENCH_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (RuneforgeItemRules.isValidForSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT, stack)) {
            if (!moveItemStackTo(stack, RuneforgeWorkbenchBlockEntity.BASE_SLOT,
                    RuneforgeWorkbenchBlockEntity.BASE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (RuneforgeItemRules.isValidForSlot(RuneforgeWorkbenchBlockEntity.PREFIX_SLOT, stack)) {
            if (!moveItemStackTo(stack, RuneforgeWorkbenchBlockEntity.PREFIX_SLOT,
                    RuneforgeWorkbenchBlockEntity.PREFIX_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (RuneforgeItemRules.isValidForSlot(RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT, stack)) {
            if (!moveItemStackTo(stack, RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT,
                    RuneforgeWorkbenchBlockEntity.UPGRADE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (RuneforgeItemRules.isValidForSlot(RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT, stack)) {
            if (!moveItemStackTo(stack, RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT,
                    RuneforgeWorkbenchBlockEntity.SUFFIX_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_HOTBAR_START) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RUNEFORGE_WORKBENCH.get());
    }
}
