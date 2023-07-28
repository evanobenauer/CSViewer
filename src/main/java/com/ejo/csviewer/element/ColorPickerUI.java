package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;

public class ColorPickerUI extends ElementUI {

    public ColorPickerUI(Vector pos, boolean shouldRender, boolean shouldTick) {
        super(pos, shouldRender, shouldTick);
    }

    @Override
    protected void drawElement(Scene scene, Vector mousePos) {

    }

    @Override
    protected void tickElement(Scene scene, Vector mousePos) {

    }

    @Override
    public boolean updateMouseOver(Vector mousePos) {
        return false;
    }
}
