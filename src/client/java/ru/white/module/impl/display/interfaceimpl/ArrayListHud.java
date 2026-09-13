package ru.white.module.impl.display.interfaceimpl;

import net.minecraft.client.gui.screen.ChatScreen;
import ru.white.Client;
import ru.white.module.api.Category;
import ru.white.module.api.Module;
import ru.white.module.api.settings.impl.DragSetting;
import ru.white.module.impl.display.Hud;
import ru.white.module.impl.display.InterFace;
import ru.white.theme.ThemeColor;
import ru.white.utils.animation.satoshi.Direction;
import ru.white.utils.colors.ColorUtil;
import ru.white.utils.render.RenderUtil;
import ru.white.utils.render.font.Fonts;

import java.util.List;

public class ArrayListHud implements element {

    // --- Переменная для логики масштабирования ---
    private static float S = 1.0F;

    private static float getAlpha() {
        float opacity = ThemeColor.getOpacity();
        return opacity < 0.98f ? opacity : 0.98f;
    }

    @Override
    public void onRender(DragSetting dragSetting, InterFace interFace) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Обновляем множитель масштаба
        S = InterFace.getInstance().sizeHud.getValue();

        // Динамические переменные с учетом скейла
        final float TEXT_SIZE = 7f * S;
        final float ROW_H = 12f * S;
        final float BAR_W = Math.max(1f, 1f * S); // минимум 1 пиксель, чтобы полоска не исчезала
        final float ROW_RADIUS = 4f * S;

        List<Module> activeModules = Client.get().moduleManager().values().stream()
                .filter(module -> module.getAnimation().getOutput() > 0 && module.getCategory() != Category.VISUALS)
                .sorted((m1, m2) -> {
                    int width1 = (int) Fonts.sf_regular.getWidth(m1.getBigName(), TEXT_SIZE);
                    int width2 = (int) Fonts.sf_regular.getWidth(m2.getBigName(), TEXT_SIZE);
                    return Integer.compare(width2, width1);
                })
                .toList();

        boolean chatOpen = mc.currentScreen instanceof ChatScreen;
        if (activeModules.isEmpty() && !chatOpen) {
            dragSetting.active = false;
            return;
        }

        dragSetting.active = true;

        float maxTextW = 0;
        for (Module module : activeModules) {
            maxTextW = Math.max(maxTextW, Fonts.sf_regular.getWidth(module.getBigName(), TEXT_SIZE));
        }

        float listW = maxTextW + 11f * S;
        float dragX = dragSetting.position.x;
        float dragY = dragSetting.position.y;

        float scaleFix = (float) mc.getWindow().getScaleFactor();
        int screenWidth = (int) (mc.getWindow().getScaledWidth() / scaleFix);
        boolean rightSide = dragX + listW / 2F > screenWidth / 2F;

        float yOffset = dragY;

        for (Module module : activeModules) {
            module.animation15.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);

            String moduleName = module.getBigName();
            float textWidth = Fonts.sf_regular.getWidth(moduleName, TEXT_SIZE);
            float mAnim = module.animation15.getOutput();
            if (mAnim <= 0) {
                continue;
            }

            float xPosition = rightSide
                    ? dragX + listW - 5f * S - (textWidth + 4f * S) - 2f * S
                    : dragX + 7f * S + 2f * S;

            float rowW = textWidth + 8f * S;

            RenderUtil.Blur.blur(xPosition - 2f * S, yOffset - 1f * S, rowW, ROW_H, mAnim,
                    ROW_RADIUS, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue() * mAnim));

            Fonts.sf_regular.draw(moduleName, xPosition + 1.5F * S, yOffset + 0.5F * S, TEXT_SIZE, ThemeColor.getTextColor(mAnim));

            yOffset += (ROW_H + 0.5F * S) * mAnim;
        }

        float listH = Math.max(yOffset - dragY, ROW_H);
        if (!activeModules.isEmpty()) {
            float barX = rightSide ? dragX + listW - 5f * S : dragX + 4f * S;

            RenderUtil.Blur.blur(barX - 2f * S, dragY - 1f * S - 2f * S, BAR_W + 4f * S, listH + 4f * S, 1,
                    2f * S, ColorUtil.replAlpha(ColorUtil.background(), InterFace.getInstance().alphaHUD.getValue()));

            RenderUtil.Render2D.rect(barX, dragY - 1f * S, BAR_W, listH, ThemeColor.getHudColor(), 3f * S);
        }

        dragSetting.size.set(listW, listH);
    }
}