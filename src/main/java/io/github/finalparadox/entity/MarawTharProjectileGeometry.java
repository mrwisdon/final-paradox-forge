package io.github.finalparadox.entity;

/**
 * Pure spherical-sampling geometry for the Maraw'Thar small/red laser barrage.
 *
 * <p>The source datapack selects players with the selector {@code distance=..1}
 * at each sampled projectile center, i.e. exact Euclidean distance from the
 * player position to the center must be {@code <= 1.0}. The entity-side AABB
 * remains only a broadphase query; this predicate performs the authoritative
 * sphere check and includes the exact radius boundary.
 */
final class MarawTharProjectileGeometry {

    private MarawTharProjectileGeometry() {
    }

    static boolean withinSphereRadius(double sampleX, double sampleY, double sampleZ,
                                      double playerX, double playerY, double playerZ,
                                      double radius) {
        double dx = playerX - sampleX;
        double dy = playerY - sampleY;
        double dz = playerZ - sampleZ;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
