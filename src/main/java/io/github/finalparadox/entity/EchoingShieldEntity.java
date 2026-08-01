package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.item.EchoingAmethystShieldItem;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.*;

public final class EchoingShieldEntity extends Entity {
    private static final EntityDataAccessor<Boolean> HELD=SynchedEntityData.defineId(EchoingShieldEntity.class,EntityDataSerializers.BOOLEAN);
    private UUID ownerId,targetId;private Vec3 targetPoint=Vec3.ZERO;private boolean returning;private int bounces,kills,bonus,retargetDelay;private final Set<UUID> hitTargets=new HashSet<>();
    public EchoingShieldEntity(EntityType<EchoingShieldEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public boolean held(){return entityData.get(HELD);}
    public static boolean isHeld(ServerPlayer owner){EchoingShieldEntity shield=find(owner);return shield!=null&&shield.held();}
    public static void ensureHeld(ServerPlayer owner){if(!owner.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get()))return;if(find(owner)!=null)return;owner.getPersistentData().remove(EchoingAmethystShieldItem.IN_FLIGHT_KEY);EchoingShieldEntity shield=new EchoingShieldEntity(ModEntities.ECHOING_SHIELD.get(),owner.serverLevel());shield.ownerId=owner.getUUID();shield.entityData.set(HELD,true);shield.follow(owner);owner.serverLevel().addFreshEntity(shield);}
    public static void launch(ServerPlayer owner){if(!owner.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get()))return;int cooldown=owner.getPersistentData().getInt(EchoingAmethystShieldItem.COOLDOWN_KEY);if(cooldown>0){owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.echoing_shield.cooldown",(cooldown+19)/20),true);return;}ensureHeld(owner);EchoingShieldEntity shield=find(owner);if(shield==null||!shield.held())return;owner.getPersistentData().putBoolean(EchoingAmethystShieldItem.IN_FLIGHT_KEY,true);owner.getOffhandItem().getOrCreateTag().remove(EchoingAmethystShieldItem.READY_FOIL_KEY);shield.entityData.set(HELD,false);shield.returning=false;shield.bounces=shield.kills=shield.bonus=shield.retargetDelay=0;shield.hitTargets.clear();shield.targetId=null;shield.targetPoint=owner.getEyePosition().add(owner.getLookAngle().scale(20));shield.setPos(owner.getEyePosition());owner.serverLevel().playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,1,1.25F);}
    private static EchoingShieldEntity find(ServerPlayer owner){return owner.serverLevel().getEntitiesOfClass(EchoingShieldEntity.class,owner.getBoundingBox().inflate(80),e->owner.getUUID().equals(e.ownerId)).stream().findFirst().orElse(null);}
    private void follow(ServerPlayer owner){Vec3 forward=Vec3.directionFromRotation(0,owner.getYRot()),right=new Vec3(forward.z,0,-forward.x);Vec3 p=owner.position().add(right.scale(.6)).add(forward.scale(owner.isCrouching()?.45:0)).add(0,owner.isCrouching()?1.7:1.0,0);setPos(p);setYRot(owner.getYRot()+(owner.isCrouching()?5:0));}
    @Override protected void defineSynchedData(){entityData.define(HELD,true);}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null||!owner.getOffhandItem().is(ModItems.ECHOING_AMETHYST_SHIELD.get())){discard();return;}if(held()){follow(owner);ItemStack heldStack=owner.getOffhandItem();if((tickCount&3)==0&&heldStack.getItem().isFoil(heldStack))level.sendParticles(ParticleTypes.ENCHANT,getX(),getY(),getZ(),2,.45,.45,.2,.05);return;}setYRot(getYRot()+20);if(retargetDelay>0){if(--retargetDelay==0&&!returning)selectNext(owner);return;}if(returning){targetPoint=owner.getEyePosition();if(position().distanceToSqr(targetPoint)<2){finish(owner);return;}}else if(targetId!=null){Entity target=level.getEntity(targetId);if(target instanceof LivingEntity living&&living.isAlive())targetPoint=living.getEyePosition();else selectNext(owner);}Vec3 motion=targetPoint.subtract(position());if(motion.lengthSqr()>.01)setPos(position().add(motion.normalize()));level.sendParticles(ParticleTypes.WITCH,getX(),getY(),getZ(),4,.35,.35,.35,.02);Vec3 hitCenter=position().add(0,-1,0);LivingEntity struck=level.getEntitiesOfClass(LivingEntity.class,new AABB(hitCenter,hitCenter).inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID())).stream().min(Comparator.comparingDouble(e->e.distanceToSqr(this))).orElse(null);if(struck!=null)impact(owner,struck);else if(!returning&&position().distanceToSqr(targetPoint)<1)selectNext(owner);}
    private void impact(ServerPlayer owner,LivingEntity target){hitTargets.add(target.getUUID());bounces++;boolean airborne=level().getBlockState(target.blockPosition().below()).isAir();float damage=(15+bonus)*(airborne?2:1);ArcaneTechniqueEntity.abilityDamage(owner,target,damage);Vec3 away=target.position().subtract(position()).multiply(1,0,1);if(away.lengthSqr()<.01)away=owner.getLookAngle();away=away.normalize();target.push(away.x*.9,.3,away.z*.9);if(!isBoss(target)){List<EquipmentSlot> armor=new ArrayList<>();for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})if(target.getItemBySlot(slot).is(ModTags.Items.ECHOING_SHIELD_BREAKABLE_ARMOR))armor.add(slot);if(!armor.isEmpty())target.setItemSlot(armor.get(random.nextInt(armor.size())),ItemStack.EMPTY);}if(!target.isAlive()){kills++;bonus+=5;}((ServerLevel)level()).sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);level().playSound(null,target.blockPosition(),SoundEvents.AMETHYST_BLOCK_HIT,SoundSource.PLAYERS,1,.7F);targetId=null;if(bounces>=5)returning=true;retargetDelay=3;}
    private void selectNext(ServerPlayer owner){LivingEntity next=((ServerLevel)level()).getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(20),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID())&&owner.distanceToSqr(e)<=1600).stream().min(Comparator.<LivingEntity>comparingInt(e->e.onGround()?1:0).thenComparingDouble(e->e.distanceToSqr(this))).orElse(null);if(next==null){returning=true;targetId=null;}else{targetId=next.getUUID();targetPoint=next.getEyePosition();}}
    private void finish(ServerPlayer owner){entityData.set(HELD,true);returning=false;targetId=null;owner.getPersistentData().remove(EchoingAmethystShieldItem.IN_FLIGHT_KEY);owner.getPersistentData().putInt(EchoingAmethystShieldItem.COOLDOWN_KEY,300);owner.removeEffect(MobEffects.ABSORPTION);if(kills>0)owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,160,Math.min(4,kills-1)));owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.echoing_shield.kills",kills),true);owner.serverLevel().playSound(null,owner.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,1,1.4F);follow(owner);}
    private static boolean isBoss(LivingEntity e){return e instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||e instanceof net.minecraft.world.entity.boss.wither.WitherBoss||e.getTags().contains("boss");}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");if(tag.hasUUID("Target"))targetId=tag.getUUID("Target");entityData.set(HELD,tag.getBoolean("Held"));returning=tag.getBoolean("Returning");bounces=tag.getInt("Bounces");kills=tag.getInt("Kills");bonus=tag.getInt("Bonus");retargetDelay=tag.getInt("RetargetDelay");targetPoint=new Vec3(tag.getDouble("TX"),tag.getDouble("TY"),tag.getDouble("TZ"));hitTargets.clear();for(int i=0;i<tag.getList("HitTargets",11).size();i++)hitTargets.add(net.minecraft.nbt.NbtUtils.loadUUID(tag.getList("HitTargets",11).get(i)));}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);if(targetId!=null)tag.putUUID("Target",targetId);tag.putBoolean("Held",held());tag.putBoolean("Returning",returning);tag.putInt("Bounces",bounces);tag.putInt("Kills",kills);tag.putInt("Bonus",bonus);tag.putInt("RetargetDelay",retargetDelay);tag.putDouble("TX",targetPoint.x);tag.putDouble("TY",targetPoint.y);tag.putDouble("TZ",targetPoint.z);net.minecraft.nbt.ListTag hits=new net.minecraft.nbt.ListTag();for(UUID id:hitTargets)hits.add(net.minecraft.nbt.NbtUtils.createUUID(id));tag.put("HitTargets",hits);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
