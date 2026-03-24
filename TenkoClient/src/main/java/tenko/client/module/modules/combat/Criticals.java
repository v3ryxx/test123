package tenko.client.module.modules.combat;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class Criticals extends Module {
    public Criticals() { super("Criticals", "Always land critical hits", Category.COMBAT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.AttackEvent.class, e -> {
            if (mc.player==null || !mc.player.isOnGround() || mc.player.isTouchingWater()) return;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY()+0.0625, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        });
    }
}
