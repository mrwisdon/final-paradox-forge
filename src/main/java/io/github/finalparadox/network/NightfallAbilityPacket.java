package io.github.finalparadox.network;

import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record NightfallAbilityPacket(int abilityId) {
    public static void encode(NightfallAbilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.abilityId);
    }

    public static NightfallAbilityPacket decode(FriendlyByteBuf buffer) {
        return new NightfallAbilityPacket(buffer.readUnsignedByte());
    }

    public static void handle(NightfallAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player == null) return;

        NightfallAbilityState.AbilityMode[] modes = NightfallAbilityState.AbilityMode.values();
        if (packet.abilityId < 0 || packet.abilityId >= modes.length) return;

        InteractionHand hand;
        if (player.getMainHandItem().is(ModItems.NIGHTFALL.get())) {
            hand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().is(ModItems.NIGHTFALL.get())) {
            hand = InteractionHand.OFF_HAND;
        } else {
            return;
        }
        NightfallAbilityState.tryStart(player, modes[packet.abilityId], hand);
    }
}
