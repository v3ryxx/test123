package tenko.client.event;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Events {
    public static class TickEvent {}
    public static class AttackEvent { public boolean cancelled = false; }

    public static class RenderWorldEvent {
        public final MatrixStack matrices;
        public final float tickDelta;
        public RenderWorldEvent(MatrixStack m, float t) { matrices = m; tickDelta = t; }
    }

    public static class RenderHudEvent {
        public final DrawContext context;
        public final float tickDelta;
        public RenderHudEvent(DrawContext c, float t) { context = c; tickDelta = t; }
    }

    public static class Render2DEvent {
        public final MatrixStack matrices;
        public final float tickDelta;
        public Render2DEvent(MatrixStack m, float t) { matrices = m; tickDelta = t; }
    }

    public static class MouseMoveEvent {
        public double deltaX, deltaY;
        public boolean cancelled = false;
        public MouseMoveEvent(double dx, double dy) { deltaX = dx; deltaY = dy; }
    }

    public static class KeyPressEvent {
        public final int key;
        public KeyPressEvent(int k) { key = k; }
    }
}
