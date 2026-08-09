package io.github.finalparadox.client;

/** Pure timing math for the client-only Gatling tracer renderer. */
public final class DroneTracerTimeline {
    public static final double SPEED_PER_TICK = 12.0D;
    public static final double TRAIL_LENGTH = 0.65D;
    public static final double POST_ARRIVAL_FADE_TICKS = 0.25D;
    public static final double MAX_UNRENDERED_TICKS = 20.0D;

    private DroneTracerTimeline() {
    }

    public static double elapsed(double currentTime, double spawnTime) {
        return Math.max(0.0D, currentTime - spawnTime);
    }

    public static double travelTicks(double length) {
        return Math.max(0.0D, length) / SPEED_PER_TICK;
    }

    /**
     * A packet can arrive immediately before a client tick. If no render has
     * happened yet, cap the visual clock inside the fade window so even a very
     * short or stalled-frame tracer gets one visible frame at its endpoint.
     */
    public static double visualElapsed(double elapsed, double length, boolean renderedOnce) {
        double safeElapsed = Math.max(0.0D, elapsed);
        if (renderedOnce) {
            return safeElapsed;
        }
        return Math.min(safeElapsed,
                travelTicks(length) + POST_ARRIVAL_FADE_TICKS * 0.5D);
    }

    public static double frontDistance(double visualElapsed, double length) {
        return Math.min(Math.max(0.0D, length),
                Math.max(0.0D, visualElapsed) * SPEED_PER_TICK);
    }

    public static double tailDistance(double frontDistance) {
        return Math.max(0.0D, frontDistance - TRAIL_LENGTH);
    }

    public static double arrivalFade(double visualElapsed, double length) {
        double afterArrival = Math.max(0.0D, visualElapsed - travelTicks(length));
        return clamp01(1.0D - afterArrival / POST_ARRIVAL_FADE_TICKS);
    }

    public static boolean hasArrived(double elapsed, double length) {
        return elapsed >= travelTicks(length);
    }

    public static boolean isExpired(double elapsed, double length) {
        return elapsed >= travelTicks(length) + POST_ARRIVAL_FADE_TICKS;
    }

    public static boolean isStaleWithoutRender(double elapsed) {
        return elapsed >= MAX_UNRENDERED_TICKS;
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
