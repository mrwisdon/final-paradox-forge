package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.runeforge.RuneforgeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<MenuType<RuneforgeMenu>> RUNEFORGE = REGISTER.register(
            "runeforge", () -> IForgeMenuType.create(RuneforgeMenu::new));
    private ModMenus() {
    }
}
