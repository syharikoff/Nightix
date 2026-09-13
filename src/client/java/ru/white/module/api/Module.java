package ru.white.module.api;

import ru.white.Client;
import ru.white.module.api.settings.Setting;
import ru.white.module.impl.display.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.satoshi.EaseInOutQuad;
import ru.white.utils.annotation.IMinecraft;


import ru.white.utils.other.SoundUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;


import java.util.List;


@Data
public abstract class Module implements IMinecraft {
    private final List<Setting<?>> settings = new ObjectArrayList<>();
    private ModuleInfo moduleInfo;
    private String name;
    private String bigName;
    private String desc;
    private Category category;
    private boolean enabled;
    private boolean autoEnabled;
    private boolean allowDisable;
    private boolean hidden;
    private int key;

    public final Animation animation_new = new Animation();
    public final Animation animation_new_1 = new Animation();
    public final Animation toggleAnimation = new Animation();

    public final Animation hoverModule = new Animation();
    public final Animation expandedAnimation = new Animation();
    public final Animation animtoP7 = new Animation();
    public final Animation animtoP8 = new Animation();
    public ru.white.utils.animation.satoshi.Animation animation = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation1 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation2 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation3 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation12 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation14 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation15 = new EaseInOutQuad(300, 1);
    public ru.white.utils.animation.satoshi.Animation animation16 = new EaseInOutQuad(300, 1);

    public Module() {
        Class<? extends Module> clazz = this.getClass();
        ModuleInfo moduleInfo = clazz.getAnnotation(ModuleInfo.class);

        if (moduleInfo == null) {
            throw new NotImplementedException("@ModuleInfo annotation not found on " + clazz.getSimpleName());
        }

        this.moduleInfo = moduleInfo;
        this.name = moduleInfo.name().trim().replaceAll(" ", "");
        this.bigName =  moduleInfo.name();
        this.desc = moduleInfo.desc();
        this.category = moduleInfo.category();
        this.autoEnabled = moduleInfo.autoEnabled();
        this.allowDisable = moduleInfo.allowDisable();
        this.hidden = moduleInfo.hidden();
        this.key = moduleInfo.key();
        setup();
    }

    public void toggle() {
        setEnabled(!enabled);
    }


    public void setEnabled(final boolean enabled) {
        setEnabled(enabled, true);
    }

    public void setEnabled(final boolean enabled, boolean notification) {
        if (this.enabled == enabled || (!this.allowDisable && !enabled)) {
            return;
        }
        this.enabled = enabled;

        if (enabled) {
            superEnable();
            if(mc.player != null && notification) {


               // ChatUtils.addChatMessageDev(this.name + " modules was enabled");

                //       Notify.get().showNotification("on", this.name,"function was enabled", 2000,
            //               ColorUtil.getTextTwoColor());

                String type = "tone_enable";
                if (InterFace.getInstance().typeNotify.is("Второй")) {
                    type = "notify_enable";
                } else if (InterFace.getInstance().typeNotify.is("Третий")) {
                    type = "akrien_enable";
                } else if (InterFace.getInstance().typeNotify.is("Четвертый")) {
                    type = "enable3";
                } else if (InterFace.getInstance().typeNotify.is("Пятый")) {
                    type = "enable4";
                }
                SoundUtil.playSound_wav(type, InterFace.getInstance().volume.getValue());
              // if(Notify.get().isEnabled()) {
              //     SoundUtil.playSound_wav("Function_ON", Notify.get().setting.getValue());
              //     Notify.get().showNotification("on",this.name + " modules was enabled");
              // }

            }
        } else {
            superDisable();
            if(mc.player != null && notification) {


               // ChatUtils.addChatMessageDev(this.name + " modules was disabled");

            //      Notify.get().showNotification("off", this.name ,"function was disabled", 2000,
            //              ColorUtil.getTextTwoColor());

                String type = "tone_disable";
                if (InterFace.getInstance().typeNotify.is("Второй")) {
                    type = "notify_disable";
                } else if (InterFace.getInstance().typeNotify.is("Третий")) {
                    type = "akrien_disable";
                } else if (InterFace.getInstance().typeNotify.is("Четвертый")) {
                    type = "disable3";
                } else if (InterFace.getInstance().typeNotify.is("Пятый")) {
                    type = "disable4";
                }
                SoundUtil.playSound_wav(type, InterFace.getInstance().volume.getValue());
                //if(Notify.get().isEnabled()) {
                //    SoundUtil.playSound_wav("Function_OFF", Notify.get().setting.getValue());
                //    Notify.get().showNotification("off",this.name + " modules was disabled");
                //}
            //  SoundUtil.playSound_wav("notify/tone_disable", 0.7F);

            }
        }


    }

    private void eventEnable() {
        Client.eventHandler().subscribe(this);
    }

    private void eventDisable() {
        Client.eventHandler().unsubscribe(this);
    }

    private void superEnable() {
        if (mc.player != null) onEnable();

        eventEnable();
    }

    private boolean expanded;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    private void superDisable() {
        if (mc.player != null) onDisable();

        eventDisable();
    }

    public void setup() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}