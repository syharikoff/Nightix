package ru.white.module.impl.display.interfaceimpl;

import net.minecraft.client.gui.screen.ChatScreen;
import ru.white.Client;
import ru.white.module.api.Module;
import ru.white.module.api.settings.impl.DragSetting;
import ru.white.module.impl.display.InterFace;
import ru.white.theme.ThemeColor;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.Easings;
import ru.white.utils.animation.satoshi.Direction;
import ru.white.utils.animation.satoshi.EaseInOutQuad;
import ru.white.utils.colors.ColorUtil;
import ru.white.utils.math.Keyboard;
import ru.white.utils.render.RenderUtil;
import ru.white.utils.render.font.Font;
import ru.white.utils.render.font.Fonts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KeyBinds implements element {

    /** Общий масштаб плашки: один множитель на шрифты, иконки и все отступы. */
    private static  float S = 1.0F;


    private static float H = 16F * S;
    private static float MIN_W = 50F * S;
    private static float RADIUS = 5F * S;

    private static float TITLE_TEXT = 7F * S;
    private static float TITLE_ICON = 5F * S;
    private static float ROW_TEXT = 6.5F * S;

    private static float TITLE_ICON_X = 5F * S;
    private static float TITLE_ICON_Y = 5.5F * S;
    private static float TITLE_TEXT_X = 12.5F * S;
    private static float TITLE_TEXT_Y = 3.4F * S;


    private static float ROW_HEIGHT = 14F * S;
    private static float ROW_BASE_W = 27F * S; // 22 + 5 из оригинального кода
    private static float ROW_PADDING_X = 5F * S;
    private static float ROW_START_Y = 5F * S;
    private static float ROW_ANIM_OFFSET = 15F * S;


    private static float ICON_PADDING = 4F * S;
    private static float ICON_OFFSET_Y = 0.9F * S;
    private static float SEP_PADDING_X = 4F * S;
    private static float SEP_OFFSET_Y = 3.2F * S;


    private Animation openAnimation = new Animation();

    private ru.white.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1);
    private ru.white.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300, 1);

    @Override
    public void onRender(DragSetting dragSetting, InterFace interFace) {
        S = InterFace.getInstance().sizeHud.getValue();
        H = 16F * S;
        MIN_W = 50F * S;
        RADIUS = 5F * S;
        TITLE_TEXT = 7F * S;
        TITLE_ICON = 5F * S;
        ROW_TEXT = 6.5F * S;
        TITLE_ICON_X = 5F * S;
        TITLE_ICON_Y = 5.5F * S;
        TITLE_TEXT_X = 12.5F * S;
        TITLE_TEXT_Y = 3.4F * S;
        ROW_HEIGHT = 14F * S;
        ROW_BASE_W = 27F * S; //
        ROW_PADDING_X = 5F * S;
        ROW_START_Y = 5F * S;
        ROW_ANIM_OFFSET = 15F * S;
        ICON_PADDING = 4F * S;
        ICON_OFFSET_Y = 0.9F * S;
        SEP_PADDING_X = 4F * S;
        SEP_OFFSET_Y = 3.2F * S;

        List<ru.white.module.api.Module> modules = Client.get().moduleManager().values().stream()
                .filter(m -> m.getKey() > 0
                        && !m.getName().equalsIgnoreCase("ClickGui")
                        && (m.isEnabled() || m.getAnimation().getOutput() > 0))
                .sorted(Comparator.comparingInt((ru.white.module.api.Module m) ->
                        m.getName().length() + Keyboard.keyName(m.getKey()).length()
                ).reversed())
                .collect(Collectors.toList());

        boolean isEmpty = modules.isEmpty();

        float x = dragSetting.position.x;
        float y = dragSetting.position.y;

        boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);

        animation1.setDirection(closeCondition ? Direction.BACKWARDS : Direction.FORWARDS);
        animation2.setDirection((mc.currentScreen instanceof ChatScreen) && isEmpty ? Direction.FORWARDS : Direction.BACKWARDS);

        dragSetting.active = !closeCondition;

        float alpha = animation1.getOutput();

        if (closeCondition && alpha == 0.0F) return;

        float alpha2 = animation2.getOutput();

        // Отрисовка состояния пустого списка (чат)
        RenderUtil.Render2D.glow(x, y, MIN_W, H, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha2 * 0.1F), RADIUS, 12, 1);
        RenderUtil.Blur.blur(x, y, MIN_W, H, alpha2, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha2 * InterFace.getInstance().alphaHUD.getValue()));

        Font font = Fonts.sf_regular;
        Font cat = Fonts.wvisual_2;

        cat.draw("E", x + TITLE_ICON_X, y + TITLE_ICON_Y, TITLE_ICON, ColorUtil.replAlpha(ColorUtil.client(), alpha2));
        font.draw("Key binds", x + TITLE_TEXT_X, y + TITLE_TEXT_Y, TITLE_TEXT, ColorUtil.multAlpha(ColorUtil.getColor(240), alpha2));

        float h = 4 * S;
        float w = 0;

        // Высчитываем ширину и высоту плашки
        for (Module m : modules) {
            m.animation.setDirection(m.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            float mAnim = m.getAnimation().getOutput();
            if (mAnim <= 0) continue;

            String name = m.getCategory().getIcon();
            float rowW = ROW_BASE_W + font.getWidth(m.getBigName(), ROW_TEXT)
                    + font.getWidth(Keyboard.keyName(m.getKey()), ROW_TEXT)
                    + cat.getWidth(name, ROW_TEXT);

            w = Math.max(w, rowW * mAnim);
            h += ROW_HEIGHT * mAnim;
        }

        // Отрисовка фона списка
        RenderUtil.Render2D.glow(x, y, w, h - 0.5F, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.1F), RADIUS, 12, 1);
        RenderUtil.Blur.blur(x, y, w, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha * InterFace.getInstance().alphaHUD.getValue()));

        float offsetY = y + ROW_START_Y;
        float offsetY2 = 0;

        // Отрисовка элементов списка
        for (Module m : modules) {

            float mAnim = m.getAnimation().getOutput();
            if (mAnim <= 0) continue;

            float addX = 0;
            String name = m.getCategory().getIcon();

            // Имя модуля
            font.draw(m.getBigName(), x + ROW_PADDING_X - addX, offsetY, ROW_TEXT, ColorUtil.getColor(240, alpha * mAnim));

            String key = Keyboard.keyName(m.getKey());

            float keyWidth = font.getWidth(key, ROW_TEXT);
            float iconWidth = cat.getWidth(name, ROW_TEXT);

            // Кнопка бинда
            font.draw(key, x + addX - ROW_PADDING_X + w - keyWidth - iconWidth - ICON_PADDING, offsetY, ROW_TEXT, ColorUtil.getColor(200, alpha * mAnim));

            // Иконка категории
            cat.draw(name, x + addX - ROW_PADDING_X + w - iconWidth, offsetY + ICON_OFFSET_Y, ROW_TEXT, ColorUtil.replAlpha(ColorUtil.client(), alpha * mAnim));

            // Линия сепаратора
            if (modules.getFirst() != m) {
                RenderUtil.Render2D.rect(x + SEP_PADDING_X + addX, offsetY - SEP_OFFSET_Y, w - (SEP_PADDING_X * 2) - addX, 0.5F,
                        ColorUtil.getColor(255, 0.05F * alpha * mAnim), 1);
            }

            offsetY += ROW_HEIGHT * mAnim;
            offsetY2 += ROW_HEIGHT * mAnim;
        }

        dragSetting.size.set(ColorUtil.overCol((int) Math.max(w, 20 * S), (int) MIN_W, alpha2),
                ColorUtil.overCol((int) offsetY2, (int) H, alpha2));
    }
}