package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.ability.EotharTimeStopState;
import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.item.AtacromGauntletItem;
import io.github.finalparadox.item.GlaivorusItem;
import io.github.finalparadox.item.StygianPointItem;
import io.github.finalparadox.item.PolymorphicInjectorItem;
import io.github.finalparadox.item.CosmicExtinctionHammerItem;
import io.github.finalparadox.item.LastSparkOfHopeItem;
import io.github.finalparadox.item.SoullessEdgeItem;
import io.github.finalparadox.item.TyrannicalDecapitatorItem;
import io.github.finalparadox.entity.WindTornadoEntity;
import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.entity.B8EncounterManager;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.BladeRingEntity;
import io.github.finalparadox.entity.TeslaCoreEntity;
import io.github.finalparadox.entity.FrostStormEntity;
import io.github.finalparadox.item.RepulsorGreatbowItem;
import io.github.finalparadox.item.KalamedThunderBowItem;
import io.github.finalparadox.item.FrostscaleLeggingsItem;
import io.github.finalparadox.item.PicomerangItem;
import io.github.finalparadox.item.TacheorosSoulSplitterItem;
import io.github.finalparadox.item.HarvesterScytheItem;
import io.github.finalparadox.item.DratagaItem;
import io.github.finalparadox.entity.PicomerangEntity;
import io.github.finalparadox.entity.HarvesterEntity;
import io.github.finalparadox.entity.SoulSplitEntity;
import io.github.finalparadox.entity.FireRainEntity;
import io.github.finalparadox.item.VoidArmorItem;
import io.github.finalparadox.item.WinterLamentItem;
import io.github.finalparadox.item.RuthlessRipperItem;
import io.github.finalparadox.entity.ChestLaserEntity;
import io.github.finalparadox.entity.FrostBreathEntity;
import io.github.finalparadox.entity.FrozenPrisonEntity;
import io.github.finalparadox.entity.RipperConeEntity;
import io.github.finalparadox.item.HeavyArbalestItem;
import io.github.finalparadox.item.ElectricHammerItem;
import io.github.finalparadox.item.GreatHookItem;
import io.github.finalparadox.item.BamboomerangItem;
import io.github.finalparadox.entity.HeavyArbalestBurstEntity;
import io.github.finalparadox.entity.BamboomerangEntity;
import io.github.finalparadox.ability.ArcaneMasteryState;
import io.github.finalparadox.entity.EctronTowerEntity;
import io.github.finalparadox.entity.EchoingShieldEntity;
import io.github.finalparadox.item.EchoingAmethystShieldItem;
import io.github.finalparadox.item.AdaptiveDefenseMatrixItem;
import io.github.finalparadox.item.KoyomiOmegaTridentItem;
import io.github.finalparadox.ability.ProfaneStanceState;
import io.github.finalparadox.entity.OmegaTechniqueEntity;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class GameplayEvents {
    private GameplayEvents() {
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemEntity thrownItem = event.getEntity();
        ItemStack stack = thrownItem.getItem();
        if(stack.is(ModItems.STEPS_OF_EOTHAR.get())){thrownItem.discard();if(!player.getItemBySlot(EquipmentSlot.FEET).isEmpty()){player.getInventory().placeItemBackInInventory(stack.copy());player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.eothar.boots_slot"),true);return;}player.setItemSlot(EquipmentSlot.FEET,stack.copy());EotharTimeStopState.activate(player);return;}
        if(stack.is(ModItems.PROFANE_ARCANE_BLADE.get())){thrownItem.discard();returnToSelectedSlot(player,stack.copy());ProfaneStanceState.onToss(player);return;}
        if(stack.is(ModItems.ADAPTIVE_DEFENSE_MATRIX.get())){thrownItem.discard();if(!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()){player.getInventory().placeItemBackInInventory(stack.copy());player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.defense_matrix.leggings_slot"),true);return;}player.setItemSlot(EquipmentSlot.LEGS,stack.copy());AdaptiveDefenseMatrixItem.activate(player);return;}
        if(stack.getItem() instanceof KoyomiOmegaTridentItem omegaTrident){thrownItem.discard();returnToSelectedSlot(player,stack.copy());int charges=player.getPersistentData().getInt(KoyomiOmegaTridentItem.CHARGES);if(charges<=0){player.level().playSound(null,player.blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.PLAYERS,.3F,2);KoyomiOmegaTridentItem.showCharges(player,0);return;}if(player.isCrouching()){player.getPersistentData().putInt(KoyomiOmegaTridentItem.CHARGES,0);OmegaTechniqueEntity.spawn(player,OmegaTechniqueEntity.SWEEP,omegaTrident.isReforged());}else{player.getPersistentData().putInt(KoyomiOmegaTridentItem.CHARGES,charges-1);OmegaTechniqueEntity.spawn(player,OmegaTechniqueEntity.DASH,omegaTrident.isReforged());}player.getPersistentData().putInt(KoyomiOmegaTridentItem.CHARGE_TIME,0);KoyomiOmegaTridentItem.showCharges(player,player.getPersistentData().getInt(KoyomiOmegaTridentItem.CHARGES));return;}
        if(stack.is(ModItems.ARCANE_MASTER_BLADE.get())){thrownItem.discard();returnToSelectedSlot(player,stack.copy());if(ArcaneMasteryState.isActive(player))ArcaneMasteryState.onTossDuringMastery(player);else ArcaneMasteryState.start(player);return;}
        if(stack.is(ModItems.BAMBOOMERANG.get())){thrownItem.discard();if(player.getPersistentData().getInt(BamboomerangItem.COOLDOWN_KEY)>0){player.getInventory().placeItemBackInInventory(stack.copy());player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.bamboomerang.cooldown"),true);}else{player.getPersistentData().putInt(BamboomerangItem.COOLDOWN_KEY,200);BamboomerangEntity.spawn(player,stack);}return;}
        if(stack.is(ModItems.PICOMERANG.get())){thrownItem.discard();if(player.getPersistentData().getInt(PicomerangItem.COOLDOWN_KEY)>0){player.getInventory().placeItemBackInInventory(stack.copy());player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.picomerang.cooldown"),true);}else{player.getPersistentData().putInt(PicomerangItem.COOLDOWN_KEY,300);PicomerangEntity.spawn(player,stack);}return;}
        if(stack.is(ModItems.HARVESTER_SCYTHE.get())){thrownItem.discard();if(player.getPersistentData().getInt(HarvesterScytheItem.COOLDOWN_KEY)>0){player.getInventory().placeItemBackInInventory(stack.copy());player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.harvester.cooldown"),true);}else{player.getPersistentData().putInt(HarvesterScytheItem.COOLDOWN_KEY,200);HarvesterEntity.spawn(player,stack);}return;}
        if (!stack.is(ModItems.GLAIVORUS.get())) return;
        thrownItem.discard();player.getInventory().placeItemBackInInventory(stack.copy());GlaivorusItem.activate(player, stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickSacredShieldBlocking(event.player);
        if (event.player instanceof ServerPlayer player) {
            GlaivorusAbilityState.tick(player);
            StygianPointItem.tickCooldown(player);
            PolymorphicInjectorItem.tickCooldown(player);
            tickBouncingBoots(player);
            tickForgeBatch(player);
            ArcaneMasteryState.tick(player);
            EchoingShieldEntity.ensureHeld(player);
            tickEchoingShieldBlocking(player);
            ProfaneStanceState.tick(player);
            EotharTimeStopState.tick(player);
            NightfallAbilityState.tick(player);
            AtacromGauntletItem.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getVehicle() instanceof TerrastalkerRoverEntity rover) {
                rover.prepareOwnerDisconnect(player);
            }
            EotharTimeStopState.cancel(player);
            NightfallAbilityState.cancel(player);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!event.isDismounting()
                || !(event.getEntityMounting() instanceof Player player)
                || !(event.getEntityBeingMounted() instanceof TerrastalkerRoverEntity rover)
                || !rover.shouldCancelDismount(player)) {
            return;
        }
        event.setCanceled(true);
        if (player instanceof ServerPlayer serverPlayer) {
            // Vanilla has already started the local dismount gesture. Re-send
            // the authoritative passenger list so the client cannot remain on
            // foot while the server still treats it as this rover's rider.
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(rover));
        }
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ArcaneMasteryState.onJump(player);
        ProfaneStanceState.onJump(player);
        if(!player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.BOUNCING_BOOTS.get())) return;
        String cooldownKey="finalparadox.bouncing_boots_jump_cooldown";
        int cooldown=player.getPersistentData().getInt(cooldownKey);
        if (cooldown>0) {
            cooldown--;
            player.getPersistentData().putInt(cooldownKey,cooldown);
            if(cooldown==0) bouncingReady(player);
        }
        if(player.isCrouching() && cooldown<=0){
            player.getPersistentData().putInt("finalparadox.bouncing_boots_bounces",4);
            player.getPersistentData().putInt(cooldownKey,15);
            player.getPersistentData().putBoolean("finalparadox.bouncing_boots_airborne",true);
            bounceParticles(player,true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob add
                && (add.getTags().contains("b8_h3_enemigo1")
                || add.getTags().contains("b8_h3_enemigo3"))
                && !add.getTags().contains("b8_h3_reventado")
                && add.getRandom().nextBoolean()) {
            B8EncounterManager.burstAdd(add);
        }
        triggerFrozenShatter(event);
        triggerLastSpark(event);
        if (event.getEntity() instanceof ServerPlayer injured) triggerReactiveArmor(injured);
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && player.getMainHandItem().is(ModItems.NIGHTFALL.get())) {
            NightfallAbilityState.applyHitSaturation(player);
        }
        ArcaneMasteryState.recordAttack(player,event.getEntity());
        ProfaneStanceState.recordAttack(player,event.getEntity());
        if(event.getAmount()>=20&&!player.getPersistentData().getBoolean(ArcaneMasteryState.ABILITY_DAMAGE_KEY)&&player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ECTRON_CHESTPLATE.get())&&player.getRandom().nextInt(16)==0)EctronTowerEntity.spawn(player);
        triggerTacheoros(player,event.getEntity());
        triggerWinterLament(player,event.getEntity());
        if(player.getMainHandItem().is(ModItems.SOULLESS_EDGE.get())&&player.isCrouching()
                &&player.getPersistentData().getInt(SoullessEdgeItem.SOULS_KEY)>0
                &&!player.getPersistentData().getBoolean(SoullessEdgeItem.STORM_KEY))startSoulStorm(player);
        if (player.getMainHandItem().is(ModItems.WINDCUTTER.get()) && player.getRandom().nextFloat() < 0.1666F) WindTornadoEntity.spawn(player);
        if (!player.getMainHandItem().is(ModItems.LETHAL_BLOOD_DAGGER.get()) || !player.getOffhandItem().is(ModItems.RAPID_BLOOD_DAGGER.get()) || player.getPersistentData().getBoolean("finalparadox.dagger_critical")) return;
        int hits=player.getPersistentData().getInt("finalparadox.blood_daggers_hits")+1;
        player.getPersistentData().putInt("finalparadox.blood_daggers_hits",hits);
        if(hits%2==0){player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,20,2));player.serverLevel().sendParticles(ParticleTypes.HEART,player.getX(),player.getY()+.5,player.getZ(),3,.3,0,.3,0);player.level().playSound(null,player.blockPosition(),SoundEvents.DOLPHIN_EAT,SoundSource.PLAYERS,1,1);}
        if(hits%10!=0)return;
        player.getPersistentData().putInt("finalparadox.blood_daggers_hits",0);
        LivingEntity target=event.getEntity(); player.getPersistentData().putBoolean("finalparadox.dagger_critical",true);
        target.hurt(player.damageSources().playerAttack(player),17.0F); player.getPersistentData().remove("finalparadox.dagger_critical");
        ServerLevel level=player.serverLevel(); level.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1.5,target.getZ(),1,0,0,0,0);level.sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+1.5,target.getZ(),6,0,0,0,.12);level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.REDSTONE_BLOCK)),target.getX(),target.getY()+1.5,target.getZ(),4,0,0,0,.16);
        for(ServerPlayer ally:level.players()){if(ally.distanceToSqr(player)<=225){ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0));ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,0));}}
        level.playSound(null,player.blockPosition(),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,SoundSource.PLAYERS,.4F,1.7F); level.playSound(null,player.blockPosition(),SoundEvents.PLAYER_ATTACK_STRONG,SoundSource.PLAYERS,1,1);
    }

    /** Mirrors core/recibir_dano: a genuinely hurt B8 rider costs the mount 1 energy. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTerrastalkerRiderHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.getVehicle() instanceof TerrastalkerRoverEntity rover)
                || !rover.isEncounterMode()) {
            return;
        }
        rover.damageEnergy(1);
    }

    @SubscribeEvent public static void onLivingDeath(LivingDeathEvent event){
        B8EncounterManager.onLivingDeath(event.getEntity(), event.getSource());
        if(event.getEntity() instanceof ServerPlayer deadPlayer){
            B5EncounterManager.onPlayerDeath(deadPlayer);
            B8EncounterManager.onPlayerDeath(deadPlayer);
            MarawTharBossEntity.onPlayerDeath(deadPlayer);
        }
        if(!(event.getSource().getEntity() instanceof ServerPlayer player))return;
        ServerLevel level=player.serverLevel();LivingEntity victim=event.getEntity();
        if(player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_ARMOR.get())){int kills=player.getPersistentData().getInt(VoidArmorItem.KILLS_KEY)+1;if(kills>=12){kills=0;ChestLaserEntity.spawn(player);}player.getPersistentData().putInt(VoidArmorItem.KILLS_KEY,kills);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.void_armor.kills",kills),true);}
        if(player.getMainHandItem().is(ModItems.RUTHLESS_RIPPER.get())){int kills=player.getPersistentData().getInt(RuthlessRipperItem.KILLS_KEY)+1;if(kills>=5){kills=0;RipperConeEntity.spawn(player);}player.getPersistentData().putInt(RuthlessRipperItem.KILLS_KEY,kills);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.ruthless_ripper.kills",kills),true);}
        if(player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VALYRIAN_STEEL_TOE_CAPS.get())){
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,80,0));
            if(player.getRandom().nextInt(10)<4){for(ServerPlayer ally:level.players())if(ally.distanceToSqr(player)<=49){ally.removeEffect(MobEffects.POISON);ally.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);}level.sendParticles(ParticleTypes.ENCHANTED_HIT,player.getX(),player.getY()+1,player.getZ(),64,1,0,1,.4);level.playSound(null,player.blockPosition(),SoundEvents.ENDER_EYE_DEATH,SoundSource.PLAYERS,1,1);}
        }
        if(player.getMainHandItem().is(ModItems.TYRANNICAL_DECAPITATOR.get()))spawnDecapitatedSkull(player,victim);
        if(player.getMainHandItem().is(ModItems.SOULLESS_EDGE.get())){
            int souls=Math.min(8,player.getPersistentData().getInt(SoullessEdgeItem.SOULS_KEY)+1);player.getPersistentData().putInt(SoullessEdgeItem.SOULS_KEY,souls);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.soulless_edge.souls",souls),true);
        }
    }

    /** B8 boss adds drop no items while the encounter is active. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!event.getEntity().getTags().contains("b8_add")) return;
        if (event.getEntity().level() instanceof ServerLevel level
                && B8EncounterManager.isActive(level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent public static void onLivingAttack(LivingAttackEvent event){
        if(event.getEntity() instanceof ServerPlayer shielded&&shielded.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get())&&shielded.isCrouching()&&EchoingShieldEntity.isHeld(shielded)&&!event.getSource().is(DamageTypeTags.IS_EXPLOSION)&&isFrontalDirectAttack(shielded,event.getSource().getDirectEntity())){
            event.setCanceled(true);Entity source=event.getSource().getEntity();if(source instanceof LivingEntity attacker){Vec3 away=attacker.position().subtract(shielded.position()).multiply(1,0,1);if(away.lengthSqr()<.01)away=shielded.getLookAngle();away=away.normalize();attacker.push(away.x*1.1,.25,away.z*1.1);attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1),shielded);attacker.addEffect(new MobEffectInstance(MobEffects.WITHER,20,1),shielded);attacker.getPersistentData().putInt("finalparadox.echoing_shield_target_cooldown",15);}shielded.serverLevel().sendParticles(ParticleTypes.WITCH,shielded.getX(),shielded.getY()+1,shielded.getZ(),24,.8,.8,.8,.1);shielded.level().playSound(null,shielded.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.PLAYERS,1,.8F);return;
        }
        if(!(event.getEntity() instanceof ServerPlayer player)||!player.getOffhandItem().is(ModItems.SACRED_SHIELD.get()))return;
        // Sneaking raises the item as a real vanilla shield. Let vanilla directional
        // blocking resolve the hit, and preserve the sacred one-hit immunity charge.
        if(player.isCrouching())return;
        if(player.getPersistentData().getInt("finalparadox.sacred_shield_cooldown")>0)return;
        event.setCanceled(true); player.getPersistentData().putInt("finalparadox.sacred_shield_cooldown",640);ServerLevel level=player.serverLevel();level.sendParticles(ParticleTypes.EXPLOSION,player.getX(),player.getY()+1,player.getZ(),1,0,0,0,0);level.sendParticles(ParticleTypes.END_ROD,player.getX(),player.getY()+1,player.getZ(),40,1,1,1,.2);level.sendParticles(ParticleTypes.LARGE_SMOKE,player.getX(),player.getY()+1,player.getZ(),20,0,0,0,.2);level.playSound(null,player.blockPosition(),SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),SoundSource.PLAYERS,1,1.5F);player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,20,1,true,false));player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.sacred_shield.consumed"),true);
    }

    private static void tickSacredShieldBlocking(Player player){
        boolean sacredShieldEquipped=player.getOffhandItem().is(ModItems.SACRED_SHIELD.get());
        if(sacredShieldEquipped&&player.isCrouching()){
            if(!player.isUsingItem())player.startUsingItem(InteractionHand.OFF_HAND);
        }else if(player.isUsingItem()&&player.getUsedItemHand()==InteractionHand.OFF_HAND
                &&player.getUseItem().is(ModItems.SACRED_SHIELD.get())){
            player.stopUsingItem();
        }
    }

    @SubscribeEvent public static void onLivingTick(LivingEvent.LivingTickEvent event){
        LivingEntity target=event.getEntity();if(target.level().isClientSide)return;int shieldHit=target.getPersistentData().getInt("finalparadox.echoing_shield_target_cooldown");if(shieldHit>0){if(--shieldHit>0)target.getPersistentData().putInt("finalparadox.echoing_shield_target_cooldown",shieldHit);else target.getPersistentData().remove("finalparadox.echoing_shield_target_cooldown");}if(!target.getPersistentData().contains("finalparadox.polymorph_remaining"))return;
        int remaining=target.getPersistentData().getInt("finalparadox.polymorph_remaining")-1;java.util.UUID sheepId=target.getPersistentData().getUUID("finalparadox.polymorph_sheep");Entity sheep=target.level() instanceof ServerLevel serverLevel?serverLevel.getEntity(sheepId):null;if(sheep!=null)target.teleportTo(sheep.getX(),sheep.getY(),sheep.getZ());if(remaining>0){target.getPersistentData().putInt("finalparadox.polymorph_remaining",remaining);return;}target.getPersistentData().remove("finalparadox.polymorph_remaining");target.setInvisible(false);target.setInvulnerable(false);if(target instanceof Mob mob)mob.setNoAi(false);if(target instanceof ServerPlayer player)player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);if(sheep!=null)sheep.discard();target.level().playSound(null,target.blockPosition(),SoundEvents.PUFFER_FISH_BLOW_OUT,SoundSource.PLAYERS,1,.6F);if(target.level() instanceof ServerLevel level)level.sendParticles(ParticleTypes.DRAGON_BREATH,target.getX(),target.getY()+1,target.getZ(),20,0,0,0,.1);
    }

    @SubscribeEvent public static void onProjectileImpact(ProjectileImpactEvent event){
        if(!(event.getProjectile() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof ServerPlayer owner)) return;
        if(arrow.getPersistentData().getBoolean(RepulsorGreatbowItem.ARROW_KEY)){repulsorImpact(owner,arrow,event.getRayTraceResult());return;}
        if(arrow.getPersistentData().getBoolean(KalamedThunderBowItem.ARROW_KEY)){thunderImpact(owner,event.getRayTraceResult());return;}
        if(!arrow.getPersistentData().getBoolean(PolymorphicInjectorItem.ARROW_KEY)||!(event.getRayTraceResult() instanceof EntityHitResult hit)||!(hit.getEntity() instanceof LivingEntity target))return;
        morph(owner,target);for(LivingEntity nearby:owner.serverLevel().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(5),candidate->candidate!=target&&io.github.finalparadox.ability.GlaivorusAbilityState.isHostileTarget(owner,candidate)&&owner.getRandom().nextBoolean()))morph(owner,nearby);arrow.discard();
    }

    @SubscribeEvent public static void onArrowSpawn(EntityJoinLevelEvent event){
        EotharTimeStopState.restoreIfOrphaned(event.getEntity());
        if(event.getLevel().isClientSide||!(event.getEntity() instanceof AbstractArrow arrow)||!(arrow.getOwner() instanceof ServerPlayer player))return;
        if(arrow.getPersistentData().getBoolean(HeavyArbalestItem.ARROW_KEY))return;
        if(player.getMainHandItem().is(ModItems.HEAVY_ARBALEST.get())){event.setCanceled(true);Vec3 origin=arrow.position(),velocity=arrow.getDeltaMovement();if(player.isCrouching()&&player.getPersistentData().getInt(HeavyArbalestItem.COOLDOWN_KEY)<=0){player.getPersistentData().putInt(HeavyArbalestItem.COOLDOWN_KEY,300);player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,60,0));HeavyArbalestBurstEntity.start(player,origin,velocity);}else HeavyArbalestBurstEntity.fireVolley(player.serverLevel(),player,origin,velocity,false);return;}
        if(player.getMainHandItem().is(ModItems.REPULSOR_GREATBOW.get())&&!player.onGround()&&player.getPersistentData().getInt(RepulsorGreatbowItem.COOLDOWN_KEY)<=0){
            arrow.getPersistentData().putBoolean(RepulsorGreatbowItem.ARROW_KEY,true);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;player.getPersistentData().putInt(RepulsorGreatbowItem.COOLDOWN_KEY,300);return;
        }
        if(player.getMainHandItem().is(ModItems.DRATAGA.get())&&player.isCrouching()&&player.getXRot()<=-40.0F&&player.getPersistentData().getInt(DratagaItem.COOLDOWN_KEY)<=0){arrow.discard();player.getPersistentData().putInt(DratagaItem.COOLDOWN_KEY,300);FireRainEntity.spawn(player);return;}
        if(player.getMainHandItem().is(ModItems.KALAMED_THUNDER_BOW.get())){arrow.getPersistentData().putBoolean(KalamedThunderBowItem.ARROW_KEY,true);return;}
        if(!player.isCrouching()||!player.getMainHandItem().is(ModItems.POLYMORPHIC_INJECTOR.get())||player.getPersistentData().getInt(PolymorphicInjectorItem.COOLDOWN_KEY)>0)return;
        arrow.getPersistentData().putBoolean(PolymorphicInjectorItem.ARROW_KEY,true);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;player.getPersistentData().putInt(PolymorphicInjectorItem.COOLDOWN_KEY,400);player.level().playSound(null,player.blockPosition(),SoundEvents.PUFFER_FISH_STING,SoundSource.PLAYERS,1,1.4F);
    }

    private static void repulsorImpact(ServerPlayer owner, AbstractArrow arrow, HitResult hit){
        ServerLevel level=owner.serverLevel();Vec3 center=hit.getLocation();
        for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(center,center).inflate(3.5D),entity->GlaivorusAbilityState.isHostileTarget(owner,entity))){
            target.hurt(owner.damageSources().playerAttack(owner),14.0F);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,1));target.setSecondsOnFire(8);
            Vec3 away=target.position().subtract(center);if(away.lengthSqr()<.001D)away=new Vec3(0,1,0);else away=away.normalize();target.push(away.x*1.2D,.35D,away.z*1.2D);
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,center.x,center.y,center.z,1,0,0,0,0);level.sendParticles(ParticleTypes.FLAME,center.x,center.y,center.z,50,1.5D,1.0D,1.5D,.15D);level.playSound(null,net.minecraft.core.BlockPos.containing(center),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,1.0F,1.1F);arrow.discard();
    }

    private static void thunderImpact(ServerPlayer owner,HitResult hit){
        if(!(hit instanceof EntityHitResult entityHit)||!(entityHit.getEntity() instanceof LivingEntity target))return;
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,1));ServerLevel level=owner.serverLevel();
        if(owner.getPersistentData().getInt(KalamedThunderBowItem.COOLDOWN_KEY)>0||owner.getRandom().nextInt(3)!=0||!level.getEntitiesOfClass(Villager.class,target.getBoundingBox().inflate(8)).isEmpty())return;
        owner.getPersistentData().putInt(KalamedThunderBowItem.COOLDOWN_KEY,70);
        for(int i=0;i<2;i++){LightningBolt bolt=net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);if(bolt!=null){bolt.moveTo(target.position());bolt.setCause(owner);level.addFreshEntity(bolt);}}
        TeslaCoreEntity.spawn(owner,target.getX(),target.getY(),target.getZ());
    }

    private static void triggerReactiveArmor(ServerPlayer player){
        if(player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.THORN_KNIGHT_AEGIS.get())&&player.getPersistentData().getInt(BladeRingEntity.COOLDOWN_KEY)<=0&&player.getRandom().nextInt(6)==0){
            player.getPersistentData().putInt(BladeRingEntity.COOLDOWN_KEY,70);BladeRingEntity.spawn(player);
        }
        if(player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.FROSTSCALE_LEGGINGS.get())&&player.getPersistentData().getInt(FrostscaleLeggingsItem.COOLDOWN_KEY)<=0){
            player.getPersistentData().putInt(FrostscaleLeggingsItem.COOLDOWN_KEY,400);FrostStormEntity.spawn(player);
        }
    }

    private static void triggerTacheoros(ServerPlayer player,LivingEntity target){
        if(!player.getMainHandItem().is(ModItems.TACHEOROS_SOUL_SPLITTER.get())||!GlaivorusAbilityState.isHostileTarget(player,target))return;
        int hits=player.getPersistentData().getInt(TacheorosSoulSplitterItem.HITS_KEY);
        if(player.isCrouching()&&hits>=20&&SoulSplitEntity.spawn(player,target)){player.getPersistentData().putInt(TacheorosSoulSplitterItem.HITS_KEY,0);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.tacheoros.activated"),true);return;}
        hits++;player.getPersistentData().putInt(TacheorosSoulSplitterItem.HITS_KEY,hits);player.displayClientMessage(hits>=20?net.minecraft.network.chat.Component.translatable("message.finalparadox.tacheoros.ready"):net.minecraft.network.chat.Component.translatable("message.finalparadox.tacheoros.hits",hits),true);
    }

    private static void triggerWinterLament(ServerPlayer player,LivingEntity target){
        if(!player.getMainHandItem().is(ModItems.WINTER_LAMENT.get())||!GlaivorusAbilityState.isHostileTarget(player,target))return;
        int crystals=player.getPersistentData().getInt(WinterLamentItem.CRYSTALS_KEY);
        if(player.isCrouching()&&crystals>=5){FrostBreathEntity.spawn(player);return;}
        if(crystals<5&&player.getRandom().nextFloat()<.4F){crystals++;player.getPersistentData().putInt(WinterLamentItem.CRYSTALS_KEY,crystals);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.winter_lament.crystals",crystals),true);player.level().playSound(null,player.blockPosition(),SoundEvents.END_PORTAL_FRAME_FILL,SoundSource.PLAYERS,.5F,2);}
    }

    private static void triggerFrozenShatter(LivingHurtEvent event){
        LivingEntity target=event.getEntity();if(target.level().isClientSide||target.getPersistentData().getInt(FrozenPrisonEntity.FROZEN_TICKS)<=0||target.getPersistentData().getBoolean("finalparadox.frozen_shattering"))return;
        UUID freezerId=target.getPersistentData().hasUUID(FrozenPrisonEntity.FREEZER)?target.getPersistentData().getUUID(FrozenPrisonEntity.FREEZER):null;if(!(target.level() instanceof ServerLevel level)||freezerId==null)return;ServerPlayer owner=level.getServer().getPlayerList().getPlayer(freezerId);if(owner==null)return;
        FrozenPrisonEntity.clear(target);target.getPersistentData().putBoolean("finalparadox.frozen_shattering",true);target.hurt(owner.damageSources().playerAttack(owner),100);target.getPersistentData().remove("finalparadox.frozen_shattering");level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.ICE)),target.getX(),target.getY()+1,target.getZ(),40,.5,.8,.5,.25);level.playSound(null,target.blockPosition(),SoundEvents.GLASS_BREAK,SoundSource.PLAYERS,1,.4F);
        for(ServerPlayer ally:level.players())if(ally.distanceToSqr(target)<=25){ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION,20,3));ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,100,0));}
        if(!target.isAlive()){owner.heal(3);int crystals=Math.min(5,owner.getPersistentData().getInt(WinterLamentItem.CRYSTALS_KEY)+1);owner.getPersistentData().putInt(WinterLamentItem.CRYSTALS_KEY,crystals);owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.winter_lament.crystals",crystals),true);}
    }

    private static void morph(ServerPlayer owner,LivingEntity target){
        if(target.getPersistentData().contains("finalparadox.polymorph_remaining")||target.getTags().contains("boss")||target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||target instanceof net.minecraft.world.entity.boss.wither.WitherBoss)return;
        ServerLevel level=owner.serverLevel();Sheep sheep=new Sheep(net.minecraft.world.entity.EntityType.SHEEP,level);sheep.setAge(-99999);sheep.setColor(net.minecraft.world.item.DyeColor.byId(2+owner.getRandom().nextInt(10)));sheep.setPos(target.getX(),target.getY(),target.getZ());level.addFreshEntity(sheep);target.getPersistentData().putInt("finalparadox.polymorph_remaining",70);target.getPersistentData().putUUID("finalparadox.polymorph_sheep",sheep.getUUID());target.setInvisible(true);target.setInvulnerable(true);if(target instanceof ServerPlayer player)player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);else if(target instanceof Mob mob)mob.setNoAi(true);level.playSound(null,target.blockPosition(),SoundEvents.PUFFER_FISH_BLOW_UP,SoundSource.PLAYERS,1,.6F);level.sendParticles(ParticleTypes.CLOUD,target.getX(),target.getY()+1,target.getZ(),30,1,0,1,.2);
    }

    private static void tickBouncingBoots(ServerPlayer player){
        int sacredCooldown=player.getPersistentData().getInt("finalparadox.sacred_shield_cooldown");if(sacredCooldown>0){if(--sacredCooldown>0)player.getPersistentData().putInt("finalparadox.sacred_shield_cooldown",sacredCooldown);else player.getPersistentData().remove("finalparadox.sacred_shield_cooldown");}
        if(!player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.BOUNCING_BOOTS.get()))return;
        if(player.isCrouching()&&player.onGround()&&player.getPersistentData().getInt("finalparadox.bouncing_boots_jump_cooldown")<=0){
            player.addEffect(new MobEffectInstance(MobEffects.JUMP,40,4,true,false));
            ((ServerLevel)player.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.SLIME_BALL)),player.getX(),player.getY()+0.1,player.getZ(),3,0.05,0,0.05,0.1);
        }
        int bounces=player.getPersistentData().getInt("finalparadox.bouncing_boots_bounces");if(bounces<=0)return;
        boolean wasAir=player.getPersistentData().getBoolean("finalparadox.bouncing_boots_airborne");
        if(!player.onGround()){
            player.getPersistentData().putBoolean("finalparadox.bouncing_boots_airborne",true);
            ((ServerLevel)player.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.SLIME_BLOCK)),player.getX(),player.getY()+0.1,player.getZ(),1,0.05,0,0.05,0.03);
        }else if(wasAir){
            player.getPersistentData().putBoolean("finalparadox.bouncing_boots_airborne",false);
            bounceImpact(player,bounces);player.getPersistentData().putInt("finalparadox.bouncing_boots_bounces",bounces-1);
        }
    }

    private static void tickForgeBatch(ServerPlayer player){
        tickCooldown(player,RepulsorGreatbowItem.COOLDOWN_KEY,"message.finalparadox.repulsor_greatbow.ready");
        tickCooldown(player,KalamedThunderBowItem.COOLDOWN_KEY,null);
        tickCooldown(player,BladeRingEntity.COOLDOWN_KEY,null);
        tickCooldown(player,FrostscaleLeggingsItem.COOLDOWN_KEY,null);
        tickCooldown(player,PicomerangItem.COOLDOWN_KEY,"message.finalparadox.picomerang.ready");
        tickCooldown(player,HarvesterScytheItem.COOLDOWN_KEY,"message.finalparadox.harvester.ready");
        tickCooldown(player,DratagaItem.COOLDOWN_KEY,"message.finalparadox.drataga.ready");
        tickCooldown(player,HeavyArbalestItem.COOLDOWN_KEY,"message.finalparadox.heavy_arbalest.ready");
        tickCooldown(player,ElectricHammerItem.ARC_COOLDOWN_KEY,null);
        tickCooldown(player,GreatHookItem.COOLDOWN_KEY,null);
        tickCooldown(player,BamboomerangItem.COOLDOWN_KEY,"message.finalparadox.bamboomerang.ready");
        tickEchoingShieldCooldown(player);
        tickCooldown(player,AdaptiveDefenseMatrixItem.COOLDOWN_KEY,"message.finalparadox.defense_matrix.ready");
        int sparkCooldown=player.getPersistentData().getInt(LastSparkOfHopeItem.COOLDOWN_KEY);if(sparkCooldown>0){if(--sparkCooldown>0)player.getPersistentData().putInt(LastSparkOfHopeItem.COOLDOWN_KEY,sparkCooldown);else{player.getPersistentData().remove(LastSparkOfHopeItem.COOLDOWN_KEY);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.last_spark.ready"),true);player.level().playSound(null,player.blockPosition(),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,1,1.5F);}}
        if(player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VALYRIAN_STEEL_TOE_CAPS.get())){
            boolean bossNearby=!player.serverLevel().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(128),entity->entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss||entity.getTags().contains("boss")).isEmpty();
            if(!bossNearby)player.addEffect(new MobEffectInstance(MobEffects.JUMP,25,1,true,false));
        }
        tickDecapitatedSkulls(player);tickSoulStorm(player);
    }

    private static void tickCooldown(ServerPlayer player,String key,String readyMessage){int ticks=player.getPersistentData().getInt(key);if(ticks<=0)return;if(--ticks>0){player.getPersistentData().putInt(key,ticks);return;}player.getPersistentData().remove(key);if(readyMessage!=null){player.displayClientMessage(net.minecraft.network.chat.Component.translatable(readyMessage),true);player.level().playSound(null,player.blockPosition(),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,.8F,1.4F);}}

    private static void returnToSelectedSlot(ServerPlayer player,ItemStack stack){int slot=player.getInventory().selected;if(player.getInventory().getItem(slot).isEmpty())player.getInventory().setItem(slot,stack);else player.getInventory().placeItemBackInInventory(stack);}

    private static boolean isFrontalDirectAttack(ServerPlayer player,Entity direct){if(!(direct instanceof LivingEntity)&&!(direct instanceof Projectile))return false;Vec3 look=player.getLookAngle().multiply(1,0,1);Vec3 toward=direct.position().subtract(player.position()).multiply(1,0,1);if(look.lengthSqr()<.01||toward.lengthSqr()<.01){if(direct instanceof Projectile projectile)toward=projectile.getDeltaMovement().reverse().multiply(1,0,1);else return false;}return look.normalize().dot(toward.normalize())>0;}

    private static void tickEchoingShieldBlocking(ServerPlayer player){if(!player.isCrouching()||!player.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get())||!EchoingShieldEntity.isHeld(player)||player.getMainHandItem().is(Items.BOW)||player.getMainHandItem().is(Items.CROSSBOW))return;ServerLevel level=player.serverLevel();Vec3 forward=player.getLookAngle().multiply(1,0,1);if(forward.lengthSqr()<.01)return;forward=forward.normalize();Vec3 projectileCenter=player.position().add(0,1,0).add(forward.scale(2));for(Projectile projectile:level.getEntitiesOfClass(Projectile.class,new net.minecraft.world.phys.AABB(projectileCenter,projectileCenter).inflate(3),entity->entity.isAlive())){if(projectile instanceof ThrownTrident)projectile.setDeltaMovement(Vec3.ZERO);else projectile.discard();level.sendParticles(ParticleTypes.WITCH,projectile.getX(),projectile.getY(),projectile.getZ(),6,.2,.2,.2,.03);}for(int height=0;height<=1;height++){Vec3 center=player.position().add(0,height,0).add(forward);for(LivingEntity enemy:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(center,center).inflate(2.5),entity->GlaivorusAbilityState.isHostileTarget(player,entity)&&entity.getPersistentData().getInt("finalparadox.echoing_shield_target_cooldown")<=0)){Vec3 away=enemy.position().subtract(player.position()).multiply(1,0,1);if(away.lengthSqr()<.01)away=forward;away=away.normalize();enemy.push(away.x*1.1,.25,away.z*1.1);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1),player);enemy.addEffect(new MobEffectInstance(MobEffects.WITHER,20,1),player);enemy.getPersistentData().putInt("finalparadox.echoing_shield_target_cooldown",15);}}}

    private static void tickEchoingShieldCooldown(ServerPlayer player){int ticks=player.getPersistentData().getInt(EchoingAmethystShieldItem.COOLDOWN_KEY);if(ticks<=0)return;if(--ticks>0){player.getPersistentData().putInt(EchoingAmethystShieldItem.COOLDOWN_KEY,ticks);return;}player.getPersistentData().remove(EchoingAmethystShieldItem.COOLDOWN_KEY);for(ItemStack stack:player.getInventory().items)if(stack.is(ModItems.ECHOING_AMETHYST_SHIELD.get()))stack.getOrCreateTag().putBoolean(EchoingAmethystShieldItem.READY_FOIL_KEY,true);if(player.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get()))player.getOffhandItem().getOrCreateTag().putBoolean(EchoingAmethystShieldItem.READY_FOIL_KEY,true);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.echoing_shield.ready"),true);player.level().playSound(null,player.blockPosition(),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,.8F,1.4F);}

    private static void triggerLastSpark(LivingHurtEvent event){
        if(!(event.getEntity() instanceof ServerPlayer injured)||injured.getHealth()-event.getAmount()>5.0F)return;
        ServerPlayer wearer=injured.serverLevel().players().stream().filter(player->player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.LAST_SPARK_OF_HOPE.get())&&player.getPersistentData().getInt(LastSparkOfHopeItem.COOLDOWN_KEY)<=0&&player.distanceToSqr(injured)<=900).min(java.util.Comparator.comparingDouble(player->player.distanceToSqr(injured))).orElse(null);if(wearer==null)return;
        wearer.getPersistentData().putInt(LastSparkOfHopeItem.COOLDOWN_KEY,6000);injured.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,2));injured.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,2));ServerLevel level=injured.serverLevel();
        level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.translatable("message.finalparadox.last_spark.consumed", injured.getDisplayName()), false);
        for(LivingEntity enemy:level.getEntitiesOfClass(LivingEntity.class,injured.getBoundingBox().inflate(8),entity->GlaivorusAbilityState.isHostileTarget(wearer,entity))){Vec3 away=enemy.position().subtract(injured.position()).normalize();enemy.push(away.x*.8,.3,away.z*.8);enemy.setSecondsOnFire(10);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1));}
        level.sendParticles(ParticleTypes.FLAME,injured.getX(),injured.getY()+.8,injured.getZ(),120,3,1,3,.3);level.sendParticles(ParticleTypes.EXPLOSION,injured.getX(),injured.getY()+.5,injured.getZ(),1,0,0,0,0);level.playSound(null,injured.blockPosition(),SoundEvents.GHAST_HURT,SoundSource.PLAYERS,.3F,.7F);level.playSound(null,injured.blockPosition(),SoundEvents.ZOMBIE_INFECT,SoundSource.PLAYERS,1,.6F);
    }

    private static void spawnDecapitatedSkull(ServerPlayer player,LivingEntity victim){
        ServerLevel level=player.serverLevel();ItemEntity skull=new ItemEntity(level,victim.getX(),victim.getY()+2.2,victim.getZ(),new ItemStack(Items.SKELETON_SKULL));skull.setPickUpDelay(32767);skull.getPersistentData().putBoolean(TyrannicalDecapitatorItem.SKULL_TAG,true);skull.setDeltaMovement((player.getRandom().nextDouble()-.5)*.5,.4,(player.getRandom().nextDouble()-.5)*.5);level.addFreshEntity(skull);level.sendParticles(ParticleTypes.SWEEP_ATTACK,victim.getX(),victim.getY()+2.2,victim.getZ(),1,0,0,0,0);level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.REDSTONE_BLOCK)),victim.getX(),victim.getY()+2,victim.getZ(),30,0,0,0,.15);level.playSound(null,victim.blockPosition(),SoundEvents.PUMPKIN_CARVE,SoundSource.PLAYERS,1,.5F);
        for(LivingEntity enemy:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(8),entity->GlaivorusAbilityState.isHostileTarget(player,entity))){Vec3 away=enemy.position().subtract(player.position()).normalize();enemy.push(away.x*.7,.15,away.z*.7);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2));level.sendParticles(ParticleTypes.CRIT,enemy.getX(),enemy.getY()+1,enemy.getZ(),10,0,0,0,.5);}
    }

    private static void tickDecapitatedSkulls(ServerPlayer player){
        for(ItemEntity skull:player.serverLevel().getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(80),entity->entity.getPersistentData().getBoolean(TyrannicalDecapitatorItem.SKULL_TAG))){if(skull.tickCount>=200){skull.discard();continue;}player.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.REDSTONE_BLOCK.defaultBlockState()),skull.getX(),skull.getY()+.3,skull.getZ(),1,.08,.08,.08,0);if(player.getMainHandItem().is(ModItems.TYRANNICAL_DECAPITATOR.get())&&player.distanceToSqr(skull)<=1){player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,80,0));player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,80,1));player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,1));player.serverLevel().sendParticles(ParticleTypes.HEART,player.getX(),player.getY()+1,player.getZ(),3,.5,.5,.5,0);player.level().playSound(null,player.blockPosition(),SoundEvents.WITCH_DRINK,SoundSource.PLAYERS,1,.5F);skull.discard();}}
    }

    private static void startSoulStorm(ServerPlayer player){player.getPersistentData().putBoolean(SoullessEdgeItem.STORM_KEY,true);player.getPersistentData().putInt(SoullessEdgeItem.STORM_TICKS_KEY,0);ServerLevel level=player.serverLevel();for(LivingEntity enemy:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5),entity->GlaivorusAbilityState.isHostileTarget(player,entity))){Vec3 away=enemy.position().subtract(player.position()).normalize();enemy.push(away.x*.7,.2,away.z*.7);enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1));}level.sendParticles(ParticleTypes.LARGE_SMOKE,player.getX(),player.getY()+1,player.getZ(),50,1.5,1.5,1.5,.1);level.playSound(null,player.blockPosition(),SoundEvents.ILLUSIONER_CAST_SPELL,SoundSource.PLAYERS,1,1.1F);}

    private static void tickSoulStorm(ServerPlayer player){if(!player.getPersistentData().getBoolean(SoullessEdgeItem.STORM_KEY))return;int ticks=player.getPersistentData().getInt(SoullessEdgeItem.STORM_TICKS_KEY);ServerLevel level=player.serverLevel();double angle=ticks*.3;for(int i=0;i<6;i++){double a=angle+i*Math.PI/3;level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,player.getX()+Math.cos(a)*2,player.getY(),player.getZ()+Math.sin(a)*2,1,Math.cos(a)*.1,.3,Math.sin(a)*.1,0);}level.sendParticles(ParticleTypes.SOUL,player.getX(),player.getY()+1,player.getZ(),2,1.2,1.2,1.2,.05);for(AbstractArrow arrow:level.getEntitiesOfClass(AbstractArrow.class,player.getBoundingBox().inflate(3))){level.sendParticles(ParticleTypes.CRIT,arrow.getX(),arrow.getY(),arrow.getZ(),10,0,0,0,.5);arrow.discard();}
        if(ticks%20==0){int souls=player.getPersistentData().getInt(SoullessEdgeItem.SOULS_KEY);if(souls<=0){endSoulStorm(player);return;}souls--;player.getPersistentData().putInt(SoullessEdgeItem.SOULS_KEY,souls);player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,20,0));player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20,0));player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,20,0));if(!player.hasEffect(MobEffects.REGENERATION))player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,40,1));for(LivingEntity enemy:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(4.5),entity->GlaivorusAbilityState.isHostileTarget(player,entity))){enemy.addEffect(new MobEffectInstance(MobEffects.WITHER,20,1));enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,1));}player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.soulless_edge.souls",souls),true);if(souls<=0){endSoulStorm(player);return;}}
        player.getPersistentData().putInt(SoullessEdgeItem.STORM_TICKS_KEY,ticks+1);
    }
    private static void endSoulStorm(ServerPlayer player){player.getPersistentData().remove(SoullessEdgeItem.STORM_KEY);player.getPersistentData().remove(SoullessEdgeItem.STORM_TICKS_KEY);player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE,player.getX(),player.getY()+1,player.getZ(),50,1.5,1.5,1.5,0);player.level().playSound(null,player.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,.8F,.6F);}

    private static void bounceImpact(ServerPlayer player,int bounces){
        ServerLevel level=player.serverLevel();bounceParticles(player,true);
        for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5.5),e->io.github.finalparadox.ability.GlaivorusAbilityState.isHostileTarget(player,e))){
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,1),player);target.hurt(player.damageSources().playerAttack(player),5.0F);
        }
        if(bounces>=2)player.addEffect(new MobEffectInstance(MobEffects.JUMP,40,bounces-1,true,false));
    }

    private static void bounceParticles(ServerPlayer player,boolean explosion){
        ServerLevel level=player.serverLevel();
        for(int i=0;i<64;i++){double a=i*Math.PI/32;level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.SLIME_BLOCK)),player.getX()+Math.cos(a),player.getY()+0.1,player.getZ()+Math.sin(a),1,0,0,0,0.9);}
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.SLIME_BLOCK)),player.getX(),player.getY()+0.1,player.getZ(),30,0,0,0,0.35);
        if(explosion)level.sendParticles(ParticleTypes.EXPLOSION,player.getX(),player.getY()+0.1,player.getZ(),1,0,0,0,0);
        level.playSound(null,player.blockPosition(),SoundEvents.SLIME_JUMP,SoundSource.PLAYERS,0.6F,1.7F);
    }

    private static void bouncingReady(ServerPlayer player){
        ServerLevel level=player.serverLevel();level.playSound(null,player.blockPosition(),SoundEvents.HONEY_BLOCK_BREAK,SoundSource.PLAYERS,1,1.5F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.SLIME_BLOCK)),player.getX(),player.getY()+1,player.getZ(),50,0.5,0.7,0.5,0.02);
    }
}
