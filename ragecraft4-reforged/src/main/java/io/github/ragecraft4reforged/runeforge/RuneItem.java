package io.github.ragecraft4reforged.runeforge;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class RuneItem extends Item {
    private final RuneDefinition definition;

    public RuneItem(RuneDefinition definition) {
        super(new Item.Properties().stacksTo(1));
        this.definition = definition;
    }

    public RuneCategory getCategory() {
        return definition.category();
    }

    public int getRunePower() {
        return definition.runePower();
    }

    public int getCost() {
        return definition.cost();
    }

    public RuneCurrency getCurrency() {
        return definition.currency();
    }

    public RuneDefinition getDefinition() {
        return definition;
    }

    public boolean isCompatible(ItemStack stack) {
        return definition.matches(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component component = Component.Serializer.fromJson(definition.nameJson());
        return component == null ? super.getName(stack) : component;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String json : definition.loreJson()) {
            Component component = Component.Serializer.fromJson(json);
            if (component != null) {
                tooltip.add(component);
            }
        }
    }

}
