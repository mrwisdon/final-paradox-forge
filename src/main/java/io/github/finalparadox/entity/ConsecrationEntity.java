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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public final class ConsecrationEntity extends Entity {
    public ConsecrationEntity(EntityType<ConsecrationEntity> type, Level level) { super(type, level); noPhysics=true; noCulling=true; }

    public static void spawn(ServerPlayer player) {
        ServerLevel level=player.serverLevel();
        for(ConsecrationEntity old: level.getEntitiesOfClass(ConsecrationEntity.class, player.getBoundingBox().inflate(128))) old.discard();
        ConsecrationEntity area=new ConsecrationEntity(ModEntities.CONSECRATION.get(),level);
        BlockPos.MutableBlockPos pos=player.blockPosition().mutable();
        while(pos.getY()>level.getMinBuildHeight() && level.getBlockState(pos.below()).getCollisionShape(level,pos.below()).isEmpty()) pos.move(0,-1,0);
        area.setPos(pos.getX()+0.5D,pos.getY(),pos.getZ()+0.5D); level.addFreshEntity(area);
        level.sendParticles(ParticleTypes.EXPLOSION,area.getX(),area.getY()+0.1D,area.getZ(),1,0,0,0,0);
        level.sendParticles(ParticleTypes.FLAME,area.getX(),area.getY()+0.1D,area.getZ(),30,0,0,0,0.4D);
        level.sendParticles(ParticleTypes.LAVA,area.getX(),area.getY()+0.1D,area.getZ(),4,3,0,3,0);
        level.playSound(null,area.blockPosition(),SoundEvents.ILLUSIONER_PREPARE_MIRROR,SoundSource.PLAYERS,1,1);
    }

    @Override protected void defineSynchedData() { }
    @Override public void tick(){
        super.tick(); if(level().isClientSide)return; if(tickCount>=200){discard();return;}
        if(!(level() instanceof ServerLevel server))return;
        AABB box=getBoundingBox().inflate(7.0D);
        for(Player player:server.getEntitiesOfClass(Player.class,box,p->p.isAlive())){
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,0,true,false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,25,0,true,false));
            if(!player.hasEffect(MobEffects.REGENERATION)) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,0,true,false));
        }
        for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive() && e.getType().getCategory()==net.minecraft.world.entity.MobCategory.MONSTER)){
            if(!target.hasEffect(MobEffects.WITHER)) target.addEffect(new MobEffectInstance(MobEffects.WITHER,60,0));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,25,0));
        }
        if(tickCount%10==0){
            for(int i=0;i<64;i++){double a=i*Math.PI/32.0D;server.sendParticles(ParticleTypes.FLAME,getX()+Math.cos(a)*7,getY()+0.1,getZ()+Math.sin(a)*7,1,0,0,0,0.005);}
            server.sendParticles(ParticleTypes.ENTITY_EFFECT,getX(),getY(),getZ(),12,3.5,0,3.5,0);
            server.sendParticles(ParticleTypes.LAVA,getX(),getY(),getZ(),1,3.5,0,3.5,0);
        }
    }
    public boolean contains(LivingEntity entity){return distanceToSqr(entity)<=49.0D;}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
