package io.github.finalparadox.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/** Display-only copy of Glaivorus used by Apiglo; it deliberately has no enchantment glint. */
public final class ApigloBladeItem extends SwordItem {
    public ApigloBladeItem() {
        super(Tiers.IRON, 3, -2.4F, new Properties().stacksTo(1).fireResistant());
    }
}
