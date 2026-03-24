package tenko.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import tenko.client.TenkoClient;
import tenko.client.module.Module;
import tenko.client.util.RenderUtil;
import java.awt.Color;
import java.util.*;

public class ClickGUI extends Screen {

    private static final int WIN_W  = 500;
    private static final int WIN_H  = 300;
    private static final int SIDE_W = 110;
    private static final int HEAD_H = 28;
    private static final int MOD_H  = 16;
    private static final int CONF_W = 160;

    // Tenko color palette: deep purple background, magenta/violet accents
    private static final Color BG_WINDOW  = new Color(14, 10, 22);       // Very dark purple
    private static final Color BG_HEADER  = new Color(22, 14, 38);       // Dark purple header
    private static final Color BG_SIDEBAR = new Color(18, 12, 30);       // Sidebar bg
    private static final Color ACCENT_SEP = new Color(120, 60, 200);     // Purple separator line
    private static final Color ACCENT_SIDE_SEP = new Color(80, 40, 140); // Sidebar divider

    private int winX, winY;
    private boolean dragging;
    private int dragOX, dragOY;

    private Module.Category selectedCat = Module.Category.COMBAT;
    private float scrollY = 0;

    private Module configModule = null;
    private boolean bindingKey   = false;

    private float alpha = 0f;

    public ClickGUI() {
        super(Text.literal("Tenko Client"));
    }

    @Override
    public void init() {
        winX = (width  - WIN_W) / 2;
        winY = (height - WIN_H) / 2;
        alpha = 0f;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        alpha = Math.min(1f, alpha + delta * 0.15f);

        // Dark overlay
        ctx.fill(0, 0, width, height, new Color(0, 0, 0, (int)(130 * alpha)).getRGB());

        int x = winX, y = winY;

        // Window shadow (purple-tinted)
        ctx.fill(x+3, y+3, x+WIN_W+3, y+WIN_H+3, new Color(60, 0, 100, (int)(60 * alpha)).getRGB());

        // Window background
        ctx.fill(x, y, x+WIN_W, y+WIN_H, withAlpha(BG_WINDOW, (int)(245 * alpha)));

        // Header
        ctx.fill(x, y, x+WIN_W, y+HEAD_H, withAlpha(BG_HEADER, (int)(255 * alpha)));

        // Header separator line (magenta glow)
        ctx.fill(x, y+HEAD_H-2, x+WIN_W, y+HEAD_H-1, new Color(180, 60, 255, (int)(255 * alpha)).getRGB());
        ctx.fill(x, y+HEAD_H-1, x+WIN_W, y+HEAD_H,   new Color(120, 40, 200, (int)(200 * alpha)).getRGB());

        // Title: "Tenko Client"
        ctx.drawTextWithShadow(mc.textRenderer,
            Text.literal("§dTenko §fClient §8— §f" + selectedCat.display),
            x + 8, y + 10, 0xFFFFFF);

        drawSidebar(ctx, x, y, mx, my, alpha);
        drawModules(ctx, x + SIDE_W, y + HEAD_H, mx, my, alpha);
        if (configModule != null) drawConfig(ctx, mx, my, alpha);
    }

