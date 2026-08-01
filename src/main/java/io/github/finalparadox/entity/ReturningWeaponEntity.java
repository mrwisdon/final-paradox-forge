package io.github.finalparadox.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public abstract class ReturningWeaponEntity extends Entity implements IEntityAdditionalSpawnData {
    protected UUID ownerId;
    protected ItemStack weapon = ItemStack.EMPTY;
    protected Vec3 launchDirection = Vec3.ZERO;
    protected ReturningWeaponEntity(EntityType<? extends ReturningWeaponEntity> type, Level level){super(type,level);noPhysics=true;noCulling=true;}
    public ItemStack displayStack(){return weapon;}
    @Nullable protected ServerPlayer owner(){return ownerId==null||!(level() instanceof ServerLevel server)?null:server.getServer().getPlayerList().getPlayer(ownerId);}
    protected void returnWeapon(){
        if(weapon.isEmpty()){discard();return;}
        ServerPlayer owner=owner();ItemStack returned=weapon;weapon=ItemStack.EMPTY;
        if(owner!=null)owner.getInventory().placeItemBackInInventory(returned);
        else if(level() instanceof ServerLevel server)server.addFreshEntity(new ItemEntity(server,getX(),getY(),getZ(),returned));
        discard();
    }
    @Override protected void defineSynchedData(){}
    @Override protected void readAdditionalSaveData(CompoundTag tag){if(tag.hasUUID("Owner"))ownerId=tag.getUUID("Owner");weapon=ItemStack.of(tag.getCompound("Weapon"));launchDirection=new Vec3(tag.getDouble("DirX"),tag.getDouble("DirY"),tag.getDouble("DirZ"));}
    @Override protected void addAdditionalSaveData(CompoundTag tag){if(ownerId!=null)tag.putUUID("Owner",ownerId);tag.put("Weapon",weapon.save(new CompoundTag()));tag.putDouble("DirX",launchDirection.x);tag.putDouble("DirY",launchDirection.y);tag.putDouble("DirZ",launchDirection.z);}
    @Override public void writeSpawnData(FriendlyByteBuf buf){buf.writeBoolean(ownerId!=null);if(ownerId!=null)buf.writeUUID(ownerId);buf.writeItem(weapon);buf.writeDouble(launchDirection.x);buf.writeDouble(launchDirection.y);buf.writeDouble(launchDirection.z);}
    @Override public void readSpawnData(FriendlyByteBuf buf){if(buf.readBoolean())ownerId=buf.readUUID();weapon=buf.readItem();launchDirection=new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble());}
}
