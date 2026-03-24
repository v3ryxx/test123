package tenko.client.module.modules.visual;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;
import tenko.client.util.RenderUtil;
import java.awt.Color;

public class ESP extends Module {
    public final Setting<Boolean> playersOnly = addSetting("Players Only", false);
    public ESP() { super("ESP", "Show entities through walls", Category.VISUAL); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.RenderWorldEvent.class, e -> {
            if (mc.world==null || mc.player==null) return;
            for (Entity en : mc.world.getEntities()) {
                if (en==mc.player || !(en instanceof LivingEntity le) || !le.isAlive()) continue;
                if (playersOnly.get() && !(en instanceof PlayerEntity)) continue;
                Color c = en instanceof PlayerEntity
                    ? new Color(255, 80, 160, 180)   // hot pink for players
                    : new Color(140, 80, 255, 180);  // violet for mobs
                RenderUtil.drawOutlinedBox(e.matrices, en.getLerpedPos(e.tickDelta), en.getBoundingBox(), c);
            }
        });
    }
}
