package io.github.ragecraft4reforged.client;

import io.github.ragecraft4reforged.runeforge.RuneDefinition;
import io.github.ragecraft4reforged.runeforge.RuneProgression;
import io.github.ragecraft4reforged.runeforge.RuneforgeMenu;
import io.github.ragecraft4reforged.runeforge.RuneforgeRepairService;
import io.github.ragecraft4reforged.runeforge.PrefixSigilItem;
import io.github.ragecraft4reforged.runeforge.SuffixSigilItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class RuneforgeScreen extends AbstractContainerScreen<RuneforgeMenu> {
    private static final int PANEL = 0xFF17151D;
    private static final int PANEL_LIGHT = 0xFF282431;
    private static final int BORDER = 0xFF776A55;
    private static final int SLOT = 0xFF0B0A0F;
    private static final int LAPIS = 0xFF4D72FF;
    private static final int AMETHYST = 0xFF9A65E8;
    private static final int TEXT = 0xFFE7DFD0;
    private static final int MUTED = 0xFF9C95A5;

    private static final int CHOICE_WIDTH = 112;
    private static final int CHOICE_HEIGHT = 22;
    private static final int CHOICE_STEP = 25;
    private static final int CHOICE_X = 260;
    private static final int CHOICE_Y = 61;
    private static final int CHOICES_PER_PAGE = 4;

    private Button forgeButton;
    private Button unlockPowerButton;
    private Button prefixTabButton;
    private Button suffixTabButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private SelectorPage selectorPage = SelectorPage.PREFIX;
    private int choicePage;

    private enum SelectorPage {
        PREFIX,
        SUFFIX
    }

    public RuneforgeScreen(RuneforgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 392;
        imageHeight = 228;
        titleLabelX = 12;
        titleLabelY = 8;
        inventoryLabelX = 47;
        inventoryLabelY = 137;
    }

    @Override
    protected void init() {
        super.init();
        forgeButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.ragecraft4reforged.runeforge.forge"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(
                                menu.containerId, RuneforgeMenu.BUTTON_FORGE))
                .bounds(leftPos + 184, topPos + 113, 56, 18)
                .build());
        unlockPowerButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.ragecraft4reforged.rune_power.unlock"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(
                                menu.containerId, RuneforgeMenu.BUTTON_UNLOCK_POWER))
                .bounds(leftPos + 260, topPos + 181, 112, 18)
                .build());
        prefixTabButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.ragecraft4reforged.runeforge.prefix"),
                        button -> selectPage(SelectorPage.PREFIX))
                .bounds(leftPos + 260, topPos + 40, 54, 17)
                .build());
        suffixTabButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.ragecraft4reforged.runeforge.suffix"),
                        button -> selectPage(SelectorPage.SUFFIX))
                .bounds(leftPos + 318, topPos + 40, 54, 17)
                .build());
        previousPageButton = addRenderableWidget(Button.builder(
                        Component.literal("<"), button -> changeChoicePage(-1))
                .bounds(leftPos + 260, topPos + 160, 20, 17)
                .build());
        nextPageButton = addRenderableWidget(Button.builder(
                        Component.literal(">"), button -> changeChoicePage(1))
                .bounds(leftPos + 352, topPos + 160, 20, 17)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack base = menu.getBaseItem();
        forgeButton.active = !base.isEmpty() && (base.isDamaged() || menu.hasAnyRune());
        forgeButton.setMessage(Component.translatable(base.isDamaged()
                ? "gui.ragecraft4reforged.runeforge.repair"
                : "gui.ragecraft4reforged.runeforge.forge"));

        int power = menu.getRunePower();
        unlockPowerButton.active = power < RuneProgression.MAX_POWER;
        unlockPowerButton.setMessage(power >= RuneProgression.MAX_POWER
                ? Component.translatable("gui.ragecraft4reforged.rune_power.maximum")
                : Component.translatable("gui.ragecraft4reforged.rune_power.unlock_to", power + 1));

        boolean hasPrefixSigil = menu.getPrefixSlotItem().getItem() instanceof PrefixSigilItem;
        boolean hasSuffixSigil = menu.getSuffixSlotItem().getItem() instanceof SuffixSigilItem;
        if (selectorPage == SelectorPage.PREFIX && !hasPrefixSigil && hasSuffixSigil) {
            selectPage(SelectorPage.SUFFIX);
        } else if (selectorPage == SelectorPage.SUFFIX && !hasSuffixSigil && hasPrefixSigil) {
            selectPage(SelectorPage.PREFIX);
        }
        int pageCount = getChoicePageCount();
        if (choicePage >= pageCount) {
            choicePage = Math.max(0, pageCount - 1);
        }
        prefixTabButton.active = selectorPage != SelectorPage.PREFIX;
        suffixTabButton.active = selectorPage != SelectorPage.SUFFIX;
        boolean paged = getActiveChoices().size() > CHOICES_PER_PAGE;
        previousPageButton.visible = paged;
        nextPageButton.visible = paged;
        previousPageButton.active = choicePage > 0;
        nextPageButton.active = choicePage + 1 < pageCount;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        ItemStack preview = menu.getPreviewItem();
        if (!preview.isEmpty() && isHovering(191, 51, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, preview, mouseX, mouseY);
        }
        int hoveredChoice = choiceAt(mouseX, mouseY);
        if (hoveredChoice >= 0) {
            guiGraphics.renderTooltip(font, getChoiceStack(hoveredChoice), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF08070B);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, PANEL);
        outline(guiGraphics, x + 2, y + 2, imageWidth - 4, imageHeight - 4, BORDER);

        guiGraphics.fill(x + 12, y + 21, x + 147, y + 108, PANEL_LIGHT);
        outline(guiGraphics, x + 12, y + 21, 135, 87, 0xFF4E465A);
        drawConnections(guiGraphics, x, y);
        ItemStack base = menu.getBaseItem();
        boolean repairMode = base.isDamaged();
        drawSlot(guiGraphics, x + 30, y + 82, repairMode ? 0xFF39343E : AMETHYST);
        drawSlot(guiGraphics, x + 72, y + 38, repairMode ? 0xFF39343E : 0xFFB58C53);
        drawSlot(guiGraphics, x + 72, y + 82, 0xFF59B8C8);
        drawSlot(guiGraphics, x + 114, y + 82, repairMode ? 0xFF39343E : LAPIS);

        guiGraphics.fill(x + 154, y + 21, x + 242, y + 108, PANEL_LIGHT);
        outline(guiGraphics, x + 154, y + 21, 88, 87, 0xFF4E465A);
        drawSlot(guiGraphics, x + 191, y + 51, 0xFF59B8C8);
        ItemStack preview = menu.getPreviewItem();
        if (!preview.isEmpty()) {
            guiGraphics.renderItem(preview, x + 191, y + 51);
            guiGraphics.renderItemDecorations(font, preview, x + 191, y + 51);
        }

        guiGraphics.fill(x + 12, y + 112, x + 177, y + 133, PANEL_LIGHT);
        outline(guiGraphics, x + 12, y + 112, 165, 21, 0xFF4E465A);
        if (repairMode) {
            RuneforgeRepairService.Cost repairCost = menu.getRepairCost();
            if (repairCost.hasMaterialCost()) {
                guiGraphics.renderItem(repairCost.materialStack(), x + 18, y + 114);
            }
            guiGraphics.renderItem(new ItemStack(Items.EXPERIENCE_BOTTLE), x + 57, y + 114);
        } else {
            guiGraphics.renderItem(new ItemStack(Items.LAPIS_LAZULI), x + 18, y + 114);
            guiGraphics.renderItem(new ItemStack(Items.AMETHYST_SHARD), x + 57, y + 114);
            guiGraphics.fill(x + 134, y + 120, x + 170, y + 125, 0xFF0D0B12);
            guiGraphics.fill(x + 135, y + 121, x + 136, y + 124, AMETHYST);
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(guiGraphics, x + 47 + column * 18, y + 149 + row * 18, 0xFF3D3845);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(guiGraphics, x + 47 + column * 18, y + 207, 0xFF3D3845);
        }

        guiGraphics.fill(x + 250, y + 21, x + 380, y + 216, PANEL_LIGHT);
        outline(guiGraphics, x + 250, y + 21, 130, 195, 0xFF4E465A);
        renderChoices(guiGraphics, mouseX, mouseY);
        guiGraphics.renderItem(new ItemStack(Items.EMERALD_BLOCK), x + 260, y + 202);
    }

    private void renderChoices(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<RuneDefinition> choices = getActiveChoices();
        int start = choicePage * CHOICES_PER_PAGE;
        int end = Math.min(start + CHOICES_PER_PAGE, choices.size());
        for (int index = start; index < end; index++) {
            int cellX = leftPos + CHOICE_X;
            int cellY = topPos + CHOICE_Y + (index - start) * CHOICE_STEP;
            boolean hovered = mouseX >= cellX && mouseX < cellX + CHOICE_WIDTH
                    && mouseY >= cellY && mouseY < cellY + CHOICE_HEIGHT;
            boolean unlocked = isChoiceUnlocked(index);
            boolean selected = isChoiceSelected(index);
            int accent = selected ? 0xFFFFFFFF : unlocked ? 0xFF4D72FF : 0xFF4A4650;
            if (hovered && unlocked) {
                accent = 0xFF9FCBFF;
            }
            guiGraphics.fill(cellX, cellY, cellX + CHOICE_WIDTH, cellY + CHOICE_HEIGHT, accent);
            guiGraphics.fill(cellX + 1, cellY + 1,
                    cellX + CHOICE_WIDTH - 1, cellY + CHOICE_HEIGHT - 1, SLOT);
            ItemStack rune = getChoiceStack(index);
            guiGraphics.renderItem(rune, cellX + 2, cellY + 3);
            String name = font.plainSubstrByWidth(rune.getHoverName().getString(), 82);
            guiGraphics.drawString(font, name, cellX + 21, cellY + 3,
                    unlocked ? TEXT : MUTED, false);
            String power = Integer.toString(choices.get(index).runePower());
            guiGraphics.drawString(font, Component.translatable(
                            "gui.ragecraft4reforged.rune_power.short", power),
                    cellX + 21, cellY + 12, unlocked ? 0xFF74D98B : 0xFFFF7777, false);
            if (!unlocked) {
                guiGraphics.fill(cellX + 1, cellY + 1, cellX + 19, cellY + 21, 0x99000000);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
        guiGraphics.drawCenteredString(font, Component.translatable(
                "gui.ragecraft4reforged.runeforge.prefix"), 38, 68, AMETHYST);
        guiGraphics.drawCenteredString(font, Component.translatable(
                "gui.ragecraft4reforged.runeforge.upgrade"), 80, 27, 0xFFDBB477);
        guiGraphics.drawCenteredString(font, Component.translatable(
                "gui.ragecraft4reforged.runeforge.suffix"), 122, 68, LAPIS);
        guiGraphics.drawString(font, Component.translatable(
                "gui.ragecraft4reforged.runeforge.preview"), 160, 27, TEXT, false);

        ItemStack base = menu.getBaseItem();
        Component status;
        int statusColor;
        if (base.isEmpty()) {
            status = Component.translatable("gui.ragecraft4reforged.runeforge.status.empty");
            statusColor = MUTED;
        } else if (base.isDamaged()) {
            status = Component.translatable("gui.ragecraft4reforged.runeforge.status.repair");
            statusColor = 0xFFFFC45B;
        } else if (!menu.hasAnyRune()) {
            status = Component.translatable("gui.ragecraft4reforged.runeforge.status.rune");
            statusColor = 0xFF75C7D4;
        } else {
            status = Component.translatable("gui.ragecraft4reforged.runeforge.status.ready");
            statusColor = 0xFF74D98B;
        }
        guiGraphics.drawString(font, status, 160, 75, statusColor, false);
        if (base.isDamaged()) {
            RuneforgeRepairService.Cost repairCost = menu.getRepairCost();
            guiGraphics.drawString(font, repairCost.hasMaterialCost()
                    ? Integer.toString(repairCost.materialCount()) : "-", 37, 119, TEXT, false);
            guiGraphics.drawString(font, Integer.toString(repairCost.experiencePoints()), 76, 119, TEXT, false);
            guiGraphics.drawString(font, Component.translatable(
                    "gui.ragecraft4reforged.runeforge.repair_xp_short"), 101, 119, MUTED, false);
        } else {
            guiGraphics.drawString(font, Integer.toString(menu.getCost(
                    io.github.ragecraft4reforged.runeforge.RuneCurrency.LAPIS)), 37, 119, TEXT, false);
            guiGraphics.drawString(font, Integer.toString(menu.getCost(
                    io.github.ragecraft4reforged.runeforge.RuneCurrency.AMETHYST)), 76, 119, TEXT, false);
            guiGraphics.drawString(font, Component.translatable(
                    "gui.ragecraft4reforged.runeforge.power_short"), 96, 119, MUTED, false);
            guiGraphics.drawString(font, Integer.toString(menu.getRequiredPower()), 125, 119, TEXT, false);
        }

        guiGraphics.drawString(font, Component.translatable(
                "gui.ragecraft4reforged.rune_power.value", menu.getRunePower(), RuneProgression.MAX_POWER),
                260, 29, 0xFF74D98B, false);
        guiGraphics.drawString(font, Component.translatable(
                "gui.ragecraft4reforged.rune_power.cost"), 280, 206, MUTED, false);

        List<RuneDefinition> choices = getActiveChoices();
        if (choices.size() > CHOICES_PER_PAGE) {
            guiGraphics.drawCenteredString(font, Component.translatable(
                            "gui.ragecraft4reforged.rune_selector.page",
                            choicePage + 1, getChoicePageCount()),
                    316, 165, MUTED);
        }

        boolean prefix = selectorPage == SelectorPage.PREFIX;
        boolean hasSigil = prefix
                ? menu.getPrefixSlotItem().getItem() instanceof PrefixSigilItem
                : menu.getSuffixSlotItem().getItem() instanceof SuffixSigilItem;
        String keyBase = prefix ? "prefix_selector" : "suffix_selector";
        if (!hasSigil) {
            guiGraphics.drawWordWrap(font, Component.translatable(
                    "gui.ragecraft4reforged." + keyBase + ".insert_sigil"), 260, 72, 110, MUTED);
        } else if (menu.getBaseItem().isEmpty()) {
            guiGraphics.drawWordWrap(font, Component.translatable(
                    "gui.ragecraft4reforged." + keyBase + ".insert_equipment"), 260, 72, 110, MUTED);
        } else if (choices.isEmpty()) {
            guiGraphics.drawWordWrap(font, Component.translatable(
                    "gui.ragecraft4reforged." + keyBase + ".no_choices"), 260, 72, 110, MUTED);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int choice = choiceAt(mouseX, mouseY);
            if (choice >= 0 && minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId, (selectorPage == SelectorPage.PREFIX
                                ? RuneforgeMenu.BUTTON_PREFIX_START
                                : RuneforgeMenu.BUTTON_SUFFIX_START) + choice);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int choiceAt(double mouseX, double mouseY) {
        List<RuneDefinition> choices = getActiveChoices();
        int start = choicePage * CHOICES_PER_PAGE;
        int end = Math.min(start + CHOICES_PER_PAGE, choices.size());
        for (int index = start; index < end; index++) {
            int cellX = leftPos + CHOICE_X;
            int cellY = topPos + CHOICE_Y + (index - start) * CHOICE_STEP;
            if (mouseX >= cellX && mouseX < cellX + CHOICE_WIDTH
                    && mouseY >= cellY && mouseY < cellY + CHOICE_HEIGHT) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= leftPos + 250 && mouseX < leftPos + 380
                && mouseY >= topPos + 21 && mouseY < topPos + 180
                && getActiveChoices().size() > CHOICES_PER_PAGE) {
            changeChoicePage(delta > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void selectPage(SelectorPage page) {
        selectorPage = page;
        choicePage = 0;
    }

    private void changeChoicePage(int amount) {
        choicePage = Math.max(0, Math.min(getChoicePageCount() - 1, choicePage + amount));
    }

    private int getChoicePageCount() {
        return Math.max(1, (getActiveChoices().size() + CHOICES_PER_PAGE - 1) / CHOICES_PER_PAGE);
    }

    private List<RuneDefinition> getActiveChoices() {
        return selectorPage == SelectorPage.PREFIX ? menu.getPrefixChoices() : menu.getSuffixChoices();
    }

    private ItemStack getChoiceStack(int index) {
        return selectorPage == SelectorPage.PREFIX
                ? menu.getPrefixChoiceStack(index) : menu.getSuffixChoiceStack(index);
    }

    private boolean isChoiceUnlocked(int index) {
        return selectorPage == SelectorPage.PREFIX
                ? menu.isPrefixChoiceUnlocked(index) : menu.isSuffixChoiceUnlocked(index);
    }

    private boolean isChoiceSelected(int index) {
        return selectorPage == SelectorPage.PREFIX
                ? menu.isPrefixChoiceSelected(index) : menu.isSuffixChoiceSelected(index);
    }

    private static void drawConnections(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 48, y + 89, x + 72, y + 91, 0xFF645A72);
        guiGraphics.fill(x + 88, y + 89, x + 112, y + 91, 0xFF645A72);
        guiGraphics.fill(x + 79, y + 56, x + 81, y + 82, 0xFF645A72);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y, int accent) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, accent);
        guiGraphics.fill(x, y, x + 16, y + 16, SLOT);
    }

    private static void outline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
