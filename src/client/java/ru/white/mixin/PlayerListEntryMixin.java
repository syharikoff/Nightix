package ru.white.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.Client;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerListEntryMixin {

    @Unique
    private static final Identifier CAPE_ID = Identifier.of("client", "cape");
    @Unique
    private static final AssetInfo.TextureAssetInfo CAPE_ASSET = new AssetInfo.TextureAssetInfo(CAPE_ID);

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void replaceCape(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        boolean isLocal  = player.getUuid().equals(client.player.getUuid());
        boolean isFriend = Client.get().friendManager().isFriend(player.getName().getString());

        if (!isLocal && !isFriend) return;

        SkinTextures old = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(
                old.body(),
                CAPE_ASSET,
                CAPE_ASSET,
                old.model(),
                old.secure()
        ));
    }
}
