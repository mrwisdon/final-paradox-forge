package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.item.CosmicExtinctionHammerItem;
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
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CosmicHammerSwingEntity extends Entity {
    @Nullable private UUID ownerId; private int direction=1; private final Set<UUID> hitTargets=new HashSet<>();
    public CosmicHammerSwingEntity(EntityType<CosmicHammerSwingEntity> type, Level level){super(type,level);noPhysics=true;noCulling=true;}
    public static boolean hasActive(ServerPlayer owner){return !owner.level().getEntitiesOfClass(CosmicHammerSwingEntity.class,owner.getBoundingBox().inflate(16),swing->swing.isOwnedBy(owner)).isEmpty();}
    public static void spawn(ServerPlayer owner){if(hasActive(owner))return;CosmicHammerSwingEntity swing=new CosmicHammerSwingEntity(ModEntities.COSMIC_HAMMER_SWING.get(),owner.level());swing.ownerId=owner.getUUID();int side=owner.getPersistentData().getInt("finalparadox.cosmic_hammer_side");swing.direction=side==0?1:-1;owner.getPersistentData().putInt("finalparadox.cosmic_hammer_side",side==0?1:0);swing.setPos(owner.position());owner.level().addFreshEntity(swing);owner.level().playSound(null,owner.blockPosition(),SoundEvents.PUFFER_FISH_BLOW_OUT,SoundSource.PLAYERS,1,0);}
    public boolean isOwnedBy(ServerPlayer player){return player.getUUID().equals(ownerId);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)){discard();return;}ServerPlayer owner=owner(server);if(owner==null||!owner.isUsingItem()||!owner.getUseItem().is(ModItems.COSMIC_EXTINCTION_HAMMER.get())||owner.isCrouching()){discard();return;}int cycle=(tickCount-1)%16;if(cycle==0&&tickCount>1){direction=-direction;hitTargets.clear();owner.level().playSound(null,owner.blockPosition(),SoundEvents.PUFFER_FISH_BLOW_OUT,SoundSource.PLAYERS,1,0);}setPos(owner.getX(),owner.getY()+.1,owner.getZ());setYRot(owner.getYRot()+direction*(190-cycle*20));if(cycle<4)return;Vec3 forward=Vec3.directionFromRotation(0,getYRot()).scale(2.5);Vec3 center=position().add(forward);AABB box=new AABB(center.x-1.5,center.y-1.5,center.z-1.5,center.x+1.5,center.y+1.5,center.z+1.5);for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,box,e->GlaivorusAbilityState.isHostileTarget(owner,e)&&!hitTargets.contains(e.getUUID()))){if(target.hurt(owner.damageSources().playerAttack(owner),6)){hitTargets.add(target.getUUID());CosmicExtinctionHammerItem.recordHit(owner);target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40));target.push(forward.x*.45,.25,forward.z*.45);server.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);server.playSound(null,target.blockPosition(),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,SoundSource.PLAYERS,1,1.2F);}}server.sendParticles(ParticleTypes.LARGE_SMOKE,center.x,center.y+.7,center.z,1,0,0,0,0);}
    @Nullable private ServerPlayer owner(ServerLevel level){return ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=tag.getInt("Direction");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putInt("Direction",direction);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
