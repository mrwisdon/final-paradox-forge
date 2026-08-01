package io.github.finalparadox.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Particle patterns reconstructed from the precomputed geometry in `bossfight/b5/**`. */
public final class B5Particles {
    private B5Particles() {
    }

    /** 20-point vertical ring (h1 shield, h2 bomb): x/y circle around the anchor. */
    public static void ring(ServerLevel server, Vec3 pos, double radius, double offsetDeg, ParticleOptions particle,
                            double speed) {
        double off = Math.toRadians(offsetDeg);
        for (int i = 0; i < 20; i++) {
            double a = off + i * Math.PI * 2.0D / 20.0D;
            server.sendParticles(particle,
                    pos.x + Math.cos(a) * radius, pos.y + Math.sin(a) * radius, pos.z,
                    0, 0, -speed, 0, 100000);
        }
    }

    /** h1/particles/s1..s5: a camera-facing 24-point ring sent separately to each player. */
    public static void shieldRing(ServerLevel server, Vec3 pos, double radius) {
        for (net.minecraft.server.level.ServerPlayer player : server.players()) {
            Vec3 forward = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
            Vec3 up = Vec3.directionFromRotation(player.getXRot() - 90.0F, player.getYRot());
            Vec3 left = up.cross(forward).normalize();
            Vec3 velocity = up.scale(-1.0D);
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI * 2.0D / 24.0D;
                Vec3 point = pos.add(left.scale(Math.cos(a) * radius)).add(up.scale(Math.sin(a) * radius));
                server.sendParticles(player, ParticleTypes.END_ROD, true,
                        point.x, point.y, point.z, 0, velocity.x, velocity.y, velocity.z, 100000);
            }
        }
    }

    /** h2/particles/s1..s4: a 16-point breathing ring oriented to each player's camera. */
    public static void h2BombRing(ServerLevel server, Vec3 pos, double radius) {
        for (net.minecraft.server.level.ServerPlayer player : server.players()) {
            Vec3 forward = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
            Vec3 up = Vec3.directionFromRotation(player.getXRot() - 90.0F, player.getYRot());
            Vec3 left = up.cross(forward).normalize();
            for (int i = 0; i < 16; i++) {
                double a = -(i + 1) * Math.PI * 2.0D / 16.0D;
                Vec3 point = pos.add(left.scale(Math.cos(a) * radius)).add(up.scale(Math.sin(a) * radius));
                server.sendParticles(player, ParticleTypes.END_ROD, true,
                        point.x, point.y, point.z, 0, 0, -1, 0, 100000);
            }
        }
    }

    /** n-point horizontal fan (crit/totem/item debris bursts). */
    public static void fan(ServerLevel server, Vec3 pos, double radius, int count, ParticleOptions particle,
                           double speed) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0D / count;
            server.sendParticles(particle,
                    pos.x + Math.cos(a) * radius, pos.y, pos.z + Math.sin(a) * radius,
                    1, Math.cos(a) * speed, 0, Math.sin(a) * speed, 0);
        }
    }

    /** Item debris fan (h3 boom: andesite / dark prismarine). */
    public static void itemFan(ServerLevel server, Vec3 pos, ItemStack item, int count, double speed) {
        ParticleOptions particle = new ItemParticleOption(ParticleTypes.ITEM, item);
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0D / count;
            server.sendParticles(particle,
                    pos.x + Math.cos(a) * 1.0D, pos.y + 0.02D, pos.z + Math.sin(a) * 1.0D,
                    1, Math.cos(a) * speed, 0.6D, Math.sin(a) * speed, 0);
        }
    }

    /**
     * h5/particle/1..12: six mirrored pairs rotating 15 degrees per frame.
     * instance_run flips pitch by 180 degrees and places the pattern 0.8 blocks
     * forward from the marker, so its local up/forward axes are inverted.
     */
    public static void h5Wing(ServerLevel server, Vec3 markerPos, Vec3 aim, int frame) {
        if (aim.lengthSqr() < 1.0E-8D) return;
        Vec3 forward = aim.normalize();
        Vec3 left = new Vec3(0.0D, 1.0D, 0.0D).cross(forward).normalize();
        if (left.lengthSqr() < 1.0E-8D) left = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 up = forward.cross(left).normalize();
        Vec3 base = markerPos.add(forward.scale(0.8D)).add(0.0D, -0.3D, 0.0D);
        double rot = Math.toRadians((Math.floorMod(frame - 1, 12)) * 15.0D);
        for (int k = 1; k <= 6; k++) {
            double dz = 0.21D * k;
            double spread = 0.0909090909D * k;
            for (int side = -1; side <= 1; side += 2) {
                double dx = side * spread * Math.cos(rot);
                double dy = side * spread * Math.sin(rot);
                Vec3 point = base.add(left.scale(dx)).subtract(up.scale(dy)).subtract(forward.scale(dz));
                server.sendParticles(ParticleTypes.END_ROD,
                        point.x, point.y, point.z, 0, 0, 1, 0, 100000);
            }
        }
        server.sendParticles(ParticleTypes.END_ROD, base.x, base.y, base.z, 0, 0, 1, 0, 0);
    }

    /** h4/particulas_tp: squid-ink burst in four directions. */
    public static void squidBurst(ServerLevel server, Vec3 pos) {
        server.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y + 1.2D, pos.z,
                15, 0.3D, 0.8D, 0.3D, 0);
        double[] dirs = {0.5D, 0.8660254037844386D, 1.0D, 0.8660254037844386D, 0.5D, 0.0D,
                -0.5D, -0.8660254037844386D, -1.0D, -0.8660254037844386D, -0.5D, 0.0D};
        for (int i = 0; i < 12; i += 2) {
            double vx = dirs[i];
            double vz = dirs[i + 1];
            server.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y + 1.2D, pos.z,
                    1, vx * 0.5D, 0.8660254037844386D * 0.5D, vz * 0.5D, 0.5D);
        }
    }

    /** h1/rebotar + h5/boom: 48-point crit fan with anvil/golem feel. */
    public static void critFan(ServerLevel server, Vec3 pos) {
        fan(server, pos, 1.2D, 48, ParticleTypes.CRIT, 1.2D);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0D, pos.z, 14, 0, 0, 0, 0.3D);
    }

    /** h2/particulas_boom: double explosion + 32-point campfire smoke ring + sounds. */
    public static void h2Boom(ServerLevel server, Vec3 pos) {
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 3.0D, pos.z, 50, 8, 4, 8, 0);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.5D, pos.z, 10, 1, 1, 1, 0);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.END_GATEWAY_SPAWN, SoundSource.PLAYERS, 2.0F, 0.0F);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.0F, 2.0F);
        for (int i = 0; i < 32; i++) {
            double a = i * Math.PI * 2.0D / 32.0D;
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x + Math.cos(a), pos.y + 0.1D, pos.z + Math.sin(a),
                    1, Math.cos(a) * 0.8D, 0, Math.sin(a) * 0.8D, 0);
        }
    }

    /** h2/particulas_golpe: end-rod burst + 24-point ring. */
    public static void h2Golpe(ServerLevel server, Vec3 pos) {
        server.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0D, pos.z, 20, 0, 0, 0, 0.5D);
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2.0D / 24.0D;
            server.sendParticles(ParticleTypes.END_ROD,
                    pos.x + Math.cos(a), pos.y + 1.0D, pos.z + Math.sin(a),
                    1, Math.cos(a) * 0.4D, 0, Math.sin(a) * 0.4D, 0);
        }
    }

    /** h4 reaccion: 48-point totem-of-undying fan. */
    public static void totemFan(ServerLevel server, Vec3 pos) {
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y + 1.0D, pos.z, 15, 0, 0, 0, 1);
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.6D, pos.z, 1, 0, 0, 0, 1);
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI * 2.0D / 48.0D;
            server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    pos.x + Math.cos(a), pos.y + 1.0D, pos.z + Math.sin(a),
                    1, Math.cos(a) * 1.2D, 0, Math.sin(a) * 1.2D, 1.5D);
        }
    }

    /** h3 boom debris: andesite + dark prismarine fans. */
    public static void h3Boom(ServerLevel server, Vec3 pos) {
        server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 0.1D, pos.z, 15, 2, 0, 2, 0);
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y + 0.1D, pos.z, 6, 1.5D, 0, 1.5D, 0);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.DARK_PRISMARINE)),
                pos.x, pos.y + 0.1D, pos.z, 30, 2, 0, 2, 0.1D);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ANDESITE)),
                pos.x, pos.y + 0.1D, pos.z, 30, 2, 0, 2, 0.1D);
        itemFan(server, pos, new ItemStack(Items.ANDESITE), 48, 0.5D);
    }

    public static DustParticleOptions dust(float r, float g, float b, float size) {
        return new DustParticleOptions(new Vector3f(r, g, b), size);
    }
}
