package ru.white.module.api;


import ru.white.utils.animation.Animation;
import ru.white.utils.animation.satoshi.EaseInOutQuad;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    VISUALS("Visuals","Q"),
    HUD("Hud","H"),
    UTILITIES("Utilities","L");
    private final String name;
    private final String icon;



    public ru.white.utils.animation.satoshi.Animation alphaS = new EaseInOutQuad(300,1);
    public ru.white.utils.animation.satoshi.Animation alphaS2 = new EaseInOutQuad(300,1);


    public Animation animation = new Animation();

}