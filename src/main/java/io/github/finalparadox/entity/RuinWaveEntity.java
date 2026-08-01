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

public final class RuinWaveEntity extends Entity {
    private UUID ownerId;private Vec3 direction=Vec3.ZERO;private final Set<UUID> hitTargets=new HashSet<>();
    public RuinWaveEntity(EntityType<RuinWaveEntity> type,Level level){super(type,level);noPhysics=true;}
    public static void spawn(ServerPlayer owner){Vec3 direction=owner.getLookAngle().multiply(1,0,1);if(direction.lengthSqr()<.001)direction=new Vec3(0,0,1);direction=direction.normalize();RuinWaveEntity wave=new RuinWaveEntity(ModEntities.RUIN_WAVE.get(),owner.serverLevel());wave.ownerId=owner.getUUID();wave.direction=direction;wave.setPos(owner.position().add(direction.scale(1.2)));owner.serverLevel().addFreshEntity(wave);owner.serverLevel().playSound(null,owner.blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,SoundSource.PLAYERS,1,.55F);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null){discard();return;}if(tickCount>18){discard();return;}Vec3 base=position().add(direction.scale(.6));setPos(adjustToTerrain(level,base));Vec3 right=new Vec3(-direction.z,0,direction.x);boolean first=(tickCount&1)==0;double randomSide=random.nextDouble()*.5+random.nextDouble()*.5-random.nextDouble()*.25;double lateral=first?1+randomSide:-1-randomSide;double longitudinal=(random.nextDouble()-.5)*.4;double vertical=-.4*(random.nextDouble()+random.nextDouble()+random.nextDouble());Vec3 point=position().add(right.scale(lateral)).add(direction.scale(longitudinal)).add(0,vertical-1.5,0);float yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z))+(float)((first?-1:1)*(random.nextDouble()*4+random.nextDouble()*4+random.nextDouble()*3));RuinPillarEntity.spawn(level,point,yaw);Vec3 damageCenter=point.add(0,1.5,0);for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(damageCenter,damageCenter).inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID())&&e.position().add(0,e.getBbHeight()*.5,0).distanceToSqr(damageCenter)<=4)){if(ArcaneTechniqueEntity.abilityDamage(owner,target,30)){hitTargets.add(target.getUUID());target.setDeltaMovement(target.getDeltaMovement().x,1,target.getDeltaMovement().z);}}level.sendParticles(ParticleTypes.POOF,point.x,point.y+1.7,point.z,10,.6,.2,.6,.08);level.playSound(null,BlockPos.containing(point),SoundEvents.STONE_PLACE,SoundSource.PLAYERS,.8F,.65F);}
    private static Vec3 adjustToTerrain(ServerLevel level,Vec3 point){BlockPos.MutableBlockPos pos=BlockPos.containing(point).mutable();for(int i=0;i<3;i++){if(level.getBlockState(pos.below()).isAir())pos.move(0,-1,0);else if(!level.getBlockState(pos).isAir())pos.move(0,1,0);}return new Vec3(point.x,pos.getY(),point.z);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=new Vec3(tag.getDouble("DX"),0,tag.getDouble("DZ"));}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putDouble("DX",direction.x);tag.putDouble("DZ",direction.z);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
