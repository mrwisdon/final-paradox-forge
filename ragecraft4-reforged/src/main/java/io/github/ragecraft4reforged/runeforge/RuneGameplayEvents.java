package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.content.AccessoryCuriosIntegration;
import io.github.ragecraft4reforged.content.WandItem;
import io.github.ragecraft4reforged.content.effect.EffectDamage;
import io.github.ragecraft4reforged.network.ModNetwork;
import io.github.ragecraft4reforged.registry.ModEnchantments;
import io.github.ragecraft4reforged.registry.ModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RuneGameplayEvents {
    private static final String PLAYER = "RC4RunePlayer";
    private static final String MANA = "Mana";
    private static final String SYNCED_MANA = "SyncedMana";
    private static final String EVIS_STAGE = "EvisStage";
    private static final String EVIS_TIMER = "EvisTimer";
    private static final String FROST_READY = "FrostReady";
    private static final String PENDING_SHARPSHOT = "PendingSharpshot";
    private static final String PENDING_TRUESHOT = "PendingTrueshot";
    private static final String PENDING_VOLLEY = "PendingVolley";
    private static final String PENDING_TRAP = "PendingTrap";
    private static final String ABILITY_DAMAGE = "AbilityDamage";
    private static final String SLICE_TICKS = "RC4SliceTicks";
    private static final String SLICE_LEVEL = "RC4SliceLevel";
    private static final String GENERATED_VOLLEY_ARROW = "RC4GeneratedVolleyArrow";
    private static final String FROZEN_TICKS = "RC4FrozenTicks";
    private static final String FROZEN_PREVIOUS_NO_AI = "RC4FrozenPreviousNoAi";
    private static final String TRAP = "RC4ExplosiveTrap";
    private static final String ABILITY_TARGET_TAG = "r4r_ability_target";
    private static final String FUNGUS_DOT_APPLY_TAG = "r4r_fungus_dot_apply";
    private static final String CORROSION_DOT_APPLY_TAG = "r4r_corrosion_dot_apply";
    private static final String CORROSION_DOT_TICKS = "R4RCorrosionDotTicks";
    private static final String CORROSION_DOT_PULSE = "R4RCorrosionDotPulse";
    private static final String CORROSION_DOT_OWNER = "R4RCorrosionDotOwner";
    private static final int CORROSION_DOT_DURATION = 100;
    private static final int CORROSION_DOT_INTERVAL = 20;
    private static final float CORROSION_DOT_DAMAGE = 5.0f;
    private static final int MAX_MANA = 20;
    private static final String RUNE_POWER_SCORE = "r4r_rune_power";
    private static final ResourceLocation SUFFIX_RUNTIME = new ResourceLocation(Ragecraft4Reforged.MOD_ID, "suffix_tick");
    private static final ResourceLocation AXE_SKILL_TRIGGER = new ResourceLocation("skills", "misc/carrot_stick_use");

    private static final Map<ServerLevel, TrapState> TRAPS = new WeakHashMap<>();
    private static final Map<ServerLevel, List<DragonBreathState>> BREATHS = new WeakHashMap<>();
    private static final Set<String> ACTIVE_AXE_ABILITIES = Set.of(
            "axe_throw", "cold_snap", "shadow_grasp", "combustion", "void_rage",
            "lightning_warp", "snowstorm", "thunder_slam", "vt_axe_throw");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        AccessoryCuriosIntegration.syncAbilityTags(player);
        CompoundTag data = playerData(player);
        if (player.tickCount % 20 == 0) {
            int runePower = RuneProgression.get(player);
            Objective runePowerObjective = player.getScoreboard().getObjective(RUNE_POWER_SCORE);
            if (runePowerObjective != null) {
                player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), runePowerObjective)
                        .setScore(runePower);
            }
        }
        Objective manaObjective = player.getScoreboard().getObjective("mana");
        if (manaObjective != null && player.getScoreboard().hasPlayerScore(player.getScoreboardName(), manaObjective)) {
            Score manaScore = player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), manaObjective);
            data.putInt(MANA, Math.max(0, Math.min(MAX_MANA, manaScore.getScore())));
        } else if (!data.contains(MANA)) {
            data.putInt(MANA, MAX_MANA);
        }
        int intellect = combinedLevel(player.getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.INTELLECT,
                "intellect", player.server);
        int intellectInterval = intellect >= 2 ? 40 : 60;
        if (intellect > 0 && player.tickCount % intellectInterval == 0) {
            addMana(player, 1);
        }
        syncManaIfChanged(player, data);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(PLAYER)) {
            event.getEntity().getPersistentData().put(PLAYER,
                    event.getOriginal().getPersistentData().getCompound(PLAYER).copy());
            if (event.getEntity() instanceof ServerPlayer player) {
                syncMana(player, playerData(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag data = playerData(player);
            if (!data.contains(MANA)) {
                data.putInt(MANA, MAX_MANA);
            }
            syncMana(player, data);
        }
    }

    @SubscribeEvent
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof WandItem wand && event.getSlotType() == EquipmentSlot.MAINHAND) {
            for (int index = 0; index < wand.modifiers().size(); index++) {
                WandItem.Modifier modifier = wand.modifiers().get(index);
                Attribute attribute = net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(modifier.attribute());
                if (attribute != null) {
                    String identity = "spell_" + wand.spellId() + ":" + modifier.attribute() + ":" + index + ":mainhand";
                    add(event, attribute, UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)),
                            modifier.amount(), modifier.operation());
                }
            }
        }
        if (LivingEntity.getEquipmentSlotForItem(stack) != event.getSlotType()) {
            return;
        }
        RuneDefinition definition = RuneforgeData.getDefinition(stack, RuneCategory.UPGRADE);
        if (definition == null) {
            return;
        }
        for (int index = 0; index < definition.attributes().size(); index++) {
            RuneDefinition.AttributeDefinition attributeDefinition = definition.attributes().get(index);
            String attributeId = attributeDefinition.attribute().contains(":")
                    ? attributeDefinition.attribute() : "minecraft:" + attributeDefinition.attribute();
            Attribute attribute = net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(
                    ResourceLocation.tryParse(attributeId));
            if (attribute != null) {
                String identity = definition.registryName() + ":" + attributeDefinition.attribute() + ":" + index
                        + ":" + event.getSlotType().getName();
                UUID uuid = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
                AttributeModifier.Operation operation = "addition".equals(attributeDefinition.operation())
                        ? AttributeModifier.Operation.ADDITION : AttributeModifier.Operation.MULTIPLY_BASE;
                add(event, attribute, uuid, attributeDefinition.amount(), operation);
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Component rarity = forgedRarityLine(stack);
        if (rarity == null) {
            return;
        }

        if (!event.getToolTip().isEmpty()) {
            event.getToolTip().set(0, RuneforgeService.forgedName(stack));
        }
        removeVanillaTrimTooltip(event.getToolTip(), RuneforgeData.getDefinition(stack, RuneCategory.SUFFIX));

        int top = Math.min(1, event.getToolTip().size());
        RuneDefinition upgrade = RuneforgeData.getDefinition(stack, RuneCategory.UPGRADE);
        top = insertLore(event.getToolTip(), top, upgrade);
        event.getToolTip().add(top++, rarity);

        RuneDefinition suffix = RuneforgeData.getDefinition(stack, RuneCategory.SUFFIX);
        if (suffix != null && !suffix.forgedLoreJson().isEmpty()) {
            int attributes = attributeSectionStart(event.getToolTip());
            event.getToolTip().add(attributes++, Component.empty());
            insertLore(event.getToolTip(), attributes, suffix);
        }
    }

    private static int insertLore(List<Component> tooltip, int index, RuneDefinition definition) {
        if (definition == null) {
            return index;
        }
        for (String json : definition.forgedLoreJson()) {
            Component component = Component.Serializer.fromJson(json);
            if (component != null) {
                tooltip.add(index++, component);
            }
        }
        return index;
    }

    private static void removeVanillaTrimTooltip(List<Component> tooltip, RuneDefinition definition) {
        if (definition == null || definition.trimMaterial().isEmpty() || definition.trimPattern().isEmpty()) {
            return;
        }
        String header = Component.translatable("item.upgrade").getString();
        String pattern = Component.translatable("trim_pattern." + definition.trimPattern().replace(':', '.')).getString();
        String material = Component.translatable("trim_material." + definition.trimMaterial().replace(':', '.')).getString();
        tooltip.removeIf(line -> {
            String text = line.getString();
            return text.equals(header) || text.contains(pattern) || text.contains(material);
        });
    }

    private static int attributeSectionStart(List<Component> tooltip) {
        for (int index = 1; index < tooltip.size(); index++) {
            if (tooltip.get(index).getContents() instanceof TranslatableContents contents
                    && contents.getKey().startsWith("item.modifiers.")) {
                return index > 0 && tooltip.get(index - 1).getString().isEmpty() ? index - 1 : index;
            }
        }
        return tooltip.size();
    }

    private static Component forgedRarityLine(ItemStack stack) {
        String type = forgedItemType(stack);
        if (type == null) {
            return null;
        }
        RuneRarity rarity = RuneRarity.COMMON;
        int tier = 0;
        boolean forged = false;
        for (RuneCategory category : RuneCategory.values()) {
            RuneDefinition definition = RuneforgeData.getDefinition(stack, category);
            if (definition != null) {
                forged = true;
                tier = Math.max(tier, definition.runePower());
                if (definition.rarity().ordinal() > rarity.ordinal()) {
                    rarity = definition.rarity();
                }
            }
        }
        if (!forged) {
            return null;
        }
        int translationVariant = rarity.ordinal() * 2 + 1;
        return Component.translatable("src4.cr.functions.crafting_station.lore.generate.rarity."
                        + type + "." + translationVariant)
                .withStyle(style -> style.withColor(0x555555).withItalic(false))
                .append(Component.translatable("item.emerald.1.name.1")
                        .withStyle(style -> style.withFont(new ResourceLocation("rc4", "s")).withColor(0xFFFFFF)))
                .append(Component.literal(Integer.toString(tier)).withStyle(style -> style.withColor(0x55FF55)));
    }

    private static String forgedItemType(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return switch (armor.getType()) {
                case HELMET -> "helmet";
                case CHESTPLATE -> "chestplate";
                case LEGGINGS -> "leggings";
                case BOOTS -> "boots";
            };
        }
        if (stack.getItem() instanceof SwordItem) {
            return "sword";
        }
        if (stack.getItem() instanceof AxeItem) {
            return "axe";
        }
        if (stack.getItem() instanceof BowItem) {
            return "bow";
        }
        if (stack.getItem() instanceof PickaxeItem) {
            return "pickaxe";
        }
        return null;
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = playerData(player);
        ItemStack bow = event.getBow();
        int sharpshot = combinedLevel(bow, ModEnchantments.SHARPSHOT, "sharpshot", player.server);
        if (event.getCharge() >= 50 && sharpshot > 0) {
            data.putInt(PENDING_SHARPSHOT, sharpshot);
        }
        if (combinedLevel(bow, ModEnchantments.TRUESHOT, "trueshot", player.server) > 0) {
            data.putBoolean(PENDING_TRUESHOT, true);
        }
        int volley = combinedLevel(bow, ModEnchantments.VOLLEY, "volley", player.server);
        if (volley > 0) {
            data.putInt(PENDING_VOLLEY, volley);
        }
        if (!originalRuntimeAvailable(player.server)
                && RuneforgeData.is(bow, RuneCategory.SUFFIX, "rune_of_trapping")) {
            data.putBoolean(PENDING_TRAP, true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            syncAbilityTargetTag(livingEntity);
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof ServerPlayer owner)) {
            return;
        }
        if (arrow.getPersistentData().getBoolean(GENERATED_VOLLEY_ARROW)) {
            return;
        }
        CompoundTag data = playerData(owner);
        int sharpshot = data.getInt(PENDING_SHARPSHOT);
        if (sharpshot > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + 2.0 * sharpshot);
            data.remove(PENDING_SHARPSHOT);
            ((ServerLevel) event.getLevel()).sendParticles(ParticleTypes.CRIT, arrow.getX(), arrow.getY(), arrow.getZ(), 12, .2, .2, .2, .1);
        }
        if (data.getBoolean(PENDING_TRUESHOT)) {
            if (arrow.isCritArrow()) {
                arrow.setNoGravity(true);
            }
            data.remove(PENDING_TRUESHOT);
        }
        int volley = data.getInt(PENDING_VOLLEY);
        if (volley > 0) {
            if (arrow.isCritArrow() && arrow instanceof Arrow vanillaArrow) {
                spawnVolley((ServerLevel) event.getLevel(), owner, vanillaArrow, volley * 2);
            }
            data.remove(PENDING_VOLLEY);
        }
        if (data.getBoolean(PENDING_TRAP)) {
            arrow.getPersistentData().putBoolean(TRAP, true);
            data.remove(PENDING_TRAP);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile().getServer() != null && originalRuntimeAvailable(event.getProjectile().getServer())) {
            return;
        }
        if (!(event.getProjectile() instanceof AbstractArrow arrow)
                || !arrow.getPersistentData().getBoolean(TRAP)
                || !(event.getRayTraceResult() instanceof BlockHitResult hit)
                || !(arrow.level() instanceof ServerLevel level)) {
            return;
        }
        TrapState previous = TRAPS.remove(level);
        if (previous != null && previous.stand.isAlive()) {
            previous.stand.discard();
        }
        ArmorStand stand = new ArmorStand(level, hit.getLocation().x, hit.getLocation().y - 0.7, hit.getLocation().z);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setNoBasePlate(true);
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TNT));
        CompoundTag armorStandData = new CompoundTag();
        stand.saveWithoutId(armorStandData);
        armorStandData.putBoolean("Small", true);
        armorStandData.putBoolean("Marker", true);
        stand.load(armorStandData);
        stand.getPersistentData().putBoolean(TRAP, true);
        level.addFreshEntity(stand);
        TRAPS.put(level, new TrapState(stand));
        level.playSound(null, stand.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, .4f, 1.0f);
        arrow.discard();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        RuneDefinition suffix = RuneforgeData.getDefinition(stack, RuneCategory.SUFFIX);
        if (!(stack.getItem() instanceof AxeItem)
                || event.getHand() != InteractionHand.MAIN_HAND
                || suffix == null
                || !ACTIVE_AXE_ABILITIES.contains(suffix.abilityTag())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) {
            RuneforgeData.applyRuntimeTags(stack);
            executeFunction(player, AXE_SKILL_TRIGGER);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (target.hasEffect(ModMobEffects.VULNERABILITY.get())) {
            event.setAmount(event.getAmount() * 1.5f);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && event.getSource().getDirectEntity() == player) {
            ItemStack weapon = player.getMainHandItem();
            int decapitate = combinedLevel(weapon, ModEnchantments.DECAPITATE,
                    "decapitate", player.server);
            if (decapitate > 0 && isElite(target)
                    && target.getHealth() - event.getAmount() <= 16.0f + 8.0f * decapitate) {
                event.setAmount(Math.max(event.getAmount(),
                        target.getHealth() + target.getAbsorptionAmount() + 1.0f));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // Corrosion keeps the original Wither III marker and icon, but its damage is
        // owned by the reliable 5-damage DOT below so vanilla hurt frames cannot eat it.
        if (event.getSource().is(DamageTypes.WITHER)
                && event.getEntity().getPersistentData().getInt(CORROSION_DOT_TICKS) > 0) {
            event.setCanceled(true);
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player
                || playerData(player).getBoolean(ABILITY_DAMAGE)) {
            return;
        }
        LivingEntity target = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        boolean fallbackRunes = !originalRuntimeAvailable(player.server);

        int slice = combinedLevel(weapon, ModEnchantments.SLICE, "slice", player.server);
        if (slice > 0) {
            target.getPersistentData().putInt(SLICE_TICKS, 160);
            target.getPersistentData().putInt(SLICE_LEVEL, slice);
        }
        int slam = combinedLevel(weapon, ModEnchantments.SLAM, "slam", player.server);
        boolean critical = player.fallDistance > 0.0f && !player.onGround() && !player.isInWater()
                && !player.isPassenger() && !player.isSprinting();
        if (slam > 0 && critical) {
            event.setAmount(event.getAmount() + 2.0f * slam);
        }
        if (!fallbackRunes) {
            return;
        }

        if (playerData(player).getInt(FROST_READY) > 0
                && RuneforgeData.is(weapon, RuneCategory.SUFFIX, "rune_of_frost")) {
            playerData(player).putInt(FROST_READY, 0);
            applyFrost(player, target);
        }

        String suffix = runePath(weapon, RuneCategory.SUFFIX);
        switch (suffix) {
            case "rune_of_magma" -> activateMagma(player);
            case "rune_of_evisceration" -> activateEvisceration(player);
            case "rune_of_dragonfire" -> activateDragonBreath(player);
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            ItemStack weapon = player.getMainHandItem();
            int manaLeech = combinedLevel(weapon, ModEnchantments.MANA_LEECH,
                    "mana_leech", player.server);
            if (manaLeech > 0) {
                addMana(player, manaLeech);
            }
            int lifeLeech = combinedLevel(weapon, ModEnchantments.LIFE_LEECH,
                    "life_leech", player.server);
            if (lifeLeech > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40 * lifeLeech, 1));
            }
            triggerDebuffKillAbilities(event, player, weapon);
        }
    }

    private static void triggerDebuffKillAbilities(LivingDeathEvent event, ServerPlayer player, ItemStack weapon) {
        LivingEntity target = event.getEntity();
        long debuffCount = target.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .count();

        // The original map encoded the debuff count in Blindness amplifiers. Keep those
        // advancement-driven kills untouched, and bridge native/custom Forge effects here.
        MobEffectInstance blindness = target.getEffect(MobEffects.BLINDNESS);
        boolean legacyRotBlastCounter = blindness != null && blindness.getAmplifier() >= 1;
        boolean legacySoulReaperCounter = blindness != null && blindness.getAmplifier() >= 2;

        boolean meleeKill = event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getDirectEntity() == player
                && !player.getTags().contains("rot_blast_attack");
        if (meleeKill && debuffCount >= 2 && !legacyRotBlastCounter
                && hasAbility(weapon, RuneCategory.SUFFIX, "rot_blast")) {
            RuneforgeData.applyRuntimeTags(weapon);
            int blastLevel = (int) Math.min(8, debuffCount);
            executeFunction(player, new ResourceLocation("skills", "sword/rot_blast_" + blastLevel));
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (debuffCount >= 3 && !legacySoulReaperCounter
                && hasAbility(helmet, RuneCategory.SUFFIX, "soul_reaper")) {
            RuneforgeData.applyRuntimeTags(helmet);
            int reaperLevel = (int) Math.min(8, debuffCount);
            executeFunction(player, new ResourceLocation("skills", "helmet/soul_reaper_" + reaperLevel));
        }

        if (meleeKill && target.hasEffect(MobEffects.WITHER)
                && hasAbility(weapon, RuneCategory.SUFFIX, "infestation")
                && scoreAtLeast(player, "mana", 10)) {
            RuneforgeData.applyRuntimeTags(weapon);
            executeFunction(player, new ResourceLocation("skills", "sword/infestation_kill"));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        syncAbilityTargetTag(entity);
        CompoundTag data = entity.getPersistentData();
        if (entity.removeTag(FUNGUS_DOT_APPLY_TAG)) {
            EffectDamage.hurtIgnoringInvulnerabilityFrames(entity, entity.damageSources().magic(), 5.0f);
            if (!entity.isAlive()) {
                return;
            }
        }
        if (entity.removeTag(CORROSION_DOT_APPLY_TAG)) {
            applyCorrosionDot(entity, data);
        }
        tickCorrosionDot(entity, data);
        if (!entity.isAlive()) {
            return;
        }
        int slice = data.getInt(SLICE_TICKS);
        if (slice > 0) {
            slice--;
            data.putInt(SLICE_TICKS, slice);
            if (slice % 20 == 0) {
                int level = data.getInt(SLICE_LEVEL);
                entity.hurt(entity.damageSources().magic(), 0.5f + 0.5f * level);
                ((ServerLevel) entity.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        entity.getX(), entity.getY() + entity.getBbHeight() * .6, entity.getZ(), 3, .2, .2, .2, .05);
            }
        }

        int frozen = data.getInt(FROZEN_TICKS);
        if (frozen > 0) {
            frozen--;
            data.putInt(FROZEN_TICKS, frozen);
            if (frozen == 0 && entity instanceof Mob mob) {
                mob.setNoAi(data.getBoolean(FROZEN_PREVIOUS_NO_AI));
                data.remove(FROZEN_PREVIOUS_NO_AI);
            }
        }
    }

    private static void applyCorrosionDot(LivingEntity entity, CompoundTag data) {
        if (data.getInt(CORROSION_DOT_TICKS) <= 0) {
            data.putInt(CORROSION_DOT_PULSE, CORROSION_DOT_INTERVAL);
        }
        data.putInt(CORROSION_DOT_TICKS, CORROSION_DOT_DURATION);
        nearestActiveCorrosionCaster(entity).ifPresent(player ->
                data.putUUID(CORROSION_DOT_OWNER, player.getUUID()));
    }

    private static void tickCorrosionDot(LivingEntity entity, CompoundTag data) {
        int remaining = data.getInt(CORROSION_DOT_TICKS);
        if (remaining <= 0) {
            return;
        }
        remaining--;
        data.putInt(CORROSION_DOT_TICKS, remaining);
        int pulse = data.getInt(CORROSION_DOT_PULSE) - 1;
        if (pulse <= 0) {
            Entity owner = data.hasUUID(CORROSION_DOT_OWNER)
                    ? ((ServerLevel) entity.level()).getEntity(data.getUUID(CORROSION_DOT_OWNER)) : null;
            DamageSource source = owner == null ? entity.damageSources().magic()
                    : entity.damageSources().indirectMagic(owner, owner);
            EffectDamage.hurtIgnoringInvulnerabilityFrames(entity, source, CORROSION_DOT_DAMAGE);
            pulse = CORROSION_DOT_INTERVAL;
        }
        data.putInt(CORROSION_DOT_PULSE, pulse);
        if (remaining == 0) {
            data.remove(CORROSION_DOT_TICKS);
            data.remove(CORROSION_DOT_PULSE);
            data.remove(CORROSION_DOT_OWNER);
        }
    }

    private static java.util.Optional<ServerPlayer> nearestActiveCorrosionCaster(LivingEntity target) {
        return ((ServerLevel) target.level()).players().stream()
                .filter(player -> scoreAtLeast(player, "acid_spray_cd", 1))
                .filter(player -> isSpellWand(player.getMainHandItem(), 8)
                        || isSpellWand(player.getOffhandItem(), 8))
                .filter(player -> player.distanceToSqr(target) <= 144.0)
                .min(java.util.Comparator.comparingDouble(player -> player.distanceToSqr(target)));
    }

    private static boolean isSpellWand(ItemStack stack, int spellId) {
        return stack.getItem() instanceof WandItem wand && wand.spellId() == spellId;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        tickTrap(level);
        tickBreaths(level);
    }

    private static void activateMagma(ServerPlayer player) {
        if (!spendMana(player, MAX_MANA)) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 0));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0));
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 100, 2.2, 1.0, 2.2, .08);
        level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + .5, player.getZ(), 35, 2, .5, 2, .05);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1f, .8f);
        for (LivingEntity target : abilityTargets(level, player.getBoundingBox().inflate(7), player)) {
            target.setSecondsOnFire(7);
            knockAway(target, player.position(), Math.max(.4, 1.5 - player.distanceTo(target) * .12));
        }
    }

    private static void activateEvisceration(ServerPlayer player) {
        CompoundTag data = playerData(player);
        int stage = data.getInt(EVIS_STAGE);
        if (stage == 0) {
            if (!spendMana(player, MAX_MANA)) {
                return;
            }
            data.putInt(EVIS_STAGE, 1);
            data.putInt(EVIS_TIMER, 40);
            damageForward(player, 2.0, 3.5, 5.0f);
        } else if (stage == 1 && data.getInt(EVIS_TIMER) > 0) {
            data.putInt(EVIS_STAGE, 2);
            data.putInt(EVIS_TIMER, 40);
            damageForward(player, 2.0, 3.5, 5.0f);
        } else if (stage == 2 && data.getInt(EVIS_TIMER) > 0) {
            data.putInt(EVIS_STAGE, 0);
            data.putInt(EVIS_TIMER, 0);
            damageForward(player, 3.0, 5.5, 10.0f);
        }
    }

    private static void activateDragonBreath(ServerPlayer player) {
        if (!spendMana(player, MAX_MANA)) {
            return;
        }
        Vec3 direction = player.getLookAngle().normalize();
        BREATHS.computeIfAbsent((ServerLevel) player.level(), ignored -> new ArrayList<>())
                .add(new DragonBreathState(player, player.getEyePosition().add(direction.scale(.5)), direction));
    }

    private static void applyFrost(ServerPlayer player, LivingEntity struck) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 center = player.position().add(player.getLookAngle().normalize().scale(3)).add(0, 1, 0);
        AABB area = new AABB(center, center).inflate(4);
        for (LivingEntity target : abilityTargets(level, area, player)) {
            if (isElite(target)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 4));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 6));
                if (target instanceof Mob mob) {
                    CompoundTag data = target.getPersistentData();
                    if (data.getInt(FROZEN_TICKS) <= 0) {
                        data.putBoolean(FROZEN_PREVIOUS_NO_AI, mob.isNoAi());
                    }
                    data.putInt(FROZEN_TICKS, 200);
                    mob.setNoAi(true);
                }
            }
            level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 28, .4, .7, .4, .03);
        }
    }

    private static void tickTrap(ServerLevel level) {
        TrapState trap = TRAPS.get(level);
        if (trap == null) {
            return;
        }
        if (!trap.stand.isAlive()) {
            TRAPS.remove(level);
            return;
        }
        trap.age++;
        if (trap.age % 10 == 0) {
            level.sendParticles(trap.age < 100 ? ParticleTypes.SMOKE : ParticleTypes.FLAME,
                    trap.stand.getX(), trap.stand.getY() + .6, trap.stand.getZ(), 4, .15, .15, .15, .01);
        }
        if (trap.age >= 100 && !abilityTargets(level, trap.stand.getBoundingBox().inflate(2), null).isEmpty()) {
            Vec3 center = trap.stand.position();
            level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + .5, center.z, 8, .8, .4, .8, .05);
            level.playSound(null, trap.stand.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 1f);
            for (LivingEntity target : abilityTargets(level, trap.stand.getBoundingBox().inflate(5), null)) {
                target.hurt(target.damageSources().magic(), 9.0f);
                target.setSecondsOnFire(7);
                knockAway(target, center, 1.2);
            }
            trap.stand.discard();
            TRAPS.remove(level);
        } else if (trap.age >= 1200) {
            trap.stand.discard();
            TRAPS.remove(level);
        }
    }

    private static void tickBreaths(ServerLevel level) {
        List<DragonBreathState> states = BREATHS.get(level);
        if (states == null) {
            return;
        }
        Iterator<DragonBreathState> iterator = states.iterator();
        while (iterator.hasNext()) {
            DragonBreathState state = iterator.next();
            if (!state.owner.isAlive() || state.owner.level() != level || state.age >= 20) {
                iterator.remove();
                continue;
            }
            Vec3 center = state.origin.add(state.direction.scale(state.age * .5));
            level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 28, 1.1, .7, 1.1, .06);
            level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 8, .8, .5, .8, .02);
            for (LivingEntity target : abilityTargets(level, new AABB(center, center).inflate(4), state.owner)) {
                if (state.hit.add(target.getUUID())) {
                    hurtFromPlayer(state.owner, target, 12.0f);
                    target.setSecondsOnFire(7);
                    knockAway(target, state.owner.position(), 1.0);
                }
            }
            state.age++;
        }
        if (states.isEmpty()) {
            BREATHS.remove(level);
        }
    }

    private static void damageForward(ServerPlayer player, double forward, double radius, float damage) {
        Vec3 center = player.getEyePosition().add(player.getLookAngle().normalize().scale(forward));
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 12, radius * .35, .7, radius * .35, .02);
        for (LivingEntity target : abilityTargets(level, new AABB(center, center).inflate(radius), player)) {
            hurtFromPlayer(player, target, damage);
        }
    }

    private static void spawnVolley(ServerLevel level, ServerPlayer owner, Arrow source, int extraArrows) {
        Vec3 velocity = source.getDeltaMovement();
        Vec3 lateral = new Vec3(-velocity.z, 0.0, velocity.x);
        lateral = lateral.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : lateral.normalize();
        double spacing = extraArrows == 2 ? 1.2 : 1.0;
        for (int index = 0; index < extraArrows; index++) {
            int sideIndex = index / 2 + 1;
            double lateralDistance = (index % 2 == 0 ? -1.0 : 1.0) * sideIndex * spacing;
            Arrow extra = new Arrow(level, owner);
            Vec3 position = source.position().add(lateral.scale(lateralDistance));
            extra.setPos(position.x, position.y, position.z);
            extra.setDeltaMovement(velocity);
            extra.setBaseDamage(source.getBaseDamage());
            extra.setCritArrow(true);
            extra.pickup = AbstractArrow.Pickup.DISALLOWED;
            extra.getPersistentData().putBoolean(GENERATED_VOLLEY_ARROW, true);
            extra.addTag("volley");
            extra.addTag("arrow_done");
            if (source.isOnFire()) {
                extra.setSecondsOnFire(Math.max(1, source.getRemainingFireTicks() / 20));
            }
            if (source.isNoGravity()) {
                extra.setNoGravity(true);
            }
            level.addFreshEntity(extra);
        }
    }

    private static void hurtFromPlayer(ServerPlayer player, LivingEntity target, float damage) {
        CompoundTag data = playerData(player);
        data.putBoolean(ABILITY_DAMAGE, true);
        try {
            target.hurt(target.damageSources().playerAttack(player), damage);
        } finally {
            data.remove(ABILITY_DAMAGE);
        }
    }

    private static List<LivingEntity> abilityTargets(ServerLevel level, AABB box, ServerPlayer owner) {
        return level.getEntitiesOfClass(LivingEntity.class, box, entity -> isAbilityTarget(entity, owner));
    }

    private static boolean isAbilityTarget(LivingEntity entity, ServerPlayer owner) {
        return entity.isAlive()
                && entity != owner
                && !(entity instanceof ServerPlayer)
                && !(entity instanceof ArmorStand);
    }

    private static void syncAbilityTargetTag(LivingEntity entity) {
        if (isAbilityTarget(entity, null)) {
            entity.addTag(ABILITY_TARGET_TAG);
        } else {
            entity.removeTag(ABILITY_TARGET_TAG);
        }
    }

    private static boolean isElite(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss || target.getMaxHealth() >= 80.0f;
    }

    private static void knockAway(LivingEntity target, Vec3 center, double strength) {
        target.knockback(strength, center.x - target.getX(), center.z - target.getZ());
    }

    private static boolean spendMana(ServerPlayer player, int amount) {
        CompoundTag data = playerData(player);
        int mana = data.contains(MANA) ? data.getInt(MANA) : MAX_MANA;
        if (mana < amount) {
            return false;
        }
        data.putInt(MANA, mana - amount);
        syncMana(player, data);
        player.displayClientMessage(Component.translatable("message.ragecraft4reforged.mana.value", mana - amount, MAX_MANA), true);
        return true;
    }

    private static boolean originalRuntimeAvailable(MinecraftServer server) {
        return server.getFunctions().get(SUFFIX_RUNTIME).isPresent();
    }

    private static int combinedLevel(ItemStack stack, RegistryObject<Enchantment> enchantment,
                                     String runeStat, MinecraftServer server) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(enchantment.get(), stack);
        if (level == 0 && !originalRuntimeAvailable(server)) {
            level = RuneforgeData.getStat(stack, runeStat);
        }
        return level;
    }

    private static void executeFunction(ServerPlayer player, ResourceLocation id) {
        player.server.getFunctions().get(id).ifPresent(function -> player.server.getFunctions().execute(
                function, player.createCommandSourceStack().withPermission(2).withSuppressedOutput()));
    }

    private static boolean scoreAtLeast(ServerPlayer player, String objectiveName, int minimum) {
        Objective objective = player.getScoreboard().getObjective(objectiveName);
        return objective != null
                && player.getScoreboard().hasPlayerScore(player.getScoreboardName(), objective)
                && player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore() >= minimum;
    }

    private static void addMana(ServerPlayer player, int amount) {
        CompoundTag data = playerData(player);
        Objective manaObjective = player.getScoreboard().getObjective("mana");
        if (manaObjective != null
                && player.getScoreboard().hasPlayerScore(player.getScoreboardName(), manaObjective)) {
            Score score = player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), manaObjective);
            int mana = Math.min(MAX_MANA, score.getScore() + amount);
            score.setScore(mana);
            data.putInt(MANA, mana);
        } else {
            data.putInt(MANA, Math.min(MAX_MANA, data.getInt(MANA) + amount));
        }
        syncMana(player, data);
    }

    private static void syncManaIfChanged(ServerPlayer player, CompoundTag data) {
        if (!data.contains(SYNCED_MANA) || data.getInt(SYNCED_MANA) != data.getInt(MANA)) {
            syncMana(player, data);
        }
    }

    private static void syncMana(ServerPlayer player, CompoundTag data) {
        int mana = data.getInt(MANA);
        data.putInt(SYNCED_MANA, mana);
        ModNetwork.sendMana(player, mana, MAX_MANA);
    }

    private static CompoundTag playerData(LivingEntity player) {
        return player.getPersistentData().getCompound(PLAYER).isEmpty()
                ? createPlayerData(player)
                : player.getPersistentData().getCompound(PLAYER);
    }

    private static CompoundTag createPlayerData(LivingEntity player) {
        CompoundTag data = new CompoundTag();
        player.getPersistentData().put(PLAYER, data);
        return player.getPersistentData().getCompound(PLAYER);
    }

    private static String runePath(ItemStack stack, RuneCategory category) {
        net.minecraft.resources.ResourceLocation id = RuneforgeData.getId(stack, category);
        return id == null ? "" : id.getPath();
    }

    private static boolean hasAbility(ItemStack stack, RuneCategory category, String abilityTag) {
        RuneDefinition definition = RuneforgeData.getDefinition(stack, category);
        return definition != null && abilityTag.equals(definition.abilityTag());
    }

    private static void add(ItemAttributeModifierEvent event, Attribute attribute, UUID uuid,
                            double amount, AttributeModifier.Operation operation) {
        event.addModifier(attribute, new AttributeModifier(uuid, "Ragecraft4 rune upgrade", amount, operation));
    }

    private static final class TrapState {
        private final ArmorStand stand;
        private int age;

        private TrapState(ArmorStand stand) {
            this.stand = stand;
        }
    }

    private static final class DragonBreathState {
        private final ServerPlayer owner;
        private final Vec3 origin;
        private final Vec3 direction;
        private final Set<UUID> hit = new HashSet<>();
        private int age;

        private DragonBreathState(ServerPlayer owner, Vec3 origin, Vec3 direction) {
            this.owner = owner;
            this.origin = origin;
            this.direction = direction;
        }
    }

    private RuneGameplayEvents() {
    }
}
