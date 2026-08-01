package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
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

public final class ElectricArcEntity extends Entity {
    private UUID ownerId; private Vec3 direction=Vec3.ZERO; private final Set<UUID> hitTargets=new HashSet<>();
    public ElectricArcEntity(EntityType<ElectricArcEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void spawn(ServerPlayer owner,Vec3 direction){ElectricArcEntity arc=new ElectricArcEntity(ModEntities.ELECTRIC_ARC.get(),owner.serverLevel());arc.ownerId=owner.getUUID();arc.direction=new Vec3(direction.x,0,direction.z).normalize();arc.setPos(owner.getX(),owner.getY(),owner.getZ());owner.serverLevel().addFreshEntity(arc);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null||direction.lengthSqr()<.001){discard();return;}Vec3 next=position().add(direction.scale(2));BlockPos base=BlockPos.containing(next);for(int offset=2;offset>=-2;offset--){BlockPos feet=base.offset(0,offset,0);if(!level.getBlockState(feet.below()).isAir()){next=new Vec3(next.x,feet.getY(),next.z);break;}}setPos(next.x,next.y,next.z);level.sendParticles(ParticleTypes.FLASH,getX(),getY()+.2,getZ(),1,0,0,0,0);level.sendParticles(ParticleTypes.BUBBLE_POP,getX(),getY()+.2,getZ(),8,.2,.3,.2,.01);level.sendParticles(ParticleTypes.SMOKE,getX(),getY()+.2,getZ(),1,.07,.07,.07,0);level.playSound(null,blockPosition(),SoundEvents.GRASS_BREAK,SoundSource.PLAYERS,.2F,1.4F);for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(position(),position()).inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&hitTargets.add(e.getUUID()))){target.hurt(owner.damageSources().playerAttack(owner),6.0F);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,3),owner);}if(tickCount>=7)discard();}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=new Vec3(tag.getDouble("DX"),0,tag.getDouble("DZ"));}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putDouble("DX",direction.x);tag.putDouble("DZ",direction.z);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
