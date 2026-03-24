package tenko.client.module.modules.visual;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;
import tenko.client.util.RenderUtil;
import java.awt.Color;

public class Tracers extends Module {
    public Tracers() { super("Tracers", "Lines to nearby entities", Category.VISUAL); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.RenderWorldEvent.class, e -> {
            if (mc.world==null || mc.player==null) return;
            for (Entity en : mc.world.getEntities()) {
                if (en==mc.player || !(en instanceof LivingEntity)) continue;
                Color c = en instanceof PlayerEntity
                    ? new Color(255, 80, 160, 200)   // hot pink
                    : new Color(140, 80, 255, 200);  // violet
                RenderUtil.drawTracer(e.matrices, en, c, e.tickDelta);
            }
        });
    }
}
