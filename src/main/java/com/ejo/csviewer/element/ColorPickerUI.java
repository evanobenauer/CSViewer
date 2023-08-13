package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.setting.Container;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.WidgetUI;

public class ColorPickerUI extends WidgetUI {

    private ColorE color;
    private Container<ColorE> container;

    public ColorPickerUI(String title, Vector pos, Vector size, Container<ColorE> container) {
        super(title, pos, size, true,true, null);
        setAction(() -> getContainer().set(color));
    }

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {

    }

    @Override
    protected void tickWidget(Scene scene, Vector mousePos) {

    }

    @Override
    public void onKeyPress(Scene scene, int key, int scancode, int action, int mods) {

    }

    @Override
    public void onMouseClick(Scene scene, int button, int action, int mods, Vector mousePos) {

    }

    //TODO: Make this for stuff, maybe move over to GlowUI when developed


    public Container<ColorE> getContainer() {
        return container;
    }
}
