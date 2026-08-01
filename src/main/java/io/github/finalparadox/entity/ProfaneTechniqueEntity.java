package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.ability.ProfaneStanceState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ProfaneTechniqueEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int ARS_GRAVITUM=1, PROFANE_SLASH=2, DARK_CHARGE=3;
    private static final DustParticleOptions PURPLE_BRIGHT=new DustParticleOptions(new Vector3f(.675F,0F,.878F),.5F);
    private static final DustParticleOptions PURPLE_MID=new DustParticleOptions(new Vector3f(.325F,0F,.427F),.65F);
    private static final DustParticleOptions PURPLE_DARK=new DustParticleOptions(new Vector3f(.09F,0F,.122F),.74F);
    private static final DustParticleOptions BLACK=new DustParticleOptions(new Vector3f(0F,0F,0F),1F);
    private UUID ownerId;
    private int mode;
    private Vec3 origin=Vec3.ZERO;
    private float baseYaw;
    private Vec3 gravityMarker=Vec3.ZERO;
    private float gravityYaw,gravityPitch;
    private int gravitySteps;
    private Vec3 gravityImpact=Vec3.ZERO;
    private final int[] slashSteps=new int[3];
    private final float[] slashYaw=new float[3];
    private final float[] slashPitch=new float[3];
    private final Map<UUID,Integer> slashCooldowns=new HashMap<>();
    private final int[] chargePreSteps=new int[2];
    private final float[] chargePreYaw=new float[2];
    private final float[] chargePrePitch=new float[2];
    private final int[] chargePostSteps=new int[2];
    private final float[] chargePostYaw=new float[2];
    private final float[] chargePostPitch=new float[2];
    private final Set<UUID> chargeTargets=new HashSet<>();

    public ProfaneTechniqueEntity(EntityType<ProfaneTechniqueEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}

    public static void spawn(ServerPlayer owner,int mode){
        ProfaneTechniqueEntity entity=new ProfaneTechniqueEntity(ModEntities.PROFANE_TECHNIQUE.get(),owner.serverLevel());
        entity.ownerId=owner.getUUID();entity.mode=mode;entity.origin=owner.position();entity.baseYaw=owner.getYRot();entity.setYRot(owner.getYRot());entity.setPos(owner.position());entity.initializeAnimation();owner.serverLevel().addFreshEntity(entity);
    }

    private void initializeAnimation(){
        gravityYaw=baseYaw-12;gravityPitch=0;gravityMarker=local(origin,gravityYaw,0,-1.2,0,-3.5);
        slashYaw[0]=baseYaw+90;slashPitch[0]=20;
        slashYaw[1]=baseYaw-90;slashPitch[1]=-20;
        slashYaw[2]=baseYaw-90;slashPitch[2]=20;
        chargePreYaw[0]=baseYaw+90;chargePreYaw[1]=baseYaw-90;chargePrePitch[0]=chargePrePitch[1]=-20;
        chargePostYaw[0]=baseYaw+90;chargePostYaw[1]=baseYaw-90;chargePostPitch[0]=chargePostPitch[1]=20;
    }

    public int mode(){return mode;}
    @Override protected void defineSynchedData(){}

    @Override public void tick(){
        super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)){discard();return;}
        ServerPlayer owner=ownerId==null?null:server.getServer().getPlayerList().getPlayer(ownerId);if(owner==null){discard();return;}
        if(mode==ARS_GRAVITUM)tickGravity(server,owner);else if(mode==PROFANE_SLASH)tickSlashes(server,owner);else tickDarkCharge(server,owner);
    }

    private void tickGravity(ServerLevel server,ServerPlayer owner){
        if(tickCount<=9){
            int microsteps=tickCount<=2?1:10;
            for(int i=0;i<microsteps;i++){
                gravitySteps++;
                if(gravitySteps==31)gravityYaw+=180;
                if(gravitySteps<=30)gravityPitch-=3;else if(gravitySteps<=60)gravityPitch+=3;
                gravityMarker=local(gravityMarker,gravityYaw,0,-.02,0,.17);
                particleBlade(server,gravityMarker,gravityYaw,gravityPitch,false);
            }
            if(tickCount==3)server.playSound(null,blockPosition(),SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,2,0F);
            if(tickCount==9){
                gravityImpact=fitImpact(server,local(gravityMarker,gravityYaw,gravityPitch,.5,0,2));setPos(gravityImpact);
                server.sendParticles(ParticleTypes.EXPLOSION,gravityImpact.x,gravityImpact.y,gravityImpact.z,1,0,0,0,0);
                server.sendParticles(ParticleTypes.FLASH,gravityImpact.x,gravityImpact.y,gravityImpact.z,1,0,0,0,0);
                server.sendParticles(ParticleTypes.SQUID_INK,gravityImpact.x,gravityImpact.y,gravityImpact.z,32,1,.1,1,.5);
                server.playSound(null,blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,1,2F);
            }
        }
        if(tickCount>=10&&tickCount<=20&&(tickCount-10)%2==0)gravityFrame(server,owner,(tickCount-10)/2);
        if(tickCount>=20)discard();
    }

    private void gravityFrame(ServerLevel server,ServerPlayer owner,int frame){
        double rotation=Math.toRadians(frame*5);
        for(int i=0;i<32;i++){
            double angle=rotation+i*Math.PI/16,x=Math.cos(angle)*7,z=Math.sin(angle)*7;
            server.sendParticles(ParticleTypes.SQUID_INK,gravityImpact.x+x,gravityImpact.y-1,gravityImpact.z+z,0,-x/7,.1,-z/7,.7);
        }
        AABB area=new AABB(gravityImpact,gravityImpact).inflate(16);
        for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,area,e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!e.isInvulnerable()&&(!(e instanceof Mob mob)||!mob.isNoAi()))){
            for(int step=0;step<5;step++){
                Vec3 toward=gravityImpact.subtract(target.position()).multiply(1,0,1);if(toward.lengthSqr()<.01)break;Vec3 movement=toward.normalize().scale(.3);
                if(server.noCollision(target,target.getBoundingBox().move(movement)))target.teleportTo(target.getX()+movement.x,target.getY(),target.getZ()+movement.z);
            }
            if(target.position().distanceToSqr(gravityImpact)<=16)target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,4),owner);
        }
    }

    private void tickSlashes(ServerLevel server,ServerPlayer owner){
        decrementSlashCooldowns();
        int[] starts={2,11,6};
        for(int slash=0;slash<3;slash++){
            int score=tickCount-starts[slash]+1;if(score<1||score>12)continue;
            if(score==1)server.playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,2,slash==0?1.5F:slash==1?1F:1.2F);
            int microsteps=score<=2||score>=9?1:10;
            for(int i=0;i<microsteps;i++)slashMicrostep(server,owner,slash,score);
        }
        if(tickCount>=23)discard();
    }

    private void slashMicrostep(ServerLevel server,ServerPlayer owner,int slash,int score){
        int yawSign=slash==0?-1:1,pitchSign=slash==1?1:-1;
        slashYaw[slash]+=yawSign*3;slashSteps[slash]++;
        if(slashSteps[slash]<=59)slashPitch[slash]+=pitchSign*(float)Math.sin(Math.PI*slashSteps[slash]/60D);
        Vec3 core=origin.add(0,1.2,0);particleBlade(server,core,slashYaw[slash],slashPitch[slash],false);
        if(score>=3&&score<=8){hitSlashSphere(server,owner,local(core,slashYaw[slash],slashPitch[slash],0,0,2.5));hitSlashSphere(server,owner,local(core,slashYaw[slash],slashPitch[slash],0,0,5));}
    }

    private void hitSlashSphere(ServerLevel server,ServerPlayer owner,Vec3 center){
        for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(2.5),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&e.position().distanceToSqr(center)<=6.25&&!slashCooldowns.containsKey(e.getUUID()))){
            damage(owner,target,target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)?30:19);slashCooldowns.put(target.getUUID(),4);
            server.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.REDSTONE_BLOCK)),target.getX(),target.getY()+1,target.getZ(),12,0,0,0,.2);
            server.playSound(null,target.blockPosition(),SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,SoundSource.PLAYERS,1,.7F);
        }
    }

    private void decrementSlashCooldowns(){Iterator<Map.Entry<UUID,Integer>> it=slashCooldowns.entrySet().iterator();while(it.hasNext()){Map.Entry<UUID,Integer> entry=it.next();if(entry.getValue()<=1)it.remove();else entry.setValue(entry.getValue()-1);}}

    private void tickDarkCharge(ServerLevel server,ServerPlayer owner){
        if(tickCount==1){owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20,106,true,false));owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,220,1));owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,220,1));server.playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,2,1.5F);}
        if(tickCount<=12)chargeArcFrame(server,true,tickCount);
        if(tickCount<=5)moveOwner(owner,-.25);
        if(tickCount==9)server.playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_RIPTIDE_1,SoundSource.PLAYERS,2,1.2F);
        if(tickCount>=11&&tickCount<=14){
            for(int i=0;i<13;i++){moveOwner(owner,.25);Vec3 left=local(owner.position(),baseYaw,0,3,.1,3),right=local(owner.position(),baseYaw,0,-3,.1,3);particleLine(server,left);particleLine(server,right);particle(server,BLACK,left);particle(server,BLACK,right);for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,owner.getBoundingBox().inflate(4),e->GlaivorusAbilityState.isHostileTarget(owner,e)))chargeTargets.add(target.getUUID());}
        }
        if(tickCount>=16&&tickCount<=27){int score=tickCount-15;if(score==1){server.playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,1,0F);server.playSound(null,owner.blockPosition(),SoundEvents.WITHER_SHOOT,SoundSource.PLAYERS,1,.7F);}chargeArcFrame(server,false,score);if(score==5)finishChargeHits(server,owner);}
        if(tickCount>=28)discard();
    }

    private void chargeArcFrame(ServerLevel server,boolean pre,int score){
        if(score<1||score>12)return;int microsteps=score<=2||score>=9?1:10;
        for(int arm=0;arm<2;arm++)for(int i=0;i<microsteps;i++){
            int[] steps=pre?chargePreSteps:chargePostSteps;float[] yaws=pre?chargePreYaw:chargePostYaw;float[] pitches=pre?chargePrePitch:chargePostPitch;
            yaws[arm]+=(arm==0?-3:3);steps[arm]++;if(steps[arm]<=59)pitches[arm]+=(pre?1:-1)*(float)Math.sin(Math.PI*steps[arm]/60D);
            Vec3 core=(pre?origin:position()).add(0,1.2,0);particleBlade(server,core,yaws[arm],pitches[arm],pre);
        }
    }

    private void moveOwner(ServerPlayer owner,double amount){Vec3 forward=Vec3.directionFromRotation(0,baseYaw).multiply(1,0,1).normalize().scale(amount);if(owner.level().noCollision(owner,owner.getBoundingBox().move(forward)))owner.teleportTo(owner.getX()+forward.x,owner.getY(),owner.getZ()+forward.z);owner.setYRot(baseYaw);owner.setXRot(10);setPos(owner.position());}

    private void finishChargeHits(ServerLevel server,ServerPlayer owner){
        for(UUID id:new HashSet<>(chargeTargets)){Entity entity=server.getEntity(id);if(!(entity instanceof LivingEntity target)||!target.isAlive())continue;damage(owner,target,isBoss(target)?30:50);target.setDeltaMovement(0,.7,0);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,4),owner);server.sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);server.playSound(null,target.blockPosition(),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,SoundSource.PLAYERS,.7F,2F);}
        chargeTargets.clear();
    }

    private static void particleBlade(ServerLevel server,Vec3 core,float yaw,float pitch,boolean shortBlade){
        if(shortBlade){Vec3 tip=local(core,yaw,pitch,0,0,2);particle(server,PURPLE_DARK,tip);particleLine(server,tip);return;}
        particle(server,PURPLE_BRIGHT,local(core,yaw,pitch,0,0,3));particle(server,PURPLE_BRIGHT,local(core,yaw,pitch,0,0,3.75));particle(server,PURPLE_MID,local(core,yaw,pitch,0,0,4));particle(server,PURPLE_DARK,local(core,yaw,pitch,0,0,4.25));particleLine(server,local(core,yaw,pitch,0,0,4.5));
    }

    private static void particle(ServerLevel server,net.minecraft.core.particles.ParticleOptions particle,Vec3 pos){server.sendParticles(particle,pos.x,pos.y,pos.z,1,0,0,0,0);}
    private static void particleLine(ServerLevel server,Vec3 pos){server.sendParticles(ParticleTypes.END_ROD,pos.x,pos.y,pos.z,0,0,-1,0,999999);}

    private static Vec3 fitImpact(ServerLevel server,Vec3 pos){BlockPos.MutableBlockPos cursor=BlockPos.containing(pos).mutable();for(int i=0;i<4&&server.getBlockState(cursor.below()).isAir();i++)cursor.move(0,-1,0);for(int i=0;i<4&&(!server.getBlockState(cursor).isAir()||!server.getBlockState(cursor.above()).isAir());i++)cursor.move(0,1,0);return new Vec3(cursor.getX()+.5,cursor.getY()+1,cursor.getZ()+.5);}

    private static Vec3 local(Vec3 origin,float yaw,float pitch,double x,double y,double z){Vec3 forward=Vec3.directionFromRotation(pitch,yaw);Vec3 flat=Vec3.directionFromRotation(0,yaw);Vec3 localX=new Vec3(flat.z,0,-flat.x);return origin.add(localX.scale(x)).add(0,y,0).add(forward.scale(z));}
    private static boolean isBoss(LivingEntity target){return target instanceof EnderDragon||target instanceof WitherBoss||target.getTags().contains("boss");}
    private static void damage(ServerPlayer owner,LivingEntity target,float amount){owner.getPersistentData().putBoolean(ProfaneStanceState.ABILITY_DAMAGE,true);target.hurt(owner.damageSources().playerAttack(owner),amount);owner.getPersistentData().remove(ProfaneStanceState.ABILITY_DAMAGE);}

    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");mode=tag.getInt("Mode");origin=new Vec3(tag.getDouble("OX"),tag.getDouble("OY"),tag.getDouble("OZ"));baseYaw=tag.getFloat("Yaw");initializeAnimation();}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putInt("Mode",mode);tag.putDouble("OX",origin.x);tag.putDouble("OY",origin.y);tag.putDouble("OZ",origin.z);tag.putFloat("Yaw",baseYaw);}
    @Override public void writeSpawnData(FriendlyByteBuf buffer){buffer.writeInt(mode);}
    @Override public void readSpawnData(FriendlyByteBuf buffer){mode=buffer.readInt();}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
