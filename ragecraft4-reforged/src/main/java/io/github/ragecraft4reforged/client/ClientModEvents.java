package io.github.ragecraft4reforged.client;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.content.InfinitePotionItem;
import io.github.ragecraft4reforged.content.WandItem;
import io.github.ragecraft4reforged.registry.ModItems;
import io.github.ragecraft4reforged.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        ModItems.ALL_INFINITE_POTIONS.values().forEach(entry -> event.register(
                (stack, tintIndex) -> tintIndex == 0 && stack.getItem() instanceof InfinitePotionItem potion
                        ? potion.definition().liquidColor() : -1,
                entry.get()));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.RUNEFORGE.get(), RuneforgeScreen::new);
            ResourceLocation charged = new ResourceLocation(Ragecraft4Reforged.MOD_ID, "charged");
            ModItems.ALL_WANDS.forEach(entry -> ItemProperties.register(entry.get(), charged,
                    (stack, level, entity, seed) -> stack.hasTag()
                            && stack.getTag().getBoolean(WandItem.CHARGED) ? 1.0F : 0.0F));
        });
    }

    private ClientModEvents() {
    }
}
