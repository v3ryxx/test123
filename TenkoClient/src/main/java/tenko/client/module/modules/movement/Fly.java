package tenko.client.module.modules.movement;

import org.lwjgl.glfw.GLFW;
import tenko.client.module.Module;

public class Fly extends Module {
    public final Setting<Double> speed = addSetting("Speed", 0.15, 0.05, 2.0);
    public Fly() { super("Fly", "Fly in survival", Category.MOVEMENT, GLFW.GLFW_KEY_F); }

    @Override public void onEnable()  {
        if (mc.player != null) { mc.player.getAbilities().allowFlying=true; mc.player.getAbilities().flying=true; }
    }
    @Override public void onDisable() {
        if (mc.player != null) { mc.player.getAbilities().allowFlying=mc.player.isCreative(); mc.player.getAbilities().flying=false; }
    }
}
