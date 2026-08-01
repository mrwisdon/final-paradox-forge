package io.github.finalparadox.entity;

import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;
import java.util.UUID;

public final class SoulSplitEntity extends Entity {
    private UUID ownerId,targetId;private Vec3 redPos=Vec3.ZERO,greenPos=Vec3.ZERO,redVelocity=Vec3.ZERO,greenVelocity=Vec3.ZERO;private boolean redCollected,greenCollected;
    public SoulSplitEntity(EntityType<SoulSplitEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static boolean spawn(ServerPlayer owner,LivingEntity target){ServerLevel level=owner.serverLevel();if(!level.getEntitiesOfClass(SoulSplitEntity.class,owner.getBoundingBox().inflate(128),e->owner.getUUID().equals(e.ownerId)).isEmpty())return false;SoulSplitEntity soul=new SoulSplitEntity(ModEntities.SOUL_SPLIT.get(),level);soul.ownerId=owner.getUUID();soul.targetId=target.getUUID();soul.setPos(target.getX(),target.getY()+1,target.getZ());double a=owner.getRandom().nextDouble()*Math.PI*2,b=a+Math.PI;soul.redPos=soul.position();soul.greenPos=soul.position();soul.redVelocity=new Vec3(Math.cos(a)*.09,.16,Math.sin(a)*.09);soul.greenVelocity=new Vec3(Math.cos(b)*.09,.16,Math.sin(b)*.09);level.addFreshEntity(soul);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,3),owner);level.sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);level.playSound(null,target.blockPosition(),SoundEvents.ZOMBIE_VILLAGER_CURE,SoundSource.PLAYERS,.2F,1.7F);return true;}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}LivingEntity target=targetId==null?null:(level.getEntity(targetId) instanceof LivingEntity living?living:null);if(target==null||!target.isAlive()){finish(level,target);return;}if(tickCount>120){discard();return;}if(!redCollected){redVelocity=advance(redPos,redVelocity);redPos=next(redPos,redVelocity);emit(level,redPos,true);if(hasCollector(redPos)){redCollected=true;pickup(level,redPos,true);}}if(!greenCollected){greenVelocity=advance(greenPos,greenVelocity);greenPos=next(greenPos,greenVelocity);emit(level,greenPos,false);if(hasCollector(greenPos)){greenCollected=true;pickup(level,greenPos,false);}}if((redCollected&&greenCollected))finish(level,target);else{if(random.nextInt(5)==0)emit(level,target.position().add(0,1,0),true);if(random.nextInt(5)==0)emit(level,target.position().add(0,1,0),false);}}
    private Vec3 advance(Vec3 pos,Vec3 velocity){BlockPos below=BlockPos.containing(pos.x,pos.y-.12,pos.z);if(!level().getBlockState(below).getCollisionShape(level(),below).isEmpty()){double x=Math.abs(velocity.x)<.01?0:velocity.x*.25,z=Math.abs(velocity.z)<.01?0:velocity.z*.25;return new Vec3(x,0,z);}return velocity.add(0,-.04,0);}
    private static Vec3 next(Vec3 pos,Vec3 velocity){return pos.add(velocity);}
    private boolean hasCollector(Vec3 pos){return !level().getEntitiesOfClass(Player.class,new AABB(pos,pos).inflate(.8),Player::isAlive).isEmpty();}
    private static void emit(ServerLevel level,Vec3 pos,boolean red){Vector3f color=red?new Vector3f(.78F,.4F,1F):new Vector3f(.008F,1F,.588F);level.sendParticles(new DustParticleOptions(color,2F),pos.x,pos.y+.3,pos.z,1,0,0,0,0);}
    private void pickup(ServerLevel level,Vec3 pos,boolean red){emit(level,pos,red);level.playSound(null,BlockPos.containing(pos),SoundEvents.SOUL_SOIL_BREAK,SoundSource.PLAYERS,1,1);for(Player player:level.getEntitiesOfClass(Player.class,new AABB(pos,pos).inflate(.8),Player::isAlive))player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,20,1));}
    private void finish(ServerLevel level,LivingEntity target){ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner!=null){owner.heal(8);owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,40,1));level.sendParticles(ParticleTypes.HEART,owner.getX(),owner.getY()+1,owner.getZ(),4,.3,.5,.3,0);if(target!=null&&target.isAlive())target.hurt(owner.damageSources().playerAttack(owner),99);}if(target!=null){level.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),6,.7,.5,.7,0);level.sendParticles(ParticleTypes.SOUL,target.getX(),target.getY()+1,target.getZ(),15,0,0,0,.3);level.playSound(null,target.blockPosition(),SoundEvents.PHANTOM_BITE,SoundSource.PLAYERS,1,.4F);}discard();}
    @Override protected void readAdditionalSaveData(CompoundTag t){if(t.hasUUID("Owner"))ownerId=t.getUUID("Owner");if(t.hasUUID("Target"))targetId=t.getUUID("Target");redPos=vec(t,"RedPos");greenPos=vec(t,"GreenPos");redVelocity=vec(t,"RedVel");greenVelocity=vec(t,"GreenVel");redCollected=t.getBoolean("RedDone");greenCollected=t.getBoolean("GreenDone");}
    @Override protected void addAdditionalSaveData(CompoundTag t){if(ownerId!=null)t.putUUID("Owner",ownerId);if(targetId!=null)t.putUUID("Target",targetId);putVec(t,"RedPos",redPos);putVec(t,"GreenPos",greenPos);putVec(t,"RedVel",redVelocity);putVec(t,"GreenVel",greenVelocity);t.putBoolean("RedDone",redCollected);t.putBoolean("GreenDone",greenCollected);}
    private static void putVec(CompoundTag t,String k,Vec3 v){t.putDouble(k+"X",v.x);t.putDouble(k+"Y",v.y);t.putDouble(k+"Z",v.z);}private static Vec3 vec(CompoundTag t,String k){return new Vec3(t.getDouble(k+"X"),t.getDouble(k+"Y"),t.getDouble(k+"Z"));}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
