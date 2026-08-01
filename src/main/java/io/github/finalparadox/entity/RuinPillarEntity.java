package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class RuinPillarEntity extends Entity {
    public RuinPillarEntity(EntityType<RuinPillarEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void spawn(Level level,Vec3 point,float yaw){RuinPillarEntity pillar=new RuinPillarEntity(ModEntities.RUIN_PILLAR.get(),level);pillar.setPos(point);pillar.setYRot(yaw);level.addFreshEntity(pillar);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount==40)setPos(getX(),getY()-.6,getZ());if(!level().isClientSide&&tickCount>=45)discard();}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