    private void drawSidebar(DrawContext ctx, int x, int y, int mx, int my, float a) {
        ctx.fill(x, y+HEAD_H, x+SIDE_W, y+WIN_H, withAlpha(BG_SIDEBAR, (int)(255 * a)));
        // Sidebar right border
        ctx.fill(x+SIDE_W-1, y+HEAD_H, x+SIDE_W, y+WIN_H, new Color(100, 40, 160, (int)(200 * a)).getRGB());

        int tabY = y + HEAD_H + 6;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat == selectedCat;
            Color col = catColor(cat);
            Color bg  = sel
                ? new Color(col.getRed()/5, col.getGreen()/5, col.getBlue()/5 + 10, 220)
                : new Color(0, 0, 0, 0);
            ctx.fill(x+2, tabY, x+SIDE_W-2, tabY+18, bg.getRGB());
            if (sel) ctx.fill(x+2, tabY, x+4, tabY+18, col.getRGB());
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(cat.display),
                x+8, tabY+5, sel ? col.getRGB() : 0x998899);
            tabY += 22;
        }
    }

    private void drawModules(DrawContext ctx, int startX, int startY, int mx, int my, float a) {
        List<Module> mods = TenkoClient.moduleManager.getByCategory(selectedCat);
        int y = startY + 4 - (int) scrollY;
        int panelW = WIN_W - SIDE_W;

        for (Module m : mods) {
            if (y > winY+WIN_H || y+MOD_H < startY) { y += MOD_H+2; continue; }
            boolean hov = mx>=startX && mx<=startX+panelW && my>=y && my<=y+MOD_H;
            boolean on  = m.isEnabled();
            Color accent = catColor(selectedCat);

            ctx.fill(startX+2, y, startX+panelW-2, y+MOD_H,
                hov ? new Color(50, 30, 70, 220).getRGB() : new Color(22, 14, 36, 200).getRGB());

            if (on) {
                ctx.fill(startX+2, y, startX+4, y+MOD_H, accent.getRGB());
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal(m.getName()),
                    startX+8, y+4, accent.getRGB());
            } else {
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal(m.getName()),
                    startX+8, y+4, 0x998899);
            }

            if (m.getKeybind() != GLFW.GLFW_KEY_UNKNOWN) {
                String kb = "[" + GLFW.glfwGetKeyName(m.getKeybind(), 0) + "]";
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§8" + kb),
                    startX+panelW-mc.textRenderer.getWidth(kb)-6, y+4, 0x554466);
            }
            y += MOD_H + 2;
        }
    }

    private void drawConfig(DrawContext ctx, int mx, int my, float a) {
        Module m = configModule;
        int cx = winX+WIN_W+4, cy = winY;
        int ch = HEAD_H + 24 + m.getSettings().size() * 26 + 30;
        ch = Math.max(ch, 80);

        ctx.fill(cx, cy, cx+CONF_W, cy+ch, new Color(16, 10, 28, (int)(248 * a)).getRGB());
        ctx.fill(cx, cy, cx+CONF_W, cy+HEAD_H, new Color(22, 14, 40, (int)(255 * a)).getRGB());
        // Config header separator
        ctx.fill(cx, cy+HEAD_H-2, cx+CONF_W, cy+HEAD_H-1, new Color(160, 50, 220, 200).getRGB());
        ctx.fill(cx, cy+HEAD_H-1, cx+CONF_W, cy+HEAD_H,   new Color(100, 30, 160, 180).getRGB());
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§f" + m.getName()), cx+6, cy+10, 0xFFFFFF);

        int ky = cy+HEAD_H+6;
        String kbLabel = bindingKey
            ? "§dPress a key..."
            : "§7Key: §f" + (m.getKeybind()==GLFW.GLFW_KEY_UNKNOWN ? "NONE" : GLFW.glfwGetKeyName(m.getKeybind(),0));
        ctx.fill(cx+4, ky, cx+CONF_W-4, ky+16, new Color(30, 18, 50, 220).getRGB());
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(kbLabel), cx+8, ky+4, 0xFFFFFF);
        ky += 20;

        for (Module.Setting<?> s : m.getSettings().values()) {
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§7" + s.name), cx+6, ky, 0x998899);
            ky += 10;
            if (s.isBoolean()) {
                boolean v = (Boolean) s.get();
                ctx.fill(cx+4, ky, cx+CONF_W-4, ky+14, new Color(24, 14, 40, 220).getRGB());
                ctx.fill(cx+4, ky, cx+4+(v ? CONF_W-8 : 0), ky+14,
                    new Color(v ? catColor(selectedCat).getRGB() : 0x2a1a3a).getRGB());
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal(v ? "§dON" : "§5OFF"), cx+8, ky+3, 0xFFFFFF);
            } else if (s.isNumber()) {
                String val = "§f" + s.displayValue();
                Color ac = catColor(selectedCat);
                ctx.fill(cx+4, ky, cx+CONF_W-4, ky+14, new Color(24, 14, 40, 220).getRGB());
                ctx.fill(cx+4, ky, cx+4+getBarWidth(s, CONF_W-8), ky+14,
                    new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 110).getRGB());
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal(val), cx+8, ky+3, 0xFFFFFF);
            }
            ky += 16;
        }
    }

    private int getBarWidth(Module.Setting<?> s, int maxW) {
        if (s.min==null || s.max==null) return 0;
        double v  = ((Number) s.get()).doubleValue();
        double mn = ((Number) s.min).doubleValue();
        double mx2= ((Number) s.max).doubleValue();
        return (int)((v-mn)/(mx2-mn)*maxW);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (mx>=winX && mx<=winX+WIN_W && my>=winY && my<=winY+HEAD_H && btn==0) {
            dragging=true; dragOX=(int)mx-winX; dragOY=(int)my-winY; return true;
        }
        int tabY = winY+HEAD_H+6;
        for (Module.Category cat : Module.Category.values()) {
            if (mx>=winX+2 && mx<=winX+SIDE_W-2 && my>=tabY && my<=tabY+18) {
                selectedCat=cat; scrollY=0; configModule=null; return true;
            }
            tabY += 22;
        }
        List<Module> mods = TenkoClient.moduleManager.getByCategory(selectedCat);
        int y = winY+HEAD_H+4-(int)scrollY, startX = winX+SIDE_W;
        for (Module m : mods) {
            if (mx>=startX && mx<=winX+WIN_W && my>=y && my<=y+MOD_H) {
                if (btn==0) { m.toggle(); return true; }
                if (btn==1) { configModule=(configModule==m)?null:m; bindingKey=false; return true; }
            }
            y += MOD_H+2;
        }
        if (configModule != null) {
            Module m = configModule;
            int cx = winX+WIN_W+4, cy = winY;
            int ky = cy+HEAD_H+6;
            if (mx>=cx+4 && mx<=cx+CONF_W-4 && my>=ky && my<=ky+16) { bindingKey=true; return true; }
            ky += 20;
            for (Module.Setting<?> s : m.getSettings().values()) {
                ky += 10;
                if (s.isBoolean() && mx>=cx+4 && mx<=cx+CONF_W-4 && my>=ky && my<=ky+14) {
                    @SuppressWarnings("unchecked") Module.Setting<Boolean> bs = (Module.Setting<Boolean>) s;
                    bs.set(!bs.get()); return true;
                }
                ky += 16;
            }
        }
        if (configModule != null) { configModule=null; return true; }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging && btn==0) { winX=(int)mx-dragOX; winY=(int)my-dragOY; return true; }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) { dragging=false; return super.mouseReleased(mx,my,btn); }

    @Override
    public boolean mouseScrolled(double mx, double my, double hx, double vy) {
        scrollY = (float) Math.max(0, scrollY - vy*8); return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (bindingKey && configModule != null) {
            if (key==GLFW.GLFW_KEY_ESCAPE) configModule.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            else configModule.setKeybind(key);
            bindingKey=false; return true;
        }
        if (key==GLFW.GLFW_KEY_ESCAPE) { if (configModule!=null) { configModule=null; return true; } }
        return super.keyPressed(key, scan, mods);
    }

    // Tenko color palette — purple/magenta theme per category
    public static Color catColor(Module.Category c) {
        return switch (c) {
            case COMBAT   -> new Color(255, 80, 160);  // Hot pink
            case MOVEMENT -> new Color(140, 80, 255);  // Violet
            case VISUAL   -> new Color(80, 200, 255);  // Cyan-blue
            case DONUT    -> new Color(220, 120, 255); // Lavender
            case MISC     -> new Color(180, 160, 210); // Soft purple-grey
        };
    }

    private static int withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, Math.max(0, a))).getRGB();
    }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }
    private MinecraftClient mc = MinecraftClient.getInstance();
}
