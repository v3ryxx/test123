package tenko.client.module.modules.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;
import java.util.*;
import java.util.stream.Collectors;

public class KillAura extends Module {
    public final Setting<Double>  range       = addSetting("Range",       4.5, 1.0, 6.0);
    public final Setting<Double>  delay       = addSetting("Delay",       8.0, 1.0, 20.0);
    public final Setting<Boolean> playersOnly = addSetting("Players Only", false);
    private int tick = 0;

    public KillAura() { super("KillAura", "Auto-attacks nearby entities", Category.COMBAT, GLFW.GLFW_KEY_R); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player==null || mc.world==null || mc.currentScreen!=null) return;
            if (++tick < delay.get().intValue()) return;
            tick = 0;
            List<LivingEntity> targets = mc.world.getEntities().stream()
                .filter(en -> en instanceof LivingEntity && en!=mc.player)
                .filter(en -> !playersOnly.get() || en instanceof PlayerEntity)
                .map(en -> (LivingEntity) en).filter(LivingEntity::isAlive)
                .filter(en -> mc.player.distanceTo(en) <= range.get())
                .sorted(Comparator.comparingDouble(en -> mc.player.distanceTo(en)))
                .collect(Collectors.toList());
            if (targets.isEmpty()) return;
            mc.interactionManager.attackEntity(mc.player, targets.get(0));
            mc.player.swingHand(Hand.MAIN_HAND);
        });
    }
}
