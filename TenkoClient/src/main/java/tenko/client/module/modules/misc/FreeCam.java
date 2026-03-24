package tenko.client.module.modules.misc;

import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class FreeCam extends Module {

    public final Setting<Double> speed      = addSetting("Speed",      1.5, 0.1, 5.0);
    public final Setting<Double> sprintMult = addSetting("SprintMult", 2.0, 1.0, 5.0);
    public final Setting<Double> accel      = addSetting("Accel",      0.18, 0.01, 1.0);
    public final Setting<Double> decel      = addSetting("Decel",      0.25, 0.01, 1.0);

    public final Vector3d pos  = new Vector3d();
    public final Vector3d prev = new Vector3d();

    private final Vector3d vel    = new Vector3d();
    private final Vector3d target = new Vector3d();

    public float yaw, pitch, prevYaw, prevPitch;
    private Perspective savedPers;

    public FreeCam() {
        super("FreeCam", "Smooth detached camera", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        savedPers = mc.options.getPerspective();

        var camPos = mc.gameRenderer.getCamera().getPos();
        pos.set(camPos.x, camPos.y, camPos.z);
        prev.set(pos);
        vel.set(0, 0, 0);
        target.set(0, 0, 0);

        yaw = mc.player.getYaw(); pitch = mc.player.getPitch();
        prevYaw = yaw; prevPitch = pitch;

        mc.options.setPerspective(Perspective.FIRST_PERSON);

        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> tick());

        TenkoClient.eventBus.subscribe(Events.MouseMoveEvent.class, e -> {
            if (!isEnabled()) return;
            prevYaw = yaw; prevPitch = pitch;
            yaw   += (float) e.deltaX;
            pitch  = MathHelper.clamp(pitch + (float) e.deltaY, -90f, 90f);
            e.cancelled = true;
        });
    }

    @Override
    public void onDisable() {
        vel.set(0, 0, 0);
        if (savedPers != null && mc.options != null)
            mc.options.setPerspective(savedPers);
    }

    private void tick() {
        if (mc.player == null || mc.currentScreen != null) return;

        prev.set(pos);
        prevYaw   = yaw;
        prevPitch = pitch;

        float rad = (float) Math.toRadians(yaw);
        boolean sprint = mc.options.sprintKey.isPressed();
        double spd = speed.get() * (sprint ? sprintMult.get() : 1.0);

        double tx = 0, ty = 0, tz = 0;
        boolean mh = false, ml = false;

        if (mc.options.forwardKey.isPressed()) { tx -= Math.sin(rad)*spd; tz += Math.cos(rad)*spd; mh=true; }
        if (mc.options.backKey.isPressed())    { tx += Math.sin(rad)*spd; tz -= Math.cos(rad)*spd; mh=true; }
        if (mc.options.leftKey.isPressed())    { tx -= Math.sin(rad - Math.PI/2)*spd; tz += Math.cos(rad - Math.PI/2)*spd; ml=true; }
        if (mc.options.rightKey.isPressed())   { tx -= Math.sin(rad + Math.PI/2)*spd; tz += Math.cos(rad + Math.PI/2)*spd; ml=true; }
        if (mc.options.jumpKey.isPressed())    ty += spd;
        if (mc.options.sneakKey.isPressed())   ty -= spd;

        if (mh && ml) { tx *= 0.7071067811865476; tz *= 0.7071067811865476; }

        target.set(tx, ty, tz);
        boolean stopping = tx == 0 && ty == 0 && tz == 0;
        double lf = stopping ? decel.get() : accel.get();

        vel.x = lerp(lf, vel.x, target.x);
        vel.y = lerp(lf, vel.y, target.y);
        vel.z = lerp(lf, vel.z, target.z);

        if (Math.abs(vel.x) < 5e-4) vel.x = 0;
        if (Math.abs(vel.y) < 5e-4) vel.y = 0;
        if (Math.abs(vel.z) < 5e-4) vel.z = 0;

        pos.x += vel.x;
        pos.y += vel.y;
        pos.z += vel.z;
    }

    public double getLerpX(float pt)     { return lerp(pt, prev.x, pos.x); }
    public double getLerpY(float pt)     { return lerp(pt, prev.y, pos.y); }
    public double getLerpZ(float pt)     { return lerp(pt, prev.z, pos.z); }
    public float  getLerpYaw(float pt)   { return MathHelper.lerpAngleDegrees(pt, prevYaw, yaw); }
    public float  getLerpPitch(float pt) { return MathHelper.lerp(pt, prevPitch, pitch); }

    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
}
