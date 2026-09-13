package ru.white.mixin;

import ru.white.Client;
import ru.white.manager.event_impl.EventTick;
import ru.white.utils.math.DarkUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class ClientMixin {

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void applyDarkMode(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        DarkUtils.enableImmersiveDarkMode(client.getWindow().getHandle());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void publishClientTick(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.isPaused()) {
            return;
        }

        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }

        Client.eventHandler().post(new EventTick());
    }

    @Inject(method = "getWindowTitle", at = @At("RETURN"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Minecraft 1.21.11");
    }
}
