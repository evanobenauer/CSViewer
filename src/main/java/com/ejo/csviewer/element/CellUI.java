package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.WidgetUI;
import com.ejo.glowui.util.QuickDraw;

public class CellUI extends WidgetUI {

    private ColorE fillColor;
    private ColorE outlineColor;

    public CellUI(Vector pos, Vector size) {
        super("",pos, size, true,true, null);
        this.fillColor = ColorE.WHITE;
        this.outlineColor = new ColorE(0,0,0,0);
    }

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {
        QuickDraw.drawRect(getPos(),getSize(),getFillColor());
    }

    @Override
    protected void tickWidget(Scene scene, Vector mousePos) {

    }

    @Override
    public void onMouseClick(Scene scene, int button, int action, int mods, Vector mousePos) {
    }

    @Override
    public void onKeyPress(Scene scene, int key, int scancode, int action, int mods) {
    }

    public void setFillColor(ColorE fillColor) {
        this.fillColor = fillColor;
    }

    public void setOutlineColor(ColorE outlineColor) {
        this.outlineColor = outlineColor;
    }


    public ColorE getFillColor() {
        return fillColor;
    }

    public ColorE getOutlineColor() {
        return outlineColor;
    }

}
