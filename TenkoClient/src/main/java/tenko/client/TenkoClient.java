package tenko.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenko.client.event.EventBus;
import tenko.client.gui.ClickGUI;
import tenko.client.module.ModuleManager;

public class TenkoClient implements ClientModInitializer {
    public static final String NAME    = "Tenko Client";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER  = LoggerFactory.getLogger("TenkoClient");
    public static TenkoClient   INSTANCE;
    public static EventBus      eventBus;
    public static ModuleManager moduleManager;
    public static ClickGUI      clickGUI;

    @Override
    public void onInitializeClient() {
        INSTANCE      = this;
        eventBus      = new EventBus();
        moduleManager = new ModuleManager();
        clickGUI      = new ClickGUI();
        LOGGER.info("[Tenko Client] {} modules loaded.", moduleManager.getModules().size());
    }
}
