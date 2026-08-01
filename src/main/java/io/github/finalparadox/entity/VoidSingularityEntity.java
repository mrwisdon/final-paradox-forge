package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;
import java.util.UUID;

public final class VoidSingularityEntity extends Entity {
    @Nullable private UUID ownerId;
    private double landingY;
    public VoidSingularityEntity(EntityType<VoidSingularityEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void spawn(ServerPlayer owner){VoidSingularityEntity entity=new VoidSingularityEntity(ModEntities.VOID_SINGULARITY.get(),owner.level());entity.ownerId=owner.getUUID();entity.setPos(owner.getX(),owner.getY()+1,owner.getZ());entity.landingY=findLandingY(owner.serverLevel(),owner.blockPosition());entity.setYRot(owner.getYRot());owner.level().addFreshEntity(entity);owner.level().playSound(null,owner.blockPosition(),SoundEvents.BEACON_ACTIVATE,SoundSource.PLAYERS,1,.5F);}
    private static double findLandingY(ServerLevel level, BlockPos start){for(BlockPos pos=start;pos.getY()>=level.getMinBuildHeight()&&pos.getY()>=start.getY()-16;pos=pos.below())if(!level.getBlockState(pos).getCollisionShape(level,pos).isEmpty())return pos.getY()+1D;return start.getY();}
    public boolean isOwnedBy(ServerPlayer player){return player.getUUID().equals(ownerId);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)){discard();return;}if(tickCount<=12){setPos(getX(),Math.max(landingY,getY()-.8),getZ());server.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+.1,getZ(),1,0,0,0,.02);if(tickCount==12)server.sendParticles(ParticleTypes.EXPLOSION,getX(),getY(),getZ(),1,0,0,0,0);return;}if(tickCount>112){discard();return;}ServerPlayer owner=owner(server);if(owner==null){discard();return;}server.sendParticles(ParticleTypes.FIREWORK,getX(),getY()+1,getZ(),1,.8,0,.8,.1);for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(7),e->GlaivorusAbilityState.isHostileTarget(owner,e))){Vec3 pull=position().subtract(target.position());double length=Math.max(.1,pull.horizontalDistance());target.push(pull.x/length*.10,0,pull.z/length*.10);if(tickCount%20==0&&target.distanceToSqr(this)<=2.25)target.addEffect(new MobEffectInstance(MobEffects.WITHER,40,0));}}
    @Nullable private ServerPlayer owner(ServerLevel level){return ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");landingY=tag.getDouble("LandingY");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putDouble("LandingY",landingY);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
