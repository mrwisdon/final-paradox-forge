package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import io.github.finalparadox.client.CosmicHammerSwingRenderer;
import io.github.finalparadox.client.VoidSingularityRenderer;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(NightfallKeyMappings.COMBO);
        event.register(NightfallKeyMappings.RIFT);
        event.register(NightfallKeyMappings.LASER);
        event.register(NightfallKeyMappings.SMALL_LASER);
        event.register(NightfallKeyMappings.CHAIN_BLADE);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NIGHTFALL_LASER.get(), NightfallLaserRenderer::new);
        event.registerEntityRenderer(
                ModEntities.ATACROM_GAUNTLET_VISUAL.get(), AtacromGauntletRenderer::new);
        event.registerEntityRenderer(
                ModEntities.NIGHTFALL_CHAIN_BLADE.get(), NightfallChainBladeRenderer::new);
        event.registerEntityRenderer(ModEntities.GLAIVORUS_BLADE.get(), GlaivorusBladeRenderer::new);
        event.registerEntityRenderer(ModEntities.STYGIAN_ARROW.get(), StygianArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.SUMMONED_BEE.get(), BeeRenderer::new);
        event.registerEntityRenderer(ModEntities.WIND_TORNADO.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.PALADIN_HAMMER_SWING.get(), PaladinHammerSwingRenderer::new);
        event.registerEntityRenderer(ModEntities.CONSECRATION.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.COSMIC_HAMMER_SWING.get(), CosmicHammerSwingRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_SINGULARITY.get(), VoidSingularityRenderer::new);
        event.registerEntityRenderer(ModEntities.BLADE_RING.get(), BladeRingRenderer::new);
        event.registerEntityRenderer(ModEntities.TESLA_CORE.get(), TeslaCoreRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_STORM.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.PICOMERANG.get(), ReturningWeaponRenderer::new);
        event.registerEntityRenderer(ModEntities.HARVESTER.get(), ReturningWeaponRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_SPLIT.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_RAIN.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.PUMPKIN_BOMB.get(), PumpkinBombRenderer::new);
        event.registerEntityRenderer(ModEntities.PUMPKIN_BARRAGE.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.PUMPKIN_HAMMER_SWING.get(), PumpkinHammerRenderer::new);
        event.registerEntityRenderer(ModEntities.CHEST_LASER.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_BREATH.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.FROZEN_PRISON.get(), FrozenPrisonRenderer::new);
        event.registerEntityRenderer(ModEntities.RIPPER_CONE.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.HEAVY_ARBALEST_BURST.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.ELECTRIC_HAMMER_SWING.get(), ElectricHammerSwingRenderer::new);
        event.registerEntityRenderer(ModEntities.GREAT_HOOK.get(), GreatHookRenderer::new);
        event.registerEntityRenderer(ModEntities.BAMBOOMERANG.get(), BamboomerangRenderer::new);
        event.registerEntityRenderer(ModEntities.ELECTRIC_ARC.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.RUIN_HAMMER_SWING.get(), RuinHammerSwingRenderer::new);
        event.registerEntityRenderer(ModEntities.RUIN_WAVE.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.RUIN_PILLAR.get(), RuinPillarRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCANE_TECHNIQUE.get(), ArcaneTechniqueRenderer::new);
        event.registerEntityRenderer(ModEntities.ECTRON_TOWER.get(), EctronTowerRenderer::new);
        event.registerEntityRenderer(ModEntities.ECHOING_SHIELD.get(), EchoingShieldRenderer::new);
        event.registerEntityRenderer(ModEntities.PROFANE_TECHNIQUE.get(), InvisibleAbilityRenderer::new);
        event.registerEntityRenderer(ModEntities.OMEGA_TECHNIQUE.get(), OmegaTechniqueRenderer::new);
        event.registerEntityRenderer(ModEntities.TERRASTALKER_ROVER.get(), TerrastalkerRoverRenderer::new);
        event.registerEntityRenderer(ModEntities.APIGLO.get(), ZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.THAR_KROO.get(), TharKrooRenderer::new);
        event.registerEntityRenderer(ModEntities.CONQUEROR_SHADOW.get(), SkeletonRenderer::new);
        event.registerEntityRenderer(ModEntities.MARAWTHAR.get(), MarawTharRenderer::new);
        event.registerEntityRenderer(ModEntities.KOYOMI.get(), ZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.GARI.get(), PillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.KOROS_ECHO.get(), KorosEchoRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.STYGIAN_POINT.get(),
                    ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.STYGIAN_POINT.get(),
                    ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack
                            ? 0.0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemProperties.register(ModItems.QUEEN_BEE_GREATBOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.QUEEN_BEE_GREATBOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
                            : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
            ItemProperties.register(ModItems.POLYMORPHIC_INJECTOR.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.POLYMORPHIC_INJECTOR.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
                            : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 25.0F);
            ItemProperties.register(ModItems.SACRED_SHIELD.get(), ResourceLocation.withDefaultNamespace("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.ATACROM_GAUNTLET.get(),
                    ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "cooldown"),
                    (stack, level, entity, seed) -> entity instanceof net.minecraft.world.entity.player.Player player
                            && player.getCooldowns().isOnCooldown(ModItems.ATACROM_GAUNTLET.get())
                            ? 1.0F : 0.0F);
            registerBowProperties(ModItems.REPULSOR_GREATBOW.get());
            registerBowProperties(ModItems.KALAMED_THUNDER_BOW.get());
            registerBowProperties(ModItems.DRATAGA.get());
            ItemProperties.register(ModItems.HEAVY_ARBALEST.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.HEAVY_ARBALEST.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) net.minecraft.world.item.CrossbowItem.getChargeDuration(stack));
            ItemProperties.register(ModItems.HEAVY_ARBALEST.get(), ResourceLocation.withDefaultNamespace("charged"),
                    (stack, level, entity, seed) -> net.minecraft.world.item.CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.HEAVY_ARBALEST.get(), ResourceLocation.withDefaultNamespace("firework"),
                    (stack, level, entity, seed) -> net.minecraft.world.item.CrossbowItem.isCharged(stack) && net.minecraft.world.item.CrossbowItem.containsChargedProjectile(stack, net.minecraft.world.item.Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
        });
    }

    private static void registerBowProperties(net.minecraft.world.item.Item item) {
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0.0F
                        : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F);
    }
}
