package tenko.client.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String   name;
    private final String   description;
    private final Category category;
    private int     keybind;
    private boolean enabled;
    private final Map<String, Setting<?>> settings = new LinkedHashMap<>();

    public Module(String name, String description, Category category, int keybind) {
        this.name = name; this.description = description;
        this.category = category; this.keybind = keybind;
    }
    public Module(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
    }

    public void onEnable()  {}
    public void onDisable() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void setEnabled(boolean v) { if (enabled != v) toggle(); }
    public boolean  isEnabled()      { return enabled; }
    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public Category getCategory()    { return category; }
    public int      getKeybind()     { return keybind; }
    public void     setKeybind(int k){ keybind = k; }
    public Map<String, Setting<?>> getSettings() { return settings; }

    protected <T> Setting<T> addSetting(String name, T def) {
        Setting<T> s = new Setting<>(name, def, null, null);
        settings.put(name, s); return s;
    }
    protected <T> Setting<T> addSetting(String name, T def, T min, T max) {
        Setting<T> s = new Setting<>(name, def, min, max);
        settings.put(name, s); return s;
    }

    public static class Setting<T> {
        public final String name;
        private T value;
        public final T min, max;
        public Setting(String n, T v, T min, T max) { name=n; value=v; this.min=min; this.max=max; }
        public T    get()   { return value; }
        public void set(T v){ value = v; }
        public boolean isBoolean() { return value instanceof Boolean; }
        public boolean isNumber()  { return value instanceof Number; }
        public String  displayValue() {
            if (value instanceof Double d) return String.format("%.1f", d);
            return value.toString();
        }
    }

    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), DONUT("DonutSMP"), MISC("Misc");
        public final String display;
        Category(String d) { display = d; }
    }
}
