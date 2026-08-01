package io.github.ragecraft4reforged.network;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ragecraft4Reforged.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void init() {
        CHANNEL.messageBuilder(ManaSyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ManaSyncPacket::encode)
                .decoder(ManaSyncPacket::decode)
                .consumerMainThread((packet, context) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> ClientPacketHandler.handleMana(packet));
                    context.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(OpenMechanicsWikiPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenMechanicsWikiPacket::encode)
                .decoder(OpenMechanicsWikiPacket::decode)
                .consumerMainThread((packet, context) -> {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> ClientPacketHandler.openMechanicsWiki());
                    context.get().setPacketHandled(true);
                })
                .add();
    }

    public static void sendMana(ServerPlayer player, int mana, int maximum) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ManaSyncPacket(mana, maximum));
    }

    public static void openMechanicsWiki(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenMechanicsWikiPacket());
    }

    private ModNetwork() {
    }
}
