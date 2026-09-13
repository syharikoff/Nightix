package ru.white.module.impl.display.interfaceimpl;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import ru.white.manager.event_impl.EventDisplay;
import ru.white.module.api.settings.impl.DragSetting;
import ru.white.module.impl.display.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.Easings;
import ru.white.utils.animation.satoshi.Direction;
import ru.white.utils.animation.satoshi.EaseInOutQuad;
import ru.white.utils.annotation.IMinecraft;
import ru.white.utils.colors.ColorFormatting;
import ru.white.utils.colors.ColorUtil;
import ru.white.utils.render.RenderUtil;
import ru.white.utils.render.font.Font;
import ru.white.utils.render.font.Fonts;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.hud.InGameHud.getEffectTexture;

public class Potions implements IMinecraft {

    /** Общий масштаб плашки: один множитель на шрифты, иконки и все отступы. */
    private static  float S = 1.0F;

    private static  float H = 16F * S;
    private static  float MIN_W = 44F * S;
    private static  float RADIUS = 5F * S;


    private static  float TITLE_TEXT = 7F * S;
    private static  float TITLE_ICON = 5F * S;
    private static  float ROW_TEXT = 6.5F * S;
    private static  float ICON = 7F * S;

    private static  float TITLE_ICON_X = 5F * S;
    private static  float TITLE_ICON_Y = 5.5F * S;
    private static  float TITLE_TEXT_X = 12.5F * S;
    private static  float TITLE_TEXT_Y = 3.4F * S;


    private static  float ROW_HEIGHT = 14F * S;
    private static  float ROW_BASE_W = 27F * S; // 22 + 5 из оригинального кода
    private static  float ROW_PADDING_X = 5F * S;
    private static  float ROW_START_Y = 5F * S;
    private static  float ROW_ANIM_OFFSET = 15F * S;

    private static  float ICON_PADDING = 4F * S;
    private static  float SEP_PADDING_X = 4F * S;
    private static  float SEP_OFFSET_Y = 3.2F * S;
    private static  float SCROLL_OFFSET_Y = 4F * S;


