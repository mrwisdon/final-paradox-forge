package io.github.finalparadox.entity;

import io.github.finalparadox.item.GreatHookItem;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public final class GreatHookEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final double MAX_DISTANCE=19.24D;
    private static final int RELEASE_LEVITATION_TICKS=60;
    private static final int RELEASE_LEVITATION_AMPLIFIER=1;
    private UUID ownerId; private Vec3 start=Vec3.ZERO; private Vec3 direction=Vec3.ZERO; private Vec3 holdPosition=Vec3.ZERO; private boolean anchored; private boolean resting; private boolean cancelled;
    public GreatHookEntity(EntityType<GreatHookEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static boolean toggleOrSpawn(ServerPlayer owner){for(GreatHookEntity hook:owner.serverLevel().getEntitiesOfClass(GreatHookEntity.class,owner.getBoundingBox().inflate(32),e->owner.getUUID().equals(e.ownerId))){hook.cancelled=true;return true;}if(owner.getPersistentData().getInt(GreatHookItem.COOLDOWN_KEY)>0)return false;GreatHookEntity hook=new GreatHookEntity(ModEntities.GREAT_HOOK.get(),owner.serverLevel());hook.ownerId=owner.getUUID();hook.start=owner.getEyePosition();hook.direction=owner.getLookAngle().normalize();hook.setPos(hook.start);owner.serverLevel().addFreshEntity(hook);owner.level().playSound(null,owner.blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.PLAYERS,1,.8F);owner.level().playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_RIPTIDE_1,SoundSource.PLAYERS,.7F,1.4F);return true;}
    @Nullable public Entity ownerForRender(){return ownerId==null?null:level().getPlayerByUUID(ownerId);}
    public boolean isAnchored(){return anchored;}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null||owner.serverLevel()!=level){discard();return;}if(cancelled){finish(owner,anchored,resting);return;}if(!anchored){Vec3 next=position().add(direction.scale(1.04D));HitResult hit=level.clip(new ClipContext(position(),next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.MISS){setPos(hit.getLocation());anchored=true;level.sendParticles(ParticleTypes.EXPLOSION,getX(),getY(),getZ(),1,0,0,0,0);level.playSound(null,blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.PLAYERS,2,2);return;}setPos(next.x,next.y,next.z);if(position().distanceTo(start)>=MAX_DISTANCE){level.playSound(null,blockPosition(),SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1,2);discard();}return;}if(resting){owner.teleportTo(holdPosition.x,holdPosition.y,holdPosition.z);owner.setDeltaMovement(Vec3.ZERO);owner.hurtMarked=true;owner.fallDistance=0;return;}Vec3 target=position().subtract(owner.position().add(0,.5,0));if(target.lengthSqr()<=1.5D){resting=true;holdPosition=owner.position();owner.setDeltaMovement(Vec3.ZERO);owner.hurtMarked=true;owner.fallDistance=0;return;}owner.setDeltaMovement(target.normalize().scale(.9D));owner.hurtMarked=true;owner.fallDistance=0;level.sendParticles(ParticleTypes.CRIT,owner.getX(),owner.getY()+1,owner.getZ(),2,.1,.1,.1,.1);}
    private void finish(ServerPlayer owner,boolean successful,boolean releaseFromRest){if(successful){owner.getPersistentData().putInt(GreatHookItem.COOLDOWN_KEY,GreatHookItem.COOLDOWN_TICKS);owner.setDeltaMovement(Vec3.ZERO);owner.fallDistance=0;if(releaseFromRest)owner.addEffect(new MobEffectInstance(MobEffects.LEVITATION,RELEASE_LEVITATION_TICKS,RELEASE_LEVITATION_AMPLIFIER));owner.level().playSound(null,owner.blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,1,.8F);}discard();}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");start=new Vec3(tag.getDouble("SX"),tag.getDouble("SY"),tag.getDouble("SZ"));direction=new Vec3(tag.getDouble("DX"),tag.getDouble("DY"),tag.getDouble("DZ"));holdPosition=new Vec3(tag.getDouble("HX"),tag.getDouble("HY"),tag.getDouble("HZ"));anchored=tag.getBoolean("Anchored");resting=tag.getBoolean("Resting");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putDouble("SX",start.x);tag.putDouble("SY",start.y);tag.putDouble("SZ",start.z);tag.putDouble("DX",direction.x);tag.putDouble("DY",direction.y);tag.putDouble("DZ",direction.z);tag.putDouble("HX",holdPosition.x);tag.putDouble("HY",holdPosition.y);tag.putDouble("HZ",holdPosition.z);tag.putBoolean("Anchored",anchored);tag.putBoolean("Resting",resting);}
    @Override public void writeSpawnData(FriendlyByteBuf buf){buf.writeBoolean(ownerId!=null);if(ownerId!=null)buf.writeUUID(ownerId);}
    @Override public void readSpawnData(FriendlyByteBuf buf){if(buf.readBoolean())ownerId=buf.readUUID();}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
