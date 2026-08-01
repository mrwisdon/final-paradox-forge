package io.github.ragecraft4reforged.runeforge;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RuneDefinition {
    private int sourceId;
    private String registryName;
    private int originalCustomModelData;
    private String category;
    private String rarity;
    private int runePower;
    private int cost;
    private String currency;
    private List<String> compatibilities;
    private String nameJson;
    private List<String> loreJson;
    private String affixNameJson;
    private List<String> forgedLoreJson;
    private Map<String, Integer> enchantments;
    private Map<String, Integer> stats;
    private List<AttributeDefinition> attributes;
    private String abilityTag;
    private int modelOffset;
    private String trimMaterial;
    private String trimPattern;
    private String iconModel;

    public int sourceId() {
        return sourceId;
    }

    public String registryName() {
        return registryName;
    }

    public int originalCustomModelData() {
        return originalCustomModelData;
    }

    public RuneCategory category() {
        return RuneCategory.valueOf(category.toUpperCase(java.util.Locale.ROOT));
    }

    public RuneRarity rarity() {
        return RuneRarity.valueOf(rarity.toUpperCase(java.util.Locale.ROOT));
    }

    public int runePower() {
        return runePower;
    }

    public int cost() {
        return cost;
    }

    public RuneCurrency currency() {
        return RuneCurrency.valueOf(currency.toUpperCase(java.util.Locale.ROOT));
    }

    public String nameJson() {
        return nameJson;
    }

    public List<String> loreJson() {
        return loreJson == null ? Collections.emptyList() : loreJson;
    }

    public String affixNameJson() {
        return affixNameJson == null ? "" : affixNameJson;
    }

    public List<String> forgedLoreJson() {
        return forgedLoreJson == null ? Collections.emptyList() : forgedLoreJson;
    }

    public Map<String, Integer> enchantments() {
        return enchantments == null ? Collections.emptyMap() : enchantments;
    }

    public Map<String, Integer> stats() {
        return stats == null ? Collections.emptyMap() : stats;
    }

    public List<AttributeDefinition> attributes() {
        return attributes == null ? Collections.emptyList() : attributes;
    }

    public String abilityTag() {
        return abilityTag == null ? "" : abilityTag;
    }

    public int modelOffset() {
        return modelOffset;
    }

    public String trimMaterial() {
        return trimMaterial == null ? "" : trimMaterial;
    }

    public String trimPattern() {
        return trimPattern == null ? "" : trimPattern;
    }

    public String iconModel() {
        return iconModel == null ? "" : iconModel;
    }

    public boolean matches(ItemStack stack) {
        if (compatibilities == null || compatibilities.isEmpty()) {
            return false;
        }
        for (String compatibility : compatibilities) {
            boolean matches = switch (compatibility) {
                case "sword" -> PrefixTool.SWORD.matches(stack);
                case "axe" -> PrefixTool.AXE.matches(stack);
                case "bow" -> PrefixTool.BOW.matches(stack);
                case "pickaxe" -> PrefixTool.PICKAXE.matches(stack);
                case "helmet" -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET;
                case "chestplate" -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE;
                case "leggings" -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.LEGGINGS;
                case "boots" -> stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.BOOTS;
                default -> false;
            };
            if (matches) {
                return true;
            }
        }
        return false;
    }

    public static final class AttributeDefinition {
        private String attribute;
        private double amount;
        private String operation;

        public String attribute() {
            return attribute;
        }

        public double amount() {
            return amount;
        }

        public String operation() {
            return operation;
        }
    }
}
