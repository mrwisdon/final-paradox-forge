package io.github.finalparadox.entity;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class PicomerangEntity extends ReturningWeaponEntity {
    private boolean brokeThisLeg;
    public PicomerangEntity(EntityType<PicomerangEntity> type,Level level){super(type,level);}
    public static void spawn(ServerPlayer owner,ItemStack stack){PicomerangEntity entity=new PicomerangEntity(ModEntities.PICOMERANG.get(),owner.serverLevel());entity.ownerId=owner.getUUID();entity.weapon=stack.copy();entity.launchDirection=owner.getLookAngle().normalize();entity.setPos(owner.getX(),owner.getEyeY()-.25D,owner.getZ());entity.setYRot(owner.getYRot());owner.serverLevel().addFreshEntity(entity);owner.level().playSound(null,owner.blockPosition(),SoundEvents.TRIDENT_RIPTIDE_1,SoundSource.PLAYERS,1,1.5F);owner.level().playSound(null,owner.blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.PLAYERS,1,1.5F);}
    @Override public void tick(){super.tick();if(level().isClientSide)return;ServerPlayer owner=owner();if(owner==null){if(tickCount>200)returnWeapon();return;}if(tickCount==26)brokeThisLeg=false;if(tickCount<=20)advanceTo(position().add(launchDirection.scale(1.2D)));else if(tickCount>=25){Vec3 target=owner.position().add(0,1,0);Vec3 delta=target.subtract(position());if(delta.lengthSqr()<=2.25D){returnWeapon();return;}advanceTo(position().add(delta.normalize().scale(1.2D)));}checkBlock(owner);for(LivingEntity target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(1.5D),e->GlaivorusAbilityState.isHostileTarget(owner,e)))target.addEffect(new MobEffectInstance(MobEffects.WITHER,20,1),owner);if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.ENCHANTED_HIT,getX(),getY(),getZ(),3,.2,.2,.2,.1);if(tickCount>=60)returnWeapon();}
    private void advanceTo(Vec3 pos){setPos(pos.x,pos.y,pos.z);setYRot(getYRot()+50.0F);}
    private void checkBlock(ServerPlayer owner){if(brokeThisLeg||owner.gameMode.getGameModeForPlayer()==GameType.ADVENTURE)return;BlockPos pos=blockPosition();var state=level().getBlockState(pos);if(state.isAir()||state.is(Blocks.WATER)||state.is(Blocks.LAVA)||state.is(Blocks.CHEST)||state.is(Blocks.BEDROCK)||state.is(Blocks.BARRIER)||state.is(Blocks.COMMAND_BLOCK)||state.is(Blocks.REPEATING_COMMAND_BLOCK)||state.is(Blocks.CHAIN_COMMAND_BLOCK))return;if(level().destroyBlock(pos,true,owner)){brokeThisLeg=true;if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.EXPLOSION,getX(),getY(),getZ(),1,0,0,0,0);level().playSound(null,pos,SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1,1.4F);}}
    @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){super.readAdditionalSaveData(tag);brokeThisLeg=tag.getBoolean("BrokeLeg");}
    @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("BrokeLeg",brokeThisLeg);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
