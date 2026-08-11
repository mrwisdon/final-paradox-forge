package io.github.finalparadox.entity;

/** Pure double-Gatling muzzle and spread math, kept free of Minecraft classes. */
public final class DroneGatlingBallistics {
    public static final double MUZZLE_LATERAL_OFFSET = 0.30D;
    public static final double MUZZLE_DROP = 1.10D;
    public static final double MUZZLE_FORWARD = 0.56D;
    public static final double CONVERGENCE_DISTANCE = 32.0D;
    public static final double RANGE = 128.0D;
    public static final double MIN_SPREAD_DEGREES = 0.40D;
    public static final double MAX_SPREAD_DEGREES = 1.20D;

    private DroneGatlingBallistics() {
    }

    public static Vector3 muzzleCenter(Vector3 origin, Vector3 aim) {
        Vector3 forward = aim.normalize();
        return origin.add(new Vector3(0.0D, -MUZZLE_DROP, 0.0D))
                .add(forward.scale(MUZZLE_FORWARD));
    }

    public static Vector3 muzzle(Vector3 center, Vector3 right, boolean rightGun) {
        double sign = rightGun ? 1.0D : -1.0D;
        return center.add(right.normalize().scale(MUZZLE_LATERAL_OFFSET * sign));
    }

    public static Vector3 convergencePoint(Vector3 center, Vector3 aim) {
        return center.add(aim.normalize().scale(CONVERGENCE_DISTANCE));
    }

    public static Vector3 convergingDirection(Vector3 muzzle, Vector3 convergencePoint) {
        return convergencePoint.subtract(muzzle).normalize();
    }

    public static double spreadDegrees(int heat) {
        double fraction = clamp(heat / (double) DroneGatlingCycle.MAX_HEAT, 0.0D, 1.0D);
        return MIN_SPREAD_DEGREES
                + (MAX_SPREAD_DEGREES - MIN_SPREAD_DEGREES) * fraction;
    }

    /**
     * Applies an angular offset sampled uniformly by area from a circular disk.
     * Both samples must be in [0, 1].
     */
    public static Vector3 spreadDirection(
            Vector3 baseDirection, double maxDegrees, double radialSample, double angleSample) {
        Vector3 forward = baseDirection.normalize();
        Vector3 reference = Math.abs(forward.y) < 0.999D
                ? new Vector3(0.0D, 1.0D, 0.0D)
                : new Vector3(1.0D, 0.0D, 0.0D);
        Vector3 right = forward.cross(reference).normalize();
        Vector3 up = right.cross(forward).normalize();
        double radiusDegrees = maxDegrees * Math.sqrt(clamp(radialSample, 0.0D, 1.0D));
        double angle = Math.PI * 2.0D * clamp(angleSample, 0.0D, 1.0D);
        double tangent = Math.tan(Math.toRadians(radiusDegrees));
        return forward
                .add(right.scale(tangent * Math.cos(angle)))
                .add(up.scale(tangent * Math.sin(angle)))
                .normalize();
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record Vector3(double x, double y, double z) {
        public Vector3 add(Vector3 other) {
            return new Vector3(x + other.x, y + other.y, z + other.z);
        }

        public Vector3 subtract(Vector3 other) {
            return new Vector3(x - other.x, y - other.y, z - other.z);
        }

        public Vector3 scale(double scalar) {
            return new Vector3(x * scalar, y * scalar, z * scalar);
        }

        public double dot(Vector3 other) {
            return x * other.x + y * other.y + z * other.z;
        }

        public Vector3 cross(Vector3 other) {
            return new Vector3(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x);
        }

        public double length() {
            return Math.sqrt(dot(this));
        }

        public Vector3 normalize() {
            double length = length();
            if (length < 1.0E-12D) {
                throw new IllegalArgumentException("Cannot normalize a zero-length vector");
            }
            return scale(1.0D / length);
        }

        public double distanceTo(Vector3 other) {
            return subtract(other).length();
        }
    }
}
