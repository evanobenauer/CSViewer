package com.ejo.csviewer.element;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

@Deprecated
public class TextH extends ElementUI {

    private Font font;
    private String text;
    private ColorE color;

    private float scale = 1;

    private FontMetrics fontMetrics;
    private ByteBuffer fontImageBuffer;

    public TextH(String text, Font font, Vector pos, ColorE color) {
        super(pos, true,true);
        this.font = font;
        this.text = text;
        this.color = color;

        this.fontMetrics = createFontMetrics(font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    @Override
    protected void drawElement(Scene scene, Vector mousePos) {
        renderText(scene, getPos().getX(), getPos().getY());
    }

    public void drawCentered(Scene scene, Vector mousePos, Vector size) {
        if (!shouldRender()) return;
        renderText(scene, getPos().getX() + size.getX()/2 - getHeight()/2 - 2,getPos().getY() - 2 + size.getY()/2 - getWidth()/2 + 10);
    }

    private void renderText(Scene scene, double x, double y) {
        if (getText().equals("")) return;
        Vector pos = new Vector(x,y - getHeight());
        GL11.glRasterPos2f((float)pos.getX(), (float)pos.getY());
        GL11.glDrawPixels((int)getHeight() + 4,(int)getWidth() + 4, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fontImageBuffer);
    }

    @Override
    protected void tickElement(Scene scene, Vector mousePos) {
    }


    private FontMetrics createFontMetrics(Font font) {
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = tempImage.getGraphics();
        graphics.setFont(font);
        return graphics.getFontMetrics();
    }

    private ByteBuffer createFontImageBuffer() {
        if (getText().equals("")) return null;
        BufferedImage fontImage = new BufferedImage((int)getHeight() + 4, (int)getWidth() + 4, BufferedImage.TYPE_INT_ARGB);

        //Draw Text Using Graphics
        Graphics2D graphics = (Graphics2D) fontImage.getGraphics();
        graphics.setFont(getFont());
        graphics.setColor(new Color(getColor().getHash()));

        graphics.translate(0,getWidth() + 4);

        graphics.scale(getScale(),getScale());

        graphics.rotate(-Math.PI/2);

        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] text = getText().split("\\\\n");
        for (int i = 0; i < text.length; i++) {
            graphics.drawString(text[i], 0, fontMetrics.getAscent()*(i+1));
        }

        //Get DataBuffer From Graphics
        DataBuffer dataBuffer = fontImage.getRaster().getDataBuffer();
        int[] imageData = ((DataBufferInt) dataBuffer).getData();
        ByteBuffer buffer = BufferUtils.createByteBuffer(imageData.length * 16);
        for (int pixel : imageData) buffer.putInt(pixel);
        buffer.flip();
        return buffer;
    }


    @Override
    public boolean updateMouseOver(Vector mousePos) {
        return false;
    }


    public void setColor(ColorE color) {
        if (this.color.equals(color)) return;
        this.color = color;
        this.fontMetrics = createFontMetrics(font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setFont(Font font) {
        if (this.font.equals(font)) return;
        this.font = font;
        this.fontMetrics = createFontMetrics(font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setFont(String font) {
        if (this.font.getName().equals(font)) return;
        int size = this.font.getSize();
        int style = this.font.getStyle();
        this.font = new Font(font,style,size);
        this.fontMetrics = createFontMetrics(this.font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setSize(int size) {
        if (this.font.getSize() == size) return;
        String name = this.font.getName();
        int style = this.font.getStyle();
        this.font = new Font(name,style,size);
        this.fontMetrics = createFontMetrics(this.font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setModifier(int modifier) {
        if (this.font.getStyle() == modifier) return;
        String name = this.font.getName();
        int size = this.font.getSize();
        this.font = new Font(name,modifier,size);
        this.fontMetrics = createFontMetrics(this.font);
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setText(String text) {
        if (this.text.equals(text)) return;
        this.text = text;
        this.fontImageBuffer = createFontImageBuffer();
    }

    public void setScale(double scale) {
        this.scale = (float)scale;
        this.fontImageBuffer = createFontImageBuffer();
    }


    public ColorE getColor() {
        return color;
    }

    public Font getFont() {
        return font;
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics;
    }

    public String getText() {
        return text;
    }

    public float getScale() {
        return scale;
    }

    public Vector getSize() {
        return new Vector(getHeight(),getWidth());
    }

    public double getWidth() {
        String[] text = getText().split("\\\\n");
        return fontMetrics.stringWidth(text[getLargestStringIndex(text)]) * getScale();
    }

    public double getHeight() {
        return (getFont().getSize() * getText().split("\\\\n").length * getScale());
    }

    private int getLargestStringIndex(String[] list) {
        int index = 0;
        for (int i = 0; i < list.length; i++) {
            if (getFontMetrics().stringWidth(list[i]) > getFontMetrics().stringWidth(list[index])) index = i;
        }
        return index;
    }

}
