package tenko.client.module.modules.donut;

import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.module.Module;

public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem", "Auto-equips totem in offhand", Category.DONUT); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.TickEvent.class, e -> {
            if (mc.player == null || mc.interactionManager == null) return;
            var offhand = mc.player.getOffHandStack();
            if (!offhand.isEmpty() && offhand.getItem() == Items.TOTEM_OF_UNDYING) return;
            var inv = mc.player.getInventory();
            for (int i = 0; i < 36; i++) {
                if (inv.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        i < 9 ? i + 36 : i, 40, SlotActionType.SWAP, mc.player);
                    break;
                }
            }
        });
    }
}
