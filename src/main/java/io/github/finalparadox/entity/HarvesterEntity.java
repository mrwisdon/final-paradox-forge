package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

public final class HarvesterEntity extends ReturningWeaponEntity {
    private final Set<UUID> struck=new HashSet<>();private int hitCount;
    public HarvesterEntity(EntityType<HarvesterEntity> type,Level level){super(type,level);}
    public static void spawn(ServerPlayer owner,ItemStack stack){HarvesterEntity entity=new HarvesterEntity(ModEntities.HARVESTER.get(),owner.serverLevel());entity.ownerId=owner.getUUID();entity.weapon=stack.copy();entity.launchDirection=owner.getLookAngle().normalize();entity.setPos(owner.getX(),owner.getEyeY()-.3D,owner.getZ());entity.setYRot(owner.getYRot());owner.serverLevel().addFreshEntity(entity);owner.level().playSound(null,owner.blockPosition(),SoundEvents.VEX_CHARGE,SoundSource.PLAYERS,1,1.4F);owner.level().playSound(null,owner.blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,1,1);}
    @Override public void tick(){super.tick();if(level().isClientSide)return;ServerPlayer owner=owner();if(owner==null){if(tickCount>200)returnWeapon();return;}if(tickCount==25)struck.clear();if(tickCount<=15)advanceTo(position().add(launchDirection));else if(tickCount>=21){Vec3 target=owner.position().add(0,1,0),delta=target.subtract(position());if(delta.lengthSqr()<=1.0D){finish(owner);return;}advanceTo(position().add(delta.normalize()));}if(tickCount>=2&&tickCount<=15&&!level().getBlockState(blockPosition()).getCollisionShape(level(),blockPosition()).isEmpty()){tickCount=24;level().playSound(null,blockPosition(),SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1,1.4F);}for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&struck.add(e.getUUID()))){if(target.hurt(owner.damageSources().playerAttack(owner),12)){hitCount++;target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2),owner);level().playSound(null,target.blockPosition(),SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,SoundSource.PLAYERS,1,.6F);}}if(level() instanceof ServerLevel server){server.sendParticles(ParticleTypes.SOUL,getX(),getY(),getZ(),1,.4,0,.4,.02);server.sendParticles(ParticleTypes.WITCH,getX(),getY(),getZ(),1,.2,.2,.2,.01);}if(tickCount>=50)finish(owner);}
    private void advanceTo(Vec3 pos){setPos(pos.x,pos.y,pos.z);setYRot(getYRot()+40.0F);}
    private void finish(ServerPlayer owner){int seconds=Math.min(10,hitCount);if(seconds>0){owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,seconds*20,0));owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,seconds*20,1));owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION,seconds*20,0));}owner.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.finalparadox.harvester.hits",hitCount),true);owner.level().playSound(null,owner.blockPosition(),SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.PLAYERS,1,1.2F);returnWeapon();}
    @Override protected void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);hitCount=tag.getInt("Hits");struck.clear();ListTag list=tag.getList("Struck",Tag.TAG_INT_ARRAY);for(Tag value:list)struck.add(NbtUtils.loadUUID(value));}
    @Override protected void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Hits",hitCount);ListTag list=new ListTag();for(UUID id:struck)list.add(NbtUtils.createUUID(id));tag.put("Struck",list);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
