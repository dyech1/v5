package com.colorwheel;

import net.minecraft.util.Util;

/** Estado (solo del lado del cliente) del cielo rojo y la grieta que aparecen cuando sale el color rojo. */
public final class SkyTint {
    private static final long FADE_IN_MS = 2500;
    private static final long HOLD_MS = 30_000;   // cuánto dura el cielo rojo
    private static final long FADE_OUT_MS = 5000;
    private static final long OPEN_MS = 3000;     // cuánto tarda en abrirse la grieta

    private static long startMs = -1;
    private static float crackYaw = 0f;

    private SkyTint() {}

    /** yawDeg: hacia dónde miraba el jugador; la grieta se abre en esa dirección del cielo. */
    public static void trigger(float yawDeg) {
        startMs = Util.getMeasuringTimeMs();
        crackYaw = yawDeg;
    }

    public static float crackYaw() {
        return crackYaw;
    }

    /** 0 = cielo normal, 1 = cielo totalmente rojo. */
    public static float factor() {
        if (startMs < 0) return 0f;
        long t = Util.getMeasuringTimeMs() - startMs;
        if (t < FADE_IN_MS) return t / (float) FADE_IN_MS;
        if (t < FADE_IN_MS + HOLD_MS) return 1f;
        if (t < FADE_IN_MS + HOLD_MS + FADE_OUT_MS) {
            return 1f - (t - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS;
        }
        startMs = -1;
        return 0f;
    }

    /** 0..1: cuánto se ha abierto la grieta (con frenado suave). */
    public static float openProgress() {
        if (startMs < 0) return 0f;
        float t = Math.min(1f, (Util.getMeasuringTimeMs() - startMs) / (float) OPEN_MS);
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    public static long elapsedMs() {
        return startMs < 0 ? 0 : Util.getMeasuringTimeMs() - startMs;
    }
}
