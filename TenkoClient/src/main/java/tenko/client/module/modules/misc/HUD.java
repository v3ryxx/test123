package tenko.client.module.modules.misc;

import net.minecraft.text.Text;
import tenko.client.TenkoClient;
import tenko.client.event.Events;
import tenko.client.gui.ClickGUI;
import tenko.client.module.Module;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {
    public HUD() { super("HUD", "Client HUD overlay", Category.MISC); }

    @Override public void onEnable() {
        TenkoClient.eventBus.subscribe(Events.RenderHudEvent.class, e -> {
            if (mc.player == null) return;
            var ctx = e.context;
            var tr  = mc.textRenderer;

            // Watermark — Tenko branding
            ctx.drawTextWithShadow(tr, Text.literal("§dTenko §fClient §8v1.0"), 4, 4, 0xFFFFFF);
            ctx.drawTextWithShadow(tr, Text.literal("§7FPS: §f" + mc.getCurrentFps()), 4, 14, 0xFFFFFF);

            // Module list right side
            List<Module> active = TenkoClient.moduleManager.getModules().stream()
                .filter(m -> m.isEnabled() && !(m instanceof HUD))
                .sorted(Comparator.comparingInt(m -> -tr.getWidth(m.getName())))
                .collect(Collectors.toList());

            int sw = mc.getWindow().getScaledWidth(), y = 4;
            for (Module m : active) {
                int w = tr.getWidth(m.getName()), x = sw - w - 5;
                Color col = ClickGUI.catColor(m.getCategory());
                ctx.fill(x-2, y-1, sw-2, y+9, new Color(0, 0, 0, 100).getRGB());
                ctx.fill(x-2, y-1, x-1, y+9, col.getRGB());
                ctx.drawTextWithShadow(tr, Text.literal(m.getName()), x, y, 0xFFFFFF);
                y += 11;
            }
        });
    }
}
