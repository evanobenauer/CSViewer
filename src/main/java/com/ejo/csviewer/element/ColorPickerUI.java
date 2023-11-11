package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.setting.Container;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.ModeCycleUI;
import com.ejo.glowui.util.render.QuickDraw;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorPickerUI extends ModeCycleUI<ColorE> {

    public ColorPickerUI(Vector pos, Container<ColorE> container) {
        //TODO: Add more colors
        super("ColorPicker", pos, new Vector(20,20),ColorE.NULL, container,
                new ColorE(new Color(255, 100, 100)),
                new ColorE(new Color(255, 0, 0)),
                new ColorE(new Color(175, 0, 0)),
                new ColorE(new Color(255, 140, 50)),
                new ColorE(new Color(255, 100, 0)),
                new ColorE(new Color(200, 80, 0)),
                new ColorE(new Color(255, 255, 150)),
                new ColorE(new Color(255, 255, 0)),
                new ColorE(new Color(150, 150, 0)),
                new ColorE(new Color(130, 255, 130)),
                new ColorE(new Color(0, 200, 0)),
                new ColorE(new Color(0, 125, 0)),
                new ColorE(new Color(0, 120, 255)),
                new ColorE(new Color(0, 40, 255)),
                new ColorE(new Color(0, 0, 175)),
                new ColorE(new Color(100, 100, 200)),
                new ColorE(new Color(100, 50, 255)),
                new ColorE(new Color(50, 0, 200)),
                new ColorE(new Color(0, 0, 0)),
                new ColorE(new Color(100, 100, 100)),
                new ColorE(new Color(200,200,200)),
                new ColorE(new Color(255, 255, 255)));
    }

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {
        QuickDraw.drawRect(getPos(),getSize(),getContainer().get());
        drawBorder();
    }


    //TODO: Make this simple, do a modecycle for all colors in the rainbow. That sounds pretty good


    private void drawBorder() {
        Vector pos = getPos();
        Vector size = getSize();
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glLineWidth(10f);

        GL11.glColor4f(1,0,0,1);
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY());
        GL11.glColor4f(1,1,1,1);
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY());
        GL11.glColor4f(0, 0,1,1);
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY() + (float) size.getY());
        GL11.glColor4f(0,1,0,1);
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY() + (float) size.getY());

        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
    }
}
