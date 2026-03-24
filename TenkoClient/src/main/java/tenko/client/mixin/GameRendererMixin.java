package tenko.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tenko.client.TenkoClient;
import tenko.client.event.Events;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private MinecraftClient client;

    private boolean wasRShiftDown = false;

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        TenkoClient.eventBus.post(new Events.RenderWorldEvent(matrices, tickDelta));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (client.currentScreen == null) {
            long win = client.getWindow().getHandle();
            boolean rShift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (rShift && !wasRShiftDown) {
                client.setScreen(TenkoClient.clickGUI);
            }
            wasRShiftDown = rShift;
        }
        if (tick && client.currentScreen == null) {
            long win = client.getWindow().getHandle();
            for (int key = 32; key <= 348; key++) {
                if (GLFW.glfwGetKey(win, key) == GLFW.GLFW_PRESS) {
                    TenkoClient.moduleManager.onKey(key);
                }
            }
        }
    }
}
