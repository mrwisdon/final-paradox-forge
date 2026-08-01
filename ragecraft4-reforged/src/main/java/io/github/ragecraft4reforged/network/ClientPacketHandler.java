package io.github.ragecraft4reforged.network;

import io.github.ragecraft4reforged.client.ClientManaData;
import io.github.ragecraft4reforged.client.MechanicsWikiScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandler {
    public static void handleMana(ManaSyncPacket packet) {
        ClientManaData.update(packet.mana(), packet.maximum());
    }

    public static void openMechanicsWiki() {
        Minecraft.getInstance().setScreen(new MechanicsWikiScreen());
    }

    private ClientPacketHandler() {
    }
}
