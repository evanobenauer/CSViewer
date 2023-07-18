package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.Container;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.TextFieldUI;
import com.ejo.glowui.scene.elements.widget.WidgetUI;
import com.ejo.glowui.util.QuickDraw;

public class CellUI extends TextFieldUI {

    private ColorE fillColor;
    private ColorE outlineColor;

    public CellUI(Vector pos, Vector size) {
        super(pos, size, ColorE.WHITE,new Container<>(""),"",false);
        this.fillColor = ColorE.WHITE;
        this.outlineColor = new ColorE(0,0,0,0);
    }

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {
        super.drawWidget(scene,mousePos);
        //QuickDraw.drawRect(getPos(),getSize(),getFillColor());
        //getDisplayText().setPos(getPos());
        //getDisplayText().draw();
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
