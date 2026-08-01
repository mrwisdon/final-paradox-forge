package io.github.ragecraft4reforged.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public final class MechanicsWikiScreen extends Screen {
    private static final int BOOK = 0xFFE2D3AD;
    private static final int BOOK_DARK = 0xFFC5AE7C;
    private static final int INK = 0xFF32291D;
    private static final int MUTED_INK = 0xFF6C5B43;
    private static final int COVER = 0xFF102D5B;
    private static final int COVER_LIGHT = 0xFF1F4A83;
    private static final int GOLD = 0xFFD6B75B;

    private static final int MAX_WIDTH = 404;
    private static final int MAX_HEIGHT = 246;
    private static final int SIDEBAR_WIDTH = 112;
    private static final int CHAPTER_BUTTON_HEIGHT = 16;
    private static final int CHAPTER_BUTTON_STEP = 18;

    private static final List<Chapter> CHAPTERS = List.of(
            chapter("welcome", page("welcome", 3), page("navigation", 3)),
            chapter("runeforge", pageWithKeys("runeforge_basics",
                    "gui.ragecraft4reforged.mechanics_wiki.page.runeforge_basics.body.1",
                    "block.command_block.3.command.9",
                    "block.command_block.3.command.7",
                    "gui.ragecraft4reforged.mechanics_wiki.page.runeforge_basics.body.4"),
                    page("runeforge_slots", 4),
                    pageWithKeys("repairing",
                            "block.command_block.1.command.3",
                            "npc.functions.shade_nexus.cra_5.3",
                            "gui.ragecraft4reforged.mechanics_wiki.page.repairing.body.3",
                            "gui.ragecraft4reforged.mechanics_wiki.page.repairing.body.4")),
            chapter("prefix", page("prefix", 4)),
            chapter("upgrade", page("upgrade", 5)),
            chapter("suffix", page("suffix", 6)),
            chapter("rune_power", page("rune_power", 4)),
            chapter("equipment", page("wands", 5), page("wand_crafting", 4), page("accessories", 6)),
            chapter("potions", pageWithKeys("potions",
                    "item.lingering_potion.1.lore.0.1",
                    "gui.ragecraft4reforged.mechanics_wiki.page.potions.body.2",
                    "item.lingering_potion.1.lore.5.1",
                    "item.lingering_potion.1.lore.6.1",
                    "item.lingering_potion.1.lore.7.1",
                    "gui.ragecraft4reforged.mechanics_wiki.page.potions.body.5"), pageWithKeys("elixirs",
                    "item.potion.2.name.1", "item.potion.2.lore.1.1",
                    "item.potion.35.name.1", "item.potion.35.lore.1.1",
                    "item.potion.70.name.1", "item.potion.70.lore.1.1",
                    "item.potion.107.name.1", "item.potion.107.lore.1.1",
                    "item.potion.107.lore.2.1")),
            chapter("enchantments", pageWithKeys("enchantments_melee",
                    "general.functions.encyclopedia.dis_decapitate2.1",
                    "general.functions.encyclopedia.dis_decapitate2.2",
                    "general.functions.encyclopedia.dis_life_leech2.1",
                    "general.functions.encyclopedia.dis_life_leech2.2",
                    "general.functions.encyclopedia.dis_mana_leech2.1",
                    "general.functions.encyclopedia.dis_mana_leech2.2",
                    "general.functions.encyclopedia.dis_slam2.1",
                    "general.functions.encyclopedia.dis_slam2.2"), pageWithKeys("enchantments_ranged",
                    "general.functions.encyclopedia.dis_sharpshot2.1",
                    "general.functions.encyclopedia.dis_sharpshot2.2",
                    "general.functions.encyclopedia.dis_trueshot2.1",
                    "general.functions.encyclopedia.dis_trueshot2.2",
                    "general.functions.encyclopedia.dis_volley2.1",
                    "general.functions.encyclopedia.dis_volley2.2",
                    "general.functions.encyclopedia.dis_intellect2.1",
                    "general.functions.encyclopedia.dis_intellect2.2")),
            chapter("effects", pageWithKeys("effects",
                    "general.functions.encyclopedia.dis_decay2.1",
                    "general.functions.encyclopedia.dis_decay2.2",
                    "general.functions.encyclopedia.dis_flammability2.1",
                    "general.functions.encyclopedia.dis_flammability2.2",
                    "general.functions.encyclopedia.dis_vulnerability2.1",
                    "general.functions.encyclopedia.dis_vulnerability2.2"), pageWithKeys("effects_combat",
                    "general.functions.encyclopedia.dis_slice2.1",
                    "general.functions.encyclopedia.dis_slice2.2",
                    "general.functions.encyclopedia.dis_mana_leech2.1",
                    "general.functions.encyclopedia.dis_mana_leech2.2",
                    "general.functions.encyclopedia.dis_intellect2.1",
                    "general.functions.encyclopedia.dis_intellect2.2",
                    "general.functions.encyclopedia.dis_volley2.1",
                    "general.functions.encyclopedia.dis_volley2.2"))
    );

    private int chapterIndex;
    private int pageIndex;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int scrollOffset;
    private int maxScroll;
    private Button previousButton;
    private Button nextButton;

    public MechanicsWikiScreen() {
        super(Component.translatable("gui.ragecraft4reforged.mechanics_wiki.title"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(MAX_WIDTH, width - 20);
        panelHeight = Math.min(MAX_HEIGHT, height - 20);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        int chapterX = panelX + 8;
        int chapterY = panelY + 32;
        int chapterWidth = Math.min(SIDEBAR_WIDTH - 16, panelWidth / 3);
        for (int index = 0; index < CHAPTERS.size(); index++) {
            int selectedIndex = index;
            addRenderableWidget(Button.builder(
                            Component.translatable(CHAPTERS.get(index).titleKey()),
                            button -> selectChapter(selectedIndex))
                    .bounds(chapterX, chapterY + index * CHAPTER_BUTTON_STEP,
                            chapterWidth, CHAPTER_BUTTON_HEIGHT)
                    .build());
        }

        int navigationY = panelY + panelHeight - 26;
        previousButton = addRenderableWidget(Button.builder(
                        Component.literal("<"), button -> changePage(-1))
                .bounds(panelX + SIDEBAR_WIDTH + 12, navigationY, 24, 18)
                .build());
        nextButton = addRenderableWidget(Button.builder(
                        Component.literal(">"), button -> changePage(1))
                .bounds(panelX + panelWidth - 36, navigationY, 24, 18)
                .build());
        updateNavigationButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawBook(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPage(graphics);
    }

    private void drawBook(GuiGraphics graphics) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF080E19);
        graphics.fill(panelX + 2, panelY + 2, panelX + SIDEBAR_WIDTH, panelY + panelHeight - 2, COVER);
        graphics.fill(panelX + 5, panelY + 5, panelX + SIDEBAR_WIDTH - 3, panelY + panelHeight - 5, COVER_LIGHT);
        graphics.fill(panelX + SIDEBAR_WIDTH, panelY + 2, panelX + panelWidth - 2,
                panelY + panelHeight - 2, BOOK);
        graphics.fill(panelX + SIDEBAR_WIDTH, panelY + 4, panelX + SIDEBAR_WIDTH + 2,
                panelY + panelHeight - 4, BOOK_DARK);
        graphics.fill(panelX + panelWidth - 5, panelY + 5, panelX + panelWidth - 3,
                panelY + panelHeight - 5, BOOK_DARK);

        graphics.drawCenteredString(font, title, panelX + SIDEBAR_WIDTH / 2, panelY + 10, GOLD);
        graphics.drawString(font, Component.translatable("gui.ragecraft4reforged.mechanics_wiki.contents"),
                panelX + 10, panelY + 22, 0xFFDAE8FF, false);
    }

    private void renderPage(GuiGraphics graphics) {
        Chapter chapter = CHAPTERS.get(chapterIndex);
        WikiPage page = chapter.pages().get(pageIndex);
        int contentX = panelX + SIDEBAR_WIDTH + 16;
        int contentWidth = panelWidth - SIDEBAR_WIDTH - 32;
        int contentTop = panelY + 12;
        int contentBottom = panelY + panelHeight - 31;
        int y = contentTop;

        graphics.enableScissor(contentX, contentTop, contentX + contentWidth, contentBottom);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -scrollOffset, 0);
        graphics.drawString(font, Component.translatable(page.titleKey()), contentX, y, INK, false);
        graphics.fill(contentX, y + 11, contentX + contentWidth, y + 12, BOOK_DARK);
        y += 19;

        for (String bodyKey : page.bodyKeys()) {
            Component paragraph = Component.translatable(bodyKey);
            graphics.drawWordWrap(font, paragraph, contentX, y, contentWidth, INK);
            y += font.split(paragraph, contentWidth).size() * 9 + 7;
        }
        graphics.pose().popPose();
        graphics.disableScissor();
        maxScroll = Math.max(0, y - contentTop - (contentBottom - contentTop));
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        graphics.drawCenteredString(font,
                Component.translatable("gui.ragecraft4reforged.mechanics_wiki.page",
                        pageIndex + 1, chapter.pages().size()),
                panelX + SIDEBAR_WIDTH + (panelWidth - SIDEBAR_WIDTH) / 2,
                panelY + panelHeight - 21, MUTED_INK);
    }

    private void selectChapter(int index) {
        chapterIndex = Mth.clamp(index, 0, CHAPTERS.size() - 1);
        pageIndex = 0;
        scrollOffset = 0;
        updateNavigationButtons();
    }

    private void changePage(int amount) {
        pageIndex = Mth.clamp(pageIndex + amount, 0, CHAPTERS.get(chapterIndex).pages().size() - 1);
        scrollOffset = 0;
        updateNavigationButtons();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= panelX + SIDEBAR_WIDTH && mouseX < panelX + panelWidth
                && mouseY >= panelY && mouseY < panelY + panelHeight && maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset + (delta > 0 ? -18 : 18), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateNavigationButtons() {
        if (previousButton == null || nextButton == null) {
            return;
        }
        int pageCount = CHAPTERS.get(chapterIndex).pages().size();
        previousButton.active = pageIndex > 0;
        nextButton.active = pageIndex + 1 < pageCount;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Chapter chapter(String id, WikiPage... pages) {
        return new Chapter("gui.ragecraft4reforged.mechanics_wiki.chapter." + id, List.of(pages));
    }

    private static WikiPage page(String id, int paragraphCount) {
        List<String> bodies = java.util.stream.IntStream.rangeClosed(1, paragraphCount)
                .mapToObj(index -> "gui.ragecraft4reforged.mechanics_wiki.page." + id + ".body." + index)
                .toList();
        return new WikiPage("gui.ragecraft4reforged.mechanics_wiki.page." + id + ".title", bodies);
    }

    private static WikiPage pageWithKeys(String id, String... bodyKeys) {
        return new WikiPage("gui.ragecraft4reforged.mechanics_wiki.page." + id + ".title",
                List.of(bodyKeys));
    }

    private record Chapter(String titleKey, List<WikiPage> pages) {
    }

    private record WikiPage(String titleKey, List<String> bodyKeys) {
    }
}