    private ru.white.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300,1);
    private ru.white.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300,1);

    private final Map<String, EffectData> displayedEffects = new LinkedHashMap<>();

    public void onRender(DragSetting dragSetting, InterFace interFace, EventDisplay eventDisplay) {
        Collection<StatusEffectInstance> currentEffects = mc.player.getStatusEffects();
        displayedEffects.values().forEach(data -> data.active = false);
        S = InterFace.getInstance().sizeHud.getValue();
        H = 16F * S;
        MIN_W = 44F * S;
        RADIUS = 5F * S;
        TITLE_TEXT = 7F * S;
        TITLE_ICON = 5F * S;
        ROW_TEXT = 6.5F * S;
        ICON = 7F * S;
        TITLE_ICON_X = 5F * S;
        TITLE_ICON_Y = 5.5F * S;
        TITLE_TEXT_X = 12.5F * S;
        TITLE_TEXT_Y = 3.4F * S;
        ROW_HEIGHT = 14F * S;
        ROW_BASE_W = 27F * S; // 22 + 5 из оригинального кода
        ROW_PADDING_X = 5F * S;
        ROW_START_Y = 5F * S;
        ROW_ANIM_OFFSET = 15F * S;
        ICON_PADDING = 4F * S;
        SEP_PADDING_X = 4F * S;
        SEP_OFFSET_Y = 3.2F * S;
        SCROLL_OFFSET_Y = 4F * S;

        for (StatusEffectInstance effect : currentEffects) {
            String name = effect.getEffectType().value().getName().getString();
            int amplifier = effect.getAmplifier() + 1;
            String duration = getDurationString(effect);
            boolean isNegative = effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
            EffectData data = displayedEffects.computeIfAbsent(name, k -> new EffectData(name, duration, amplifier, isNegative, effect.getDuration(), effect));

            // время сменилось — запускаем прокрутку цифр
            if (!duration.equals(data.duration)) {
                data.prevDuration = data.duration;
                data.duration = duration;
                data.digitAnim.set(0);
                data.digitAnim.run(1, 0.18, Easings.QUAD_OUT);
            }

            data.durationTicks = effect.getDuration();
            data.negative = isNegative;
            data.effectInstance = effect;
            data.active = true;
        }

        boolean isEmpty = displayedEffects.isEmpty();

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

        Fonts.wvisual_2.draw("P", x + TITLE_ICON_X, y + TITLE_ICON_Y, TITLE_ICON, ColorUtil.replAlpha(ColorUtil.client(), alpha2));
        font.draw("Potions", x + TITLE_TEXT_X, y + TITLE_TEXT_Y, TITLE_TEXT, ColorUtil.multAlpha(ColorUtil.getColor(240), alpha2));

        List<EffectData> sortedEffects = displayedEffects.values().stream()
                .sorted(Comparator.comparingInt((EffectData data) ->
                        data.name.length() + data.duration.length()
                ).reversed())
                .collect(Collectors.toList());

        float h = 4 * S;
        float w = 0;

        List<String> toRemove = new ArrayList<>();

        for (EffectData data : sortedEffects) {
            data.animation.update();
            data.animation.run(data.active ? 1f : 0f, 0.12f, Easings.QUAD_OUT);
            data.digitAnim.update();

            float a = data.animation.get();

            if (a <= 0.01f && !data.active) {
                toRemove.add(data.name);
                continue;
            }

            float rowW = ROW_BASE_W + font.getWidth(label(data), ROW_TEXT) + font.getWidth(data.duration, ROW_TEXT) + ICON;

            w = Math.max(w, rowW * a);
            h += ROW_HEIGHT * a;
        }

        // Отрисовка фона списка
        RenderUtil.Render2D.glow(x, y, w, h - 0.5F, ColorUtil.replAlpha(ColorUtil.getColor(0), alpha * 0.1F), RADIUS, 12, 1);
        RenderUtil.Blur.blur(x, y, w, h, alpha, RADIUS, ColorUtil.replAlpha(ColorUtil.background(), alpha * InterFace.getInstance().alphaHUD.getValue()));

        float offsetY = y + ROW_START_Y;
        float offsetY2 = 0;

        boolean firstRow = true;

        for (EffectData data : sortedEffects) {

            float a = data.animation.get();
            if (a <= 0.01f && !data.active) continue;

            float addX =0;

            int lvl = data.effectInstance.getAmplifier() + 1;

            // вредные эффекты подсвечиваем красным
            boolean bad = data.negative;

            int nameColor = bad ? ColorUtil.getColor(235, 70, 70, alpha * a) : ColorUtil.getColor(240, alpha * a);
            int lvlColor  = bad ? ColorUtil.getColor(170, 55, 55, alpha * a) : ColorUtil.getColor(150, alpha * a);
            int timeColor = bad ? ColorUtil.getColor(200, 60, 60, alpha * a) : ColorUtil.getColor(200, alpha * a);

            String effectname = data.name + (lvl > 1 ? " " + ColorFormatting.getColor(lvlColor) + lvl : "");

            // Отрисовка названия
            font.draw(effectname, x + ROW_PADDING_X - addX, offsetY, ROW_TEXT, nameColor);

            String key = data.duration;
            float timeWidth = font.getWidth(key, ROW_TEXT);

            // Отрисовка времени (с прокруткой)
            drawDuration(font, data, x + addX - ROW_PADDING_X + w - timeWidth - ICON - ICON_PADDING, offsetY, ROW_TEXT, timeColor);

            // Отрисовка иконки
            drawEffectIcon(eventDisplay, data, x + addX - ROW_PADDING_X + w - ICON, offsetY, alpha * a);

            // Отрисовка разделителя
            if (!firstRow) {
                RenderUtil.Render2D.rect(x + SEP_PADDING_X + addX, offsetY - SEP_OFFSET_Y, w - (SEP_PADDING_X * 2) - addX, 0.5F,
                        ColorUtil.getColor(255, 0.05F * alpha * a), 1);
            }

            firstRow = false;

            offsetY += ROW_HEIGHT * a;
            offsetY2 += ROW_HEIGHT * a;
        }

        toRemove.forEach(displayedEffects::remove);

        dragSetting.size.set(ColorUtil.overCol((int) Math.max(w, 20 * S), (int) MIN_W, alpha2),
                ColorUtil.overCol((int) offsetY2, (int) H, alpha2));
    }

    /**
     * Рисует время посимвольно: изменившийся символ уезжает вверх, новый приезжает снизу.
     * Символы сопоставляются с конца строки, чтобы «1:09» → «1:10» двигало только младшие разряды.
     */
    private void drawDuration(Font font, EffectData data, float x, float y, float size, int color) {
        float t = data.digitAnim.get();

        String now = data.duration;
        String was = data.prevDuration == null ? now : data.prevDuration;

        float cx = x;

        for (int i = 0; i < now.length(); i++) {
            String ch = now.substring(i, i + 1);

            int j = was.length() - (now.length() - i);
            String old = j >= 0 && j < was.length() ? was.substring(j, j + 1) : null;

            if (t >= 1F || ch.equals(old)) {
                font.draw(ch, cx, y, size, color);
            } else {
                font.draw(ch, cx, y + SCROLL_OFFSET_Y * (1 - t), size, ColorUtil.multAlpha(color, t));

                if (old != null) font.draw(old, cx, y - SCROLL_OFFSET_Y * t, size, ColorUtil.multAlpha(color, 1 - t));
            }

            cx += font.getWidth(ch, size);
        }
    }

    /** Строка без цветовых кодов — для замера ширины (коды каждый кадр засоряют кэш ширин). */
    private String label(EffectData data) {
        int lvl = data.effectInstance.getAmplifier() + 1;
        return lvl > 1 ? data.name + " " + lvl : data.name;
    }

    private void drawEffectIcon(EventDisplay eventDisplay, EffectData data, float x, float y, float alpha) {
        Identifier effectTex = getEffectTexture(data.effectInstance.getEffectType());

        var matrices = eventDisplay.getDrawContext().getMatrices();

        matrices.pushMatrix();
        matrices.translate(x, y);

        // ICON / 12F работает корректно, так как исходный размер текстуры эффекта всегда 12х12
        matrices.scale(ICON / 12F, ICON / 12F);

        eventDisplay.getDrawContext().drawGuiTexture(RenderPipelines.GUI_TEXTURED, effectTex,
                0, 0, 12, 12, ColorUtil.getColor(255, alpha));

        matrices.popMatrix();
    }

    private String getDurationString(StatusEffectInstance effect) {
        if (effect.isInfinite()) return "∞";
        int t = effect.getDuration() / 20;
        return String.format("%d:%02d", t / 60, t % 60);
    }

    private static class EffectData {
        String name;
        String duration;
        String prevDuration;
        final Animation digitAnim = new Animation();
        Animation animation;
        boolean active;
        boolean negative;
        int durationTicks;
        int lvl;
        StatusEffectInstance effectInstance;

        public EffectData(String name, String duration, int lvl, boolean negative, int durationTicks, StatusEffectInstance effectInstance) {
            this.name = name;
            this.duration = duration;
            this.prevDuration = duration;
            this.lvl = lvl;
            this.negative = negative;
            this.durationTicks = durationTicks;
            this.animation = new Animation();
            this.active = true;
            this.effectInstance = effectInstance;
        }
    }
}