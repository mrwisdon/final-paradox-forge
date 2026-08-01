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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;

public final class FireRainEntity extends Entity {
    private UUID ownerId;private final List<Vec3> impacts=new ArrayList<>();private final Set<UUID> struck=new HashSet<>();
    public FireRainEntity(EntityType<FireRainEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void spawn(ServerPlayer owner){ServerLevel level=owner.serverLevel();for(FireRainEntity old:level.getEntitiesOfClass(FireRainEntity.class,owner.getBoundingBox().inflate(128),e->owner.getUUID().equals(e.ownerId)))old.discard();Vec3 look=owner.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);if(forward.lengthSqr()<.001)forward=new Vec3(0,0,1);forward=forward.normalize();Vec3 side=new Vec3(-forward.z,0,forward.x);Vec3 visualCenter=owner.position().add(forward.scale(16));FireRainEntity rain=new FireRainEntity(ModEntities.FIRE_RAIN.get(),level);rain.ownerId=owner.getUUID();rain.setPos(visualCenter.x,findGround(level,visualCenter.x,owner.getY()+4,visualCenter.z),visualCenter.z);
        // Original datapack: five forward chunks, four blocks apart. Each chunk
        // builds a 5x5 grid at two-block spacing and randomly keeps five meteors.
        for(int wave=0;wave<5;wave++){Vec3 waveCenter=owner.position().add(forward.scale(8+wave*4));List<Integer> cells=new ArrayList<>();for(int i=0;i<25;i++)cells.add(i);Collections.shuffle(cells,new java.util.Random(owner.getRandom().nextLong()));for(int pick=0;pick<5;pick++){int cell=cells.get(pick),row=cell/5,column=cell%5;Vec3 point=waveCenter.add(side.scale((column-2)*2D)).add(forward.scale((row-2)*2D));double x=point.x+(owner.getRandom().nextDouble()-.5)*.8,z=point.z+(owner.getRandom().nextDouble()-.5)*.8;rain.impacts.add(new Vec3(x,findGround(level,x,owner.getY()+6,z),z));}}
        level.addFreshEntity(rain);level.playSound(null,owner.blockPosition(),SoundEvents.VEX_CHARGE,SoundSource.PLAYERS,2,1);level.playSound(null,owner.blockPosition(),SoundEvents.BAT_TAKEOFF,SoundSource.PLAYERS,2,1);}
    private static double findGround(ServerLevel level,double x,double startY,double z){BlockPos.MutableBlockPos p=new BlockPos.MutableBlockPos((int)Math.floor(x),(int)Math.floor(startY),(int)Math.floor(z));for(int i=0;i<12&&p.getY()>level.getMinBuildHeight();i++,p.move(0,-1,0))if(!level.getBlockState(p.below()).getCollisionShape(level,p.below()).isEmpty())return p.getY();return startY;}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null){discard();return;}if(tickCount<=20){for(int i=0;i<8;i++){Vec3 p=impacts.get(random.nextInt(impacts.size()));level.sendParticles(ParticleTypes.FALLING_LAVA,p.x,p.y+8+random.nextDouble()*4,p.z,1,0,-1,0,.2);}level.sendParticles(ParticleTypes.SMOKE,getX(),getY()+4,getZ(),8,5,4,12,.02);}if(tickCount>=20&&tickCount<=22)for(Vec3 impact:impacts){double progress=(tickCount-19)/3D;double x=impact.x,y=impact.y+12*(1-progress),z=impact.z-6*(1-progress);level.sendParticles(ParticleTypes.FLAME,x,y,z,3,.1,.5,.1,.05);level.sendParticles(ParticleTypes.SMOKE,x,y,z,2,.1,.5,.1,.05);}if(tickCount==23){for(Vec3 impact:impacts)explode(level,owner,impact);discard();}}
    private void explode(ServerLevel level,ServerPlayer owner,Vec3 p){level.sendParticles(ParticleTypes.EXPLOSION,p.x,p.y+.1,p.z,1,0,0,0,0);level.sendParticles(ParticleTypes.FLAME,p.x,p.y+.1,p.z,24,.8,.1,.8,.05);level.playSound(null,BlockPos.containing(p),SoundEvents.FIREWORK_ROCKET_BLAST,SoundSource.PLAYERS,2,.9F);for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(p,p).inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&struck.add(e.getUUID()))){target.hurt(owner.damageSources().playerAttack(owner),30);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2),owner);target.setSecondsOnFire(5);target.setDeltaMovement(0,.6,0);}}
    @Override protected void readAdditionalSaveData(CompoundTag t){if(t.hasUUID("Owner"))ownerId=t.getUUID("Owner");impacts.clear();int n=t.getInt("ImpactCount");for(int i=0;i<n;i++)impacts.add(new Vec3(t.getDouble("IX"+i),t.getDouble("IY"+i),t.getDouble("IZ"+i)));}
    @Override protected void addAdditionalSaveData(CompoundTag t){if(ownerId!=null)t.putUUID("Owner",ownerId);t.putInt("ImpactCount",impacts.size());for(int i=0;i<impacts.size();i++){Vec3 p=impacts.get(i);t.putDouble("IX"+i,p.x);t.putDouble("IY"+i,p.y);t.putDouble("IZ"+i,p.z);}}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
