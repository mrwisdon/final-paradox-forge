package io.github.ragecraft4reforged.client;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientManaData {
    private static int mana;
    private static int maximum = 20;
    private static boolean synchronizedFromServer;

    public static void update(int value, int maximumValue) {
        maximum = Math.max(1, maximumValue);
        mana = Mth.clamp(value, 0, maximum);
        synchronizedFromServer = true;
    }

    public static void reset() {
        mana = 0;
        maximum = 20;
        synchronizedFromServer = false;
    }

    public static int mana() {
        return mana;
    }

    public static int maximum() {
        return maximum;
    }

    public static boolean isSynchronized() {
        return synchronizedFromServer;
    }

    private ClientManaData() {
    }
}
