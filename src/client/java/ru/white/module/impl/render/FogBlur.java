package ru.white.module.impl.render;

import net.minecraft.util.math.MathHelper;
import ru.white.manager.event_impl.EventRender3D;
import ru.white.manager.events.orbit.EventHandler;
import ru.white.manager.events.orbit.EventPriority;
import ru.white.module.api.Category;
import ru.white.module.api.Module;
import ru.white.module.api.ModuleInfo;
import ru.white.module.api.settings.impl.BooleanSetting;
import ru.white.module.api.settings.impl.ColorSetting;
import ru.white.module.api.settings.impl.ModeSetting;
import ru.white.module.api.settings.impl.SliderSetting;
import ru.white.utils.colors.ColorUtil;
import ru.white.utils.render.FogBlurPipeline;

@ModuleInfo(name = "Fog Blur", category = Category.VISUALS, desc = "Размывает мир за порогом дистанции тумана")
public final class FogBlur extends Module {

    private static final String COLOR_RAINBOW = "Радуга";
    private static final String COLOR_CLIENT = "Клиент";
    private static final String COLOR_CUSTOM = "Кастом";

    private final SliderSetting fogStrength = new SliderSetting(this, "Сила размытия", 6F, 1F, 20F, 1F);
    private final SliderSetting fogDistance = new SliderSetting(this, "Дистанция тумана", 50F, 0F, 200F, 5F);
    private final SliderSetting saturation = new SliderSetting(this, "Насыщенность", 0.5F, 0.05F, 0.95F, 0.05F);
    private final ModeSetting colorMode = new ModeSetting(this, "Режим цвета", COLOR_RAINBOW, COLOR_RAINBOW, COLOR_CLIENT, COLOR_CUSTOM);
    private final BooleanSetting useSecondColor = new BooleanSetting(this, "Второй цвет", false)
            .setVisible(() -> colorMode.is(COLOR_CUSTOM));
    private final ColorSetting customColor = new ColorSetting(this, "Цвет", 0xFFFFFFFF)
            .setVisible(() -> colorMode.is(COLOR_CUSTOM));
    private final ColorSetting customSecondColor = new ColorSetting(this, "Цвет 2", 0x81575757)
            .setVisible(() -> colorMode.is(COLOR_CUSTOM) && useSecondColor.getValue());
    private final SliderSetting colorOpacity = new SliderSetting(this, "Прозрачность цвета", 30F, 1F, 100F, 1F);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRender(EventRender3D e) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        int color = resolveTintColor();
        color = ColorUtil.applyOpacity(color, colorOpacity.getValue() / 100.0F);

        float far = Math.max(16.0F, mc.options.getViewDistance().getValue() * 16.0F);
        float dist = MathHelper.clamp(fogDistance.getValue() / far, 0.001F, 0.995F);

        FogBlurPipeline.draw(
                dist,
                Math.max(0.0F, Math.min(1.0F, 1.0F - saturation.getValue())),
                computePasses(fogStrength.getValue()),
                colorMode.is(COLOR_CLIENT),
                color,
                color,
                color,
                color
        );
    }

    private int resolveTintColor() {
        if (colorMode.is(COLOR_CLIENT)) {
            return ColorUtil.getClientColor1(0);
        }
        if (colorMode.is(COLOR_CUSTOM)) {
            if (!useSecondColor.getValue()) {
                return customColor.getValue();
            }
            float f = 0.5F + 0.5F * (float) Math.sin(System.currentTimeMillis() / 1200.0);
            return ColorUtil.interpolateColor(customColor.getValue(), customSecondColor.getValue(), f);
        }
        return ColorUtil.rainbow(4, 0, 0.85F, 1.0F, 1.0F);
    }

    private static int computePasses(float strength) {
        return strength >= 14.0F ? 4 : (strength >= 8.0F ? 3 : (strength >= 3.0F ? 2 : 1));
    }
}