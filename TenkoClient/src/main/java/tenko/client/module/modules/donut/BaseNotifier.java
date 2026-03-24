package tenko.client.module.modules.donut;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class BaseNotifier extends Module {
    public final Setting<Double>  alertRange = addSetting("Alert Range", 200.0, 50.0, 2000.0);
    public final Setting<Boolean> chatAlert  = addSetting("Chat Alert",  true);
    private long lastAlert = 0;

    public BaseNotifier() { super("BaseNotifier", "Alerts when a base is detected nearby", Category.DONUT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player == null) return;
            long now = System.currentTimeMillis();
            if (now - lastAlert < 5000) return;
            ChunkFinder cf = TenkoClient.moduleManager.getModule(ChunkFinder.class);
            if (cf == null || !cf.isEnabled()) return;
            Vec3d pp = mc.player.getPos();
            for (ChunkFinder.SusChunk c : cf.getResults()) {
                if (pp.distanceTo(c.center) <= alertRange.get()) {
                    lastAlert = now;
                    if (chatAlert.get())
                        mc.player.sendMessage(Text.literal(
                            "§d[Tenko] §fBase detected §7" + (int)pp.distanceTo(c.center) + "m §d(score:" + c.score + ")"), false);
                    break;
                }
            }
        });
    }
}
