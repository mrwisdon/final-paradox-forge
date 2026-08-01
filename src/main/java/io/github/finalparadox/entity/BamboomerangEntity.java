package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BamboomerangEntity extends ReturningWeaponEntity {
    private final Set<UUID> hitTargets=new HashSet<>();
    public BamboomerangEntity(EntityType<BamboomerangEntity> type,Level level){super(type,level);}
    public static void spawn(ServerPlayer owner,ItemStack stack){BamboomerangEntity entity=new BamboomerangEntity(ModEntities.BAMBOOMERANG.get(),owner.serverLevel());entity.ownerId=owner.getUUID();entity.weapon=stack.copy();entity.launchDirection=owner.getLookAngle().normalize();entity.setPos(owner.getX(),owner.getEyeY()-.3,owner.getZ());entity.setYRot(owner.getYRot());owner.serverLevel().addFreshEntity(entity);owner.level().playSound(null,owner.blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,1,1);}
    @Override public void tick(){super.tick();if(level().isClientSide)return;ServerPlayer owner=owner();if(owner==null){if(tickCount>60)returnWeapon();return;}if(tickCount==25)hitTargets.clear();Vec3 next=position();if(tickCount<=15)next=position().add(launchDirection.scale(.8D));else if(tickCount>=21){Vec3 delta=owner.position().add(0,1,0).subtract(position());if(delta.lengthSqr()<=1){returnWeapon();return;}next=position().add(delta.normalize().scale(.8D));}if(tickCount>=2&&tickCount<=15&&level().clip(new ClipContext(position(),next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS){tickCount=24;level().playSound(null,blockPosition(),SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1,1.4F);if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.EXPLOSION,getX(),getY(),getZ(),1,0,0,0,0);}else setPos(next.x,next.y,next.z);setYRot(getYRot()+40);for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->GlaivorusAbilityState.isHostileTarget(owner,e)&&hitTargets.add(e.getUUID()))){target.hurt(owner.damageSources().playerAttack(owner),4);Vec3 push=target.position().subtract(position()).normalize();target.push(push.x*.6,.15,push.z*.6);if(level() instanceof ServerLevel server){server.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);level().playSound(null,target.blockPosition(),SoundEvents.ARMOR_STAND_HIT,SoundSource.PLAYERS,1,2);}}if(level() instanceof ServerLevel server)server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.BAMBOO)),getX(),getY(),getZ(),1,.36,0,.36,0);if(tickCount>=50)returnWeapon();}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
