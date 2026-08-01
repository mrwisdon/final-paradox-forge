package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.item.PaladinHammerItem;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PaladinHammerSwingEntity extends Entity {
    @Nullable private UUID ownerId;
    private int direction=1;
    private final Map<UUID,Integer> lastHits=new HashMap<>();
    public PaladinHammerSwingEntity(EntityType<PaladinHammerSwingEntity> type,Level level){super(type,level);noPhysics=true;noCulling=true;}

    public static void spawn(ServerPlayer owner){
        if(hasActive(owner))return;
        PaladinHammerSwingEntity swing=new PaladinHammerSwingEntity(ModEntities.PALADIN_HAMMER_SWING.get(),owner.level());
        swing.ownerId=owner.getUUID();
        int alternator=owner.getPersistentData().getInt("finalparadox.hammer_side");
        swing.direction=alternator==0?1:-1; owner.getPersistentData().putInt("finalparadox.hammer_side",alternator==0?1:0);
        swing.setPos(owner.getX(),owner.getY()+0.1,owner.getZ()); owner.level().addFreshEntity(swing);
        owner.level().playSound(null,owner.blockPosition(),SoundEvents.UI_TOAST_OUT,SoundSource.PLAYERS,0.5F,1.1F);
        owner.level().playSound(null,owner.blockPosition(),SoundEvents.ARMOR_EQUIP_GOLD,SoundSource.PLAYERS,1,0.9F);
    }
    public static boolean hasActive(ServerPlayer owner){return !owner.level().getEntitiesOfClass(PaladinHammerSwingEntity.class,owner.getBoundingBox().inflate(16),swing->swing.isOwnedBy(owner)).isEmpty();}
    public boolean isOwnedBy(ServerPlayer player){return player.getUUID().equals(ownerId);}
    @Override protected void defineSynchedData(){}
    @Override public void tick(){
        super.tick();if(level().isClientSide)return;if(!(level() instanceof ServerLevel server)){discard();return;}
        ServerPlayer owner=owner(server);if(owner==null||!owner.isUsingItem()||!owner.getUseItem().is(ModItems.PALADIN_HAMMER.get())||owner.isCrouching()){discard();return;}
        int cycle=(tickCount-1)%16;
        if(cycle==0&&tickCount>1){direction=-direction;lastHits.clear();owner.level().playSound(null,owner.blockPosition(),SoundEvents.UI_TOAST_OUT,SoundSource.PLAYERS,0.5F,1.1F);owner.level().playSound(null,owner.blockPosition(),SoundEvents.ARMOR_EQUIP_GOLD,SoundSource.PLAYERS,1,0.9F);}
        setPos(owner.getX(),owner.getY()+0.1,owner.getZ());setYRot(owner.getYRot()+direction*(190.0F-cycle*20.0F));
        if(cycle>=4){
            Vec3 forward=Vec3.directionFromRotation(0,getYRot()).scale(2.5D);Vec3 center=position().add(forward);
            AABB box=new AABB(center.x-1.75,center.y-1.75,center.z-1.75,center.x+1.75,center.y+1.75,center.z+1.75);
            for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,box,e->GlaivorusAbilityState.isValidTarget(owner,e))){
                int last=lastHits.getOrDefault(target.getUUID(),-20);if(tickCount-last<10)continue;
                boolean consecrated=!server.getEntitiesOfClass(ConsecrationEntity.class,target.getBoundingBox().inflate(7),c->c.contains(target)).isEmpty();
                if(target.hurt(owner.damageSources().playerAttack(owner),consecrated?7.0F:5.0F)){
                    lastHits.put(target.getUUID(),tickCount);PaladinHammerItem.recordHit(owner);
                    target.push(forward.x*0.35D,0.25D,forward.z*0.35D);
                    server.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);
                    if(consecrated){server.sendParticles(ParticleTypes.FLAME,target.getX(),target.getY()+1,target.getZ(),16,1,0,1,0.1);server.sendParticles(ParticleTypes.LAVA,target.getX(),target.getY()+1,target.getZ(),3,0,0,0,0);}
                    server.playSound(null,target.blockPosition(),SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,SoundSource.PLAYERS,1,1.2F);
                }
            }
            server.sendParticles(ParticleTypes.POOF,center.x,center.y+0.7,center.z,1,0,0,0,0);
        }
    }
    @Nullable private ServerPlayer owner(ServerLevel level){return ownerId==null?null:level.getServer().getPlayerList().getPlayer(ownerId);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");direction=tag.getInt("Direction");}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.putInt("Direction",direction);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
