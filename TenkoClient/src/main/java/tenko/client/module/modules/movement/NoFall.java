package tenko.client.module.modules.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class NoFall extends Module {
    public NoFall() { super("NoFall", "Prevents fall damage", Category.MOVEMENT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player != null && mc.player.fallDistance > 2f)
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
        });
    }
}
