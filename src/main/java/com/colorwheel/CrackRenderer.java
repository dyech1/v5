package com.colorwheel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/** Dibuja la grieta que se abre en el cielo (un cuadro fijo en una dirección del cielo). */
public final class CrackRenderer {
    private static final Identifier CRACK = Identifier.of(ColorWheelMod.MOD_ID, "textures/gui/crack.png");

    private static final double DIST = 100.0;        // a qué distancia (en bloques) se dibuja
    private static final double ELEVATION_DEG = 38;  // qué tan alto en el cielo
    private static final float HALF_W = 13f;         // tamaño: ~22° de alto en pantalla (ni grande ni pequeña)
    private static final float HALF_H = 20f;

    private CrackRenderer() {}

    public static void render(WorldRenderContext ctx) {
        float k = SkyTint.factor();
        if (k <= 0f) return;
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;

        float open = SkyTint.openProgress();
        float sx = 0.25f + 0.75f * open;   // se abre: primero angosta, luego ancha
        float sy = 0.35f + 0.65f * open;   // y va creciendo hacia arriba y abajo
        float pulse = 0.9f + 0.1f * (float) Math.sin(SkyTint.elapsedMs() * 0.006);
        float alpha = Math.min(1f, k * 1.2f) * pulse;

        // Dirección fija en el mundo: hacia donde miraba el jugador cuando salió el rojo
        double yaw = Math.toRadians(SkyTint.crackYaw());
        double el = Math.toRadians(ELEVATION_DEG);
        double dx = -Math.sin(yaw) * Math.cos(el), dy = Math.sin(el), dz = Math.cos(yaw) * Math.cos(el);
        double px = dx * DIST, py = dy * DIST, pz = dz * DIST;

        // Vectores "derecha" y "arriba" del cuadro, perpendiculares a la dirección
        double rx = -dz, rz = dx;
        double rl = Math.sqrt(rx * rx + rz * rz);
        rx /= rl; rz /= rl;
        double ux = dy * rz, uy = dz * rx - dx * rz, uz = -dy * rx; // dir x right
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        if (uy < 0) { ux = -ux; uy = -uy; uz = -uz; }

        float w = HALF_W * sx, h = HALF_H * sy;
        Matrix4f m = ms.peek().getPositionMatrix();

        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        corner(b, m, px - rx * w - ux * h, py - uy * h, pz - rz * w - uz * h, 0f, 1f, alpha);
        corner(b, m, px + rx * w - ux * h, py - uy * h, pz + rz * w - uz * h, 1f, 1f, alpha);
        corner(b, m, px + rx * w + ux * h, py + uy * h, pz + rz * w + uz * h, 1f, 0f, alpha);
        corner(b, m, px - rx * w + ux * h, py + uy * h, pz - rz * w + uz * h, 0f, 0f, alpha);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, CRACK);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferRenderer.drawWithGlobalProgram(b.end());
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void corner(BufferBuilder b, Matrix4f m, double x, double y, double z, float u, float v, float a) {
        b.vertex(m, (float) x, (float) y, (float) z).texture(u, v).color(1f, 1f, 1f, a);
    }
}
