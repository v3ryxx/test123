package tenko.client.module.modules.movement;

import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Always sprint", Category.MOVEMENT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player != null && mc.player.forwardSpeed > 0) mc.player.setSprinting(true);
        });
    }
}
