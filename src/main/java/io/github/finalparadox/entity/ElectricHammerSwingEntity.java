package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.item.ElectricHammerItem;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ElectricHammerSwingEntity extends Entity {
    private UUID ownerId; private int direction=1; private final Set<UUID> hitTargets=new HashSet<>();
    public ElectricHammerSwingEntity(EntityType<ElectricHammerSwingEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static boolean hasActive(ServerPlayer owner){return !owner.serverLevel().getEntitiesOfClass(ElectricHammerSwingEntity.class,owner.getBoundingBox().inflate(16),e->owner.getUUID().equals(e.ownerId)).isEmpty();}
    public static void activate(ServerPlayer owner){if(owner.isCrouching()&&owner.getPersistentData().getInt(ElectricHammerItem.CHARGE_KEY)>=16){owner.getPersistentData().putInt(ElectricHammerItem.CHARGE_KEY,0);radial(owner);return;}if(owner.isCrouching()||hasActive(owner))return;ElectricHammerSwingEntity swing=new ElectricHammerSwingEntity(ModEntities.ELECTRIC_HAMMER_SWING.get(),owner.serverLevel());swing.ownerId=owner.getUUID();int side=owner.getPersistentData().getInt("finalparadox.electric_hammer_side");swing.direction=side==0?1:-1;owner.getPersistentData().putInt("finalparadox.electric_hammer_side",side==0?1:0);swing.setPos(owner.position());owner.serverLevel().addFreshEntity(swing);}
    private static void radial(ServerPlayer owner){for(int i=0;i<12;i++)ElectricArcEntity.spawn(owner,new Vec3(Math.sin(i*Math.PI/6),0,Math.cos(i*Math.PI/6)));owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,100,0));owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,100,0));owner.serverLevel().playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_THUNDER,SoundSource.PLAYERS,1,1.4F);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null||!owner.isUsingItem()||!owner.getUseItem().is(ModItems.ELECTRIC_HAMMER.get())||owner.isCrouching()){discard();return;}int cycle=(tickCount-1)%16;if(cycle==0){if(tickCount>1){direction=-direction;hitTargets.clear();}if(owner.getPersistentData().getInt(ElectricHammerItem.ARC_COOLDOWN_KEY)<=0){ElectricArcEntity.spawn(owner,Vec3.directionFromRotation(0,owner.getYRot()));owner.getPersistentData().putInt(ElectricHammerItem.ARC_COOLDOWN_KEY,100);}}setPos(owner.getX(),owner.getY()+.1,owner.getZ());setYRot(owner.getYRot()+direction*(190-cycle*20));if(cycle<4)return;Vec3 forward=Vec3.directionFromRotation(0,getYRot()).scale(2.5),center=position().add(forward);for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(1.5),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID()))){if(target.hurt(owner.damageSources().playerAttack(owner),7.0F)){hitTargets.add(target.getUUID());ElectricHammerItem.recordHit(owner);target.push(forward.x*.45,.25,forward.z*.45);level.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);level.playSound(null,target.blockPosition(),SoundEvents.PLAYER_HURT_ON_FIRE,SoundSource.PLAYERS,.8F,1.2F);}}}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=tag.getInt("Direction");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putInt("Direction",direction);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
