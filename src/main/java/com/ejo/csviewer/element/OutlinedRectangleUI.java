package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import org.lwjgl.opengl.GL11;

public class OutlinedRectangleUI extends RectangleUI {

    public OutlinedRectangleUI(Vector pos, Vector size, ColorE color) {
        super(pos, size, color);
    }

    @Override
    public void drawElement(Scene scene, Vector mousePos) {
        vertices[1] = new Vector(0,getSize().getY());
        vertices[2] = getSize();
        vertices[3] = new Vector(getSize().getX(),0);
        GL11.glColor4f(getColor().getRed() / 255f, getColor().getGreen() / 255f, getColor().getBlue() / 255f,getColor().getAlpha() / 255f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (Vector vert : getVertices()) GL11.glVertex2f((float) getPos().getX() + (float) vert.getX(), (float) getPos().getY() + (float) vert.getY());
        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
    }
}
