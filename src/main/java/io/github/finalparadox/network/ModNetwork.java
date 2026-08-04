package io.github.finalparadox.network;

import io.github.finalparadox.FinalParadox;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "6";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(NightfallAbilityPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(NightfallAbilityPacket::encode)
                .decoder(NightfallAbilityPacket::decode)
                .consumerMainThread(NightfallAbilityPacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerDismountPacket.class, 1,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerDismountPacket::encode)
                .decoder(TerrastalkerDismountPacket::decode)
                .consumerMainThread(TerrastalkerDismountPacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerFireInputPacket.class, 2,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerFireInputPacket::encode)
                .decoder(TerrastalkerFireInputPacket::decode)
                .consumerMainThread(TerrastalkerFireInputPacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerMissilePacket.class, 3,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerMissilePacket::encode)
                .decoder(TerrastalkerMissilePacket::decode)
                .consumerMainThread(TerrastalkerMissilePacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerJumpPacket.class, 4,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerJumpPacket::encode)
                .decoder(TerrastalkerJumpPacket::decode)
                .consumerMainThread(TerrastalkerJumpPacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerGrenadePacket.class, 5,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerGrenadePacket::encode)
                .decoder(TerrastalkerGrenadePacket::decode)
                .consumerMainThread(TerrastalkerGrenadePacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerExitPacket.class, 6,
                        NetworkDirection.PLAY_TO_SERVER)
                .encoder(TerrastalkerExitPacket::encode)
                .decoder(TerrastalkerExitPacket::decode)
                .consumerMainThread(TerrastalkerExitPacket::handle)
                .add();
        CHANNEL.messageBuilder(TerrastalkerDismountAckPacket.class, 7,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TerrastalkerDismountAckPacket::encode)
                .decoder(TerrastalkerDismountAckPacket::decode)
                .consumerMainThread(TerrastalkerDismountAckPacket::handle)
                .add();
    }

    public static void sendDismountAck(
            net.minecraft.server.level.ServerPlayer player,
            int roverEntityId
    ) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new TerrastalkerDismountAckPacket(
                        roverEntityId,
                        player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot()));
    }
}
