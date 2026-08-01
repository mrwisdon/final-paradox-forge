package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class OmegaTechniqueEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int DASH=1, SWEEP=2;private UUID ownerId;private int mode;private boolean reforged;private Vec3 direction=Vec3.ZERO;private final Set<UUID> hit=new HashSet<>();
    public OmegaTechniqueEntity(EntityType<OmegaTechniqueEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void spawn(ServerPlayer owner,int mode,boolean reforged){OmegaTechniqueEntity e=new OmegaTechniqueEntity(ModEntities.OMEGA_TECHNIQUE.get(),owner.serverLevel());e.ownerId=owner.getUUID();e.mode=mode;e.reforged=reforged;e.direction=owner.getLookAngle().multiply(1,0,1);if(e.direction.lengthSqr()<.01)e.direction=Vec3.directionFromRotation(0,owner.getYRot());e.direction=e.direction.normalize();e.setYRot(owner.getYRot());e.setPos(owner.position());owner.serverLevel().addFreshEntity(e);owner.level().playSound(null,owner.blockPosition(),mode==DASH?SoundEvents.TRIDENT_RIPTIDE_2:SoundEvents.TRIDENT_THROW,SoundSource.PLAYERS,1,mode==DASH?1.2F:.5F);}
    public int mode(){return mode;}public ItemStack displayStack(){return (reforged?ModItems.REFORGED_OMEGA_TRIDENT:ModItems.KOYOMI_OMEGA_TRIDENT).get().getDefaultInstance();}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)){discard();return;}ServerPlayer owner=ownerId==null?null:server.getServer().getPlayerList().getPlayer(ownerId);if(owner==null){discard();return;}setPos(owner.position().add(0,1,0));if(mode==DASH)tickDash(server,owner);else tickSweep(server,owner);}
    private void tickDash(ServerLevel server,ServerPlayer owner){if(tickCount<=8){for(int i=0;i<5;i++){Vec3 step=direction.scale(.3);if(owner.level().noCollision(owner,owner.getBoundingBox().move(step)))owner.teleportTo(owner.getX()+step.x,owner.getY(),owner.getZ()+step.z);}server.sendParticles(ParticleTypes.END_ROD,owner.getX(),owner.getY()+1,owner.getZ(),5,.35,.5,.35,.05);for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,owner.getBoundingBox().inflate(2.75),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&hit.add(e.getUUID()))){target.hurt(owner.damageSources().playerAttack(owner),reforged?15:9);target.setDeltaMovement(0,.3,0);server.sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);server.playSound(null,target.blockPosition(),SoundEvents.TRIDENT_HIT,SoundSource.PLAYERS,1,.8F);}}if(tickCount>=16)discard();}
    private void tickSweep(ServerLevel server,ServerPlayer owner){if(tickCount<=7){for(int i=0;i<10;i++){double angle=Math.toRadians(-80+i*18+tickCount*7);Vec3 right=new Vec3(direction.z,0,-direction.x);Vec3 p=owner.position().add(direction.scale(Math.cos(angle)*5)).add(right.scale(Math.sin(angle)*5)).add(0,1,0);server.sendParticles(ParticleTypes.END_ROD,p.x,p.y,p.z,1,0,0,0,0);}}if(tickCount==5){for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,owner.getBoundingBox().inflate(5.5),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&front(owner,e,-.15))){target.hurt(owner.damageSources().playerAttack(owner),reforged?16:10);push(owner,target,.9);target.setDeltaMovement(target.getDeltaMovement().add(0,.3,0));server.sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);}}if(tickCount>=7&&tickCount<=13){for(int i=1;i<=8;i++){Vec3 p=owner.position().add(direction.scale(i)).add(0,1,0);server.sendParticles(ParticleTypes.END_ROD,p.x,p.y,p.z,1,0,0,0,0);}}if(tickCount==9){Vec3 center=owner.position().add(direction.scale(4)).add(0,1,0);for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(4,2,4),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&front(owner,e,.7))){target.hurt(owner.damageSources().playerAttack(owner),reforged?33:18);push(owner,target,1.5);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,20,0),owner);server.sendParticles(ParticleTypes.EXPLOSION,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);}}if(tickCount>=15)discard();}
    private static boolean front(ServerPlayer owner,LivingEntity e,double dot){Vec3 to=e.position().subtract(owner.position()).multiply(1,0,1);return to.lengthSqr()>.01&&owner.getLookAngle().multiply(1,0,1).normalize().dot(to.normalize())>dot;}
    private static void push(ServerPlayer owner,LivingEntity e,double force){Vec3 v=e.position().subtract(owner.position()).multiply(1,0,1);if(v.lengthSqr()>.01){v=v.normalize().scale(force);e.setDeltaMovement(v.x,.3,v.z);}}
    @Override protected void readAdditionalSaveData(CompoundTag t){if(t.hasUUID("Owner"))ownerId=t.getUUID("Owner");mode=t.getInt("Mode");reforged=t.getBoolean("Reforged");direction=new Vec3(t.getDouble("DX"),t.getDouble("DY"),t.getDouble("DZ"));} @Override protected void addAdditionalSaveData(CompoundTag t){if(ownerId!=null)t.putUUID("Owner",ownerId);t.putInt("Mode",mode);t.putBoolean("Reforged",reforged);t.putDouble("DX",direction.x);t.putDouble("DY",direction.y);t.putDouble("DZ",direction.z);} @Override public void writeSpawnData(FriendlyByteBuf b){b.writeInt(mode);b.writeBoolean(reforged);} @Override public void readSpawnData(FriendlyByteBuf b){mode=b.readInt();reforged=b.readBoolean();} @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
