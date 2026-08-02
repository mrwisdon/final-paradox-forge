package io.github.finalparadox.network;

import io.github.finalparadox.FinalParadox;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "2";

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
    }
}
