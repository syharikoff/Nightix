package ru.white.mixin;

import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import ru.white.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    @Unique private List<String> wvisual$suggestions = Collections.emptyList();
    @Unique private int wvisual$selected = 0;

    @Unique private int wvisual$boxX, wvisual$boxY, wvisual$boxW, wvisual$boxCount;
    @Unique private static final int WVISUAL_LINE_H = 12;

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void interceptMessage(String message, boolean addToHistory, CallbackInfo ci) {
        if (message.isEmpty()) return;

        char prefix = Client.get().commandManager().getPrefix();
        if (message.charAt(0) == prefix) {
            ci.cancel();
            Client.get().commandManager().handleMessage(message);
            return;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void wvisual$renderSuggestions(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        wvisual$boxCount = 0;

        String text = chatField.getText();
        char prefix = Client.get().commandManager().getPrefix();

        if (text.isEmpty() || text.charAt(0) != prefix) {
            wvisual$suggestions = Collections.emptyList();
            return;
        }

        wvisual$suggestions = Client.get().commandManager().getSuggestions(text);
        if (wvisual$suggestions.isEmpty()) return;

        if (wvisual$selected >= wvisual$suggestions.size()) wvisual$selected = 0;

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int count = Math.min(wvisual$suggestions.size(), 10);

        int width = 0;
        for (int i = 0; i < count; i++) {
            width = Math.max(width, tr.getWidth(wvisual$suggestions.get(i)));
        }
        width += 6;

        int boxH = count * WVISUAL_LINE_H;
        int x = chatField.getX() - 2;
        int y = chatField.getY() - boxH - 1;

        wvisual$boxX = x;
        wvisual$boxY = y;
        wvisual$boxW = width;
        wvisual$boxCount = count;

        context.fill(x, y, x + width, y + boxH, 0xE6000000);
        context.fill(x, y, x + width, y + 1, 0x40FFFFFF);

        for (int i = 0; i < count; i++) {
            int ly = y + i * WVISUAL_LINE_H;
            boolean sel = i == wvisual$selected;
            if (sel) context.fill(x, ly, x + width, ly + WVISUAL_LINE_H, 0x55FFFFFF);
            context.drawText(tr, wvisual$suggestions.get(i), x + 3, ly + 2,
                    sel ? 0xFFFFFF55 : 0xFFBBBBBB, false);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void wvisual$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (wvisual$suggestions.isEmpty()) return;
        int n = wvisual$suggestions.size();

        switch (input.getKeycode()) {
            case GLFW.GLFW_KEY_TAB -> {
                wvisual$apply(wvisual$suggestions.get(Math.min(wvisual$selected, n - 1)));
                cir.setReturnValue(true);
            }
            case GLFW.GLFW_KEY_UP -> {
                wvisual$selected = (wvisual$selected - 1 + n) % n;
                cir.setReturnValue(true);
            }
            case GLFW.GLFW_KEY_DOWN -> {
                wvisual$selected = (wvisual$selected + 1) % n;
                cir.setReturnValue(true);
            }
            default -> {}
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void wvisual$mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {

        int button = click.button();

        double mouseX = click.x();
        double mouseY = click.y();

        if (wvisual$suggestions.isEmpty() || wvisual$boxCount == 0 || button != 0) return;

        if (mouseX >= wvisual$boxX && mouseX <= wvisual$boxX + wvisual$boxW
                && mouseY >= wvisual$boxY && mouseY <= wvisual$boxY + wvisual$boxCount * WVISUAL_LINE_H) {
            int idx = (int) ((mouseY - wvisual$boxY) / WVISUAL_LINE_H);
            if (idx >= 0 && idx < wvisual$boxCount && idx < wvisual$suggestions.size()) {
                wvisual$apply(wvisual$suggestions.get(idx));
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private void wvisual$apply(String suggestion) {
        chatField.setText(suggestion);
        chatField.setCursorToEnd(false);
        wvisual$selected = 0;
    }
}
