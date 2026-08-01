package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.content.enchantment.Rc4Enchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> REGISTER =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<Enchantment> DECAPITATE = melee("decapitate", 4);
    public static final RegistryObject<Enchantment> INTELLECT = REGISTER.register("intellect", () ->
            new Rc4Enchantment(Enchantment.Rarity.RARE, Rc4Enchantment.Target.HELMET, 2));
    public static final RegistryObject<Enchantment> LIFE_LEECH = melee("life_leech", 2);
    public static final RegistryObject<Enchantment> MANA_LEECH = melee("mana_leech", 3);
    public static final RegistryObject<Enchantment> SHARPSHOT = bow("sharpshot", 4);
    public static final RegistryObject<Enchantment> SLAM = melee("slam", 7);
    public static final RegistryObject<Enchantment> SLICE = melee("slice", 6);
    public static final RegistryObject<Enchantment> TRUESHOT = bow("trueshot", 1);
    public static final RegistryObject<Enchantment> VOLLEY = bow("volley", 2);

    public static final List<RegistryObject<Enchantment>> ALL = List.of(
            DECAPITATE, INTELLECT, LIFE_LEECH, MANA_LEECH, SHARPSHOT,
            SLAM, SLICE, TRUESHOT, VOLLEY);
    private static final Map<String, RegistryObject<Enchantment>> BY_STAT = Map.ofEntries(
            Map.entry("decapitate", DECAPITATE),
            Map.entry("intellect", INTELLECT),
            Map.entry("life_leech", LIFE_LEECH),
            Map.entry("mana_leech", MANA_LEECH),
            Map.entry("sharpshot", SHARPSHOT),
            Map.entry("slam", SLAM),
            Map.entry("slice", SLICE),
            Map.entry("trueshot", TRUESHOT),
            Map.entry("volley", VOLLEY));

    @Nullable
    public static Enchantment byStat(String stat) {
        RegistryObject<Enchantment> enchantment = BY_STAT.get(stat);
        return enchantment == null ? null : enchantment.get();
    }

    private static RegistryObject<Enchantment> melee(String name, int maxLevel) {
        return REGISTER.register(name, () -> new Rc4Enchantment(
                Enchantment.Rarity.RARE, Rc4Enchantment.Target.MELEE, maxLevel));
    }

    private static RegistryObject<Enchantment> bow(String name, int maxLevel) {
        return REGISTER.register(name, () -> new Rc4Enchantment(
                Enchantment.Rarity.VERY_RARE, Rc4Enchantment.Target.BOW, maxLevel));
    }

    private ModEnchantments() {
    }
}
