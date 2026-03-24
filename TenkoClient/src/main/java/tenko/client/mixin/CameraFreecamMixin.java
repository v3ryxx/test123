package tenko.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tenko.client.TenkoClient;
import tenko.client.module.modules.misc.FreeCam;
import net.minecraft.util.math.Vec3d;

@Mixin(Camera.class)
public abstract class CameraFreecamMixin {

    @Shadow protected abstract void setPos(Vec3d pos);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson,
                          boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreeCam fc = TenkoClient.moduleManager.getModule(FreeCam.class);
        if (fc == null || !fc.isEnabled()) return;
        setPos(new Vec3d(fc.getLerpX(tickDelta), fc.getLerpY(tickDelta), fc.getLerpZ(tickDelta)));
        setRotation(fc.getLerpYaw(tickDelta), fc.getLerpPitch(tickDelta));
    }
}
