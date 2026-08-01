package io.github.finalparadox.entity;

import io.github.finalparadox.item.HeavyArbalestItem;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class HeavyArbalestBurstEntity extends Entity {
    private UUID ownerId; private Vec3 velocity=Vec3.ZERO; private int volleys;
    public HeavyArbalestBurstEntity(EntityType<HeavyArbalestBurstEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void start(ServerPlayer owner,Vec3 origin,Vec3 velocity){fireVolley(owner.serverLevel(),owner,origin,velocity,true);HeavyArbalestBurstEntity burst=new HeavyArbalestBurstEntity(ModEntities.HEAVY_ARBALEST_BURST.get(),owner.serverLevel());burst.ownerId=owner.getUUID();burst.velocity=velocity;burst.volleys=1;burst.setPos(origin);owner.serverLevel().addFreshEntity(burst);}
    public static void fireVolley(ServerLevel level,ServerPlayer owner,Vec3 origin,Vec3 velocity,boolean piercing){Vec3 flat=new Vec3(-velocity.z,0,velocity.x);if(flat.lengthSqr()<.001)flat=new Vec3(1,0,0);flat=flat.normalize();for(int side=-2;side<=2;side++){Arrow arrow=new Arrow(level,owner);Vec3 pos=origin.add(flat.scale(side));arrow.setPos(pos.x,pos.y,pos.z);arrow.setDeltaMovement(velocity);arrow.setBaseDamage(5.0D);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;if(piercing)arrow.setPierceLevel((byte)4);arrow.getPersistentData().putBoolean(HeavyArbalestItem.ARROW_KEY,true);level.addFreshEntity(arrow);}level.playSound(null,owner.blockPosition(),SoundEvents.CROSSBOW_SHOOT,SoundSource.PLAYERS,1.0F,1.1F);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null){discard();return;}if(tickCount%4==0){fireVolley(level,owner,position(),velocity,true);volleys++;level.sendParticles(ParticleTypes.SMOKE,getX(),getY(),getZ(),5,.3,.3,.3,.03);if(volleys>=8)discard();}}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");velocity=new Vec3(tag.getDouble("X"),tag.getDouble("Y"),tag.getDouble("Z"));volleys=tag.getInt("Volleys");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putDouble("X",velocity.x);tag.putDouble("Y",velocity.y);tag.putDouble("Z",velocity.z);tag.putInt("Volleys",volleys);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
