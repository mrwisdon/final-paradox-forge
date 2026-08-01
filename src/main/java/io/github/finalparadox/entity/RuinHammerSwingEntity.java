package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.item.RuinCreatorItem;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
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

public final class RuinHammerSwingEntity extends Entity {
    private UUID ownerId; private int direction=1; private final Set<UUID> hitTargets=new HashSet<>();
    public RuinHammerSwingEntity(EntityType<RuinHammerSwingEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static void activate(ServerPlayer owner){
        if(owner.isCrouching()&&owner.getPersistentData().getInt(RuinCreatorItem.CHARGE_KEY)>=12){owner.getPersistentData().putInt(RuinCreatorItem.CHARGE_KEY,0);RuinWaveEntity.spawn(owner);return;}
        if(owner.isCrouching()||hasActive(owner))return;
        RuinHammerSwingEntity swing=new RuinHammerSwingEntity(ModEntities.RUIN_HAMMER_SWING.get(),owner.serverLevel());swing.ownerId=owner.getUUID();int side=owner.getPersistentData().getInt("finalparadox.ruin_hammer_side");swing.direction=side==0?1:-1;owner.getPersistentData().putInt("finalparadox.ruin_hammer_side",side==0?1:0);swing.setPos(owner.position());owner.serverLevel().addFreshEntity(swing);
    }
    private static boolean hasActive(ServerPlayer owner){return !owner.serverLevel().getEntitiesOfClass(RuinHammerSwingEntity.class,owner.getBoundingBox().inflate(16),e->owner.getUUID().equals(e.ownerId)).isEmpty();}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel level)){discard();return;}ServerPlayer owner=ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);if(owner==null||!owner.isUsingItem()||!owner.getUseItem().is(ModItems.RUIN_CREATOR.get())||owner.isCrouching()){discard();return;}int cycle=(tickCount-1)%16;if(cycle==0&&tickCount>1){direction=-direction;hitTargets.clear();}setPos(owner.getX(),owner.getY()+.1,owner.getZ());setYRot(owner.getYRot()+direction*(190-cycle*20));if(cycle<4)return;Vec3 forward=Vec3.directionFromRotation(0,getYRot()).scale(2.6),center=position().add(forward);for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(1.55),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID()))){if(target.hurt(owner.damageSources().playerAttack(owner),10)){hitTargets.add(target.getUUID());RuinCreatorItem.recordHit(owner);Vec3 push=target.position().subtract(owner.position()).multiply(1,0,1);if(push.lengthSqr()<.01)push=forward;push=push.normalize();target.push(push.x*.85,.25,push.z*.85);level.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);level.playSound(null,target.blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.PLAYERS,.7F,.8F);}}}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=tag.getInt("Direction");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putInt("Direction",direction);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
