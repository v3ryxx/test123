package tenko.client.module;

import tenko.client.module.modules.combat.*;
import tenko.client.module.modules.movement.*;
import tenko.client.module.modules.visual.*;
import tenko.client.module.modules.donut.*;
import tenko.client.module.modules.misc.*;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Combat
        register(new KillAura());
        register(new Criticals());
        register(new Reach());
        // Movement
        register(new Fly());
        register(new Speed());
        register(new Sprint());
        register(new NoFall());
        // Visual
        register(new ESP());
        register(new Tracers());
        register(new Fullbright());
        register(new Xray());
        // DonutSMP
        register(new ChunkFinder());
        register(new TunnelBaseFinder());
        register(new ClusterFinder());
        register(new LightFinder());
        register(new BaseNotifier());
        register(new ScoreboardHider());
        register(new AutoTotem());
        register(new ChestStealer());
        register(new AntiBot());
        register(new NameProtect());
        register(new AntiKick());
        // Misc
        register(new FreeCam());
        register(new HUD());

        getModule(HUD.class).setEnabled(true);
    }

    private void register(Module m) { modules.add(m); }
    public List<Module> getModules() { return modules; }
    public List<Module> getByCategory(Module.Category c) {
        return modules.stream().filter(m -> m.getCategory()==c).collect(Collectors.toList());
    }
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> cls) {
        return (T) modules.stream().filter(m -> m.getClass()==cls).findFirst().orElse(null);
    }
    public void onKey(int key) {
        modules.forEach(m -> { if (m.getKeybind()==key) m.toggle(); });
    }
}
