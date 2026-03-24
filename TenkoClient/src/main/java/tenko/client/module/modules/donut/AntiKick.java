package tenko.client.module.modules.donut;

import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class AntiKick extends Module {
    private int tick = 0;
    public AntiKick() { super("AntiKick", "Prevents AFK kick", Category.DONUT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player == null || mc.currentScreen != null) return;
            if (++tick < 600) return;
            tick = 0;
            mc.player.setSneaking(true);
            mc.player.setSneaking(false);
        });
    }
}
