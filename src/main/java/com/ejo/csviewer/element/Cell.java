package com.ejo.csviewer.element;

import com.ejo.csviewer.Main;
import com.ejo.csviewer.data.FileCSV;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowlib.setting.Container;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.time.StopWatch;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.widget.TextFieldUI;
import com.ejo.glowui.util.DrawUtil;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.Mouse;
import com.ejo.glowui.util.QuickDraw;

import java.awt.*;


public class Cell extends TextFieldUI {

    private final FileCSV file;

    private boolean selected;

    private final Setting<Boolean> textBold;
    private final Setting<Boolean> textItalic;

    private final Setting<ColorE> textColor;
    private final Setting<ColorE> fillColor;
    private final Setting<ColorE> outlineColor;
    private final Setting<Double> outlineWidth;

    private final int columnIndex;
    private final int rowIndex;

    public Cell(FileCSV file, int columnIndex, int rowIndex, Vector pos, Vector size, ColorE textColor, ColorE fillColor, ColorE outlineColor) {
        super(pos, size, ColorE.WHITE,new Container<>(""),"",false);
        this.file = file;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;

        this.selected = false;

        this.textBold = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "textBold",false);
        this.textItalic = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "textItalic",false);

        this.textColor = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "textColor",textColor);
        this.fillColor = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "fillColor",fillColor);
        this.outlineColor = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "outlineColor",outlineColor);
        this.outlineWidth = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "outlineWidth",.6d);

        hoverAnimation.unsubscribe();
    }

    private final StopWatch cursorTimer = new StopWatch();
    private boolean blinked = false;

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {
        //Draw Background
        QuickDraw.drawRect(getPos(),getSize(),getFillColor().get());
        new RectangleUI(getPos(),getSize(),true,getOutlineWidth().get().floatValue(),getOutlineColor().get()).draw();

        double border = 4;//getSize().getY()/5;

        //Prepare Text
        String msg = (hasTitle() ? getTitle() + ": " : "") + getContainer().get();
        //int size = (int) (getSize().getY() / 1.5);
        int size = 13;
        setUpDisplayText(msg,border,size);
        getDisplayText().setPos(getPos().getAdded(border, getSize().getY() / 2 - getDisplayText().getHeight() / 2));
        getDisplayText().setColor(getTextColor().get());
        getDisplayText().setModifier(Font.PLAIN);
        if (isTextBold().get() && isTextItalic().get()) getDisplayText().setModifier(Font.BOLD | Font.ITALIC);
        else if (isTextBold().get()) getDisplayText().setModifier(Font.BOLD);
        else if (isTextItalic().get()) getDisplayText().setModifier(Font.ITALIC);

        //Draw Hint
        if (getContainer().get().equals(""))
            QuickDraw.drawText(getHint(), new Font("Arial", Font.PLAIN, size),
                    getPos().getAdded(border + getDisplayText().getWidth(), -2 + getSize().getY() / 2 - getDisplayText().getHeight() / 2),
                    ColorE.GRAY);

        //Draw Blinking Cursor
        if (isTyping()) {
            new RectangleUI(getPos(),getSize(),true,Math.max(2,getOutlineWidth().get().floatValue()),ColorE.GREEN).draw();
            int alpha = blinked ? 255 : 0;

            String[] text = getDisplayText().getText().split("\\\\n");
            String lastRow = text[text.length - 1];
            double lastRowWidth = getDisplayText().getFontMetrics().stringWidth(lastRow) * getDisplayText().getScale();

            double x = getPos().getX() + lastRowWidth + border;
            double height = size*getDisplayText().getScale();
            double y = getPos().getY() + getSize().getY()/2 - height/2 + (double) size * text.length / 2 - border*1.5;
            QuickDraw.drawRect(new Vector(x, y), new Vector(2, height), getTextColor().get().alpha(alpha));
        }
        if (isSelected()) new RectangleUI(getPos(),getSize(),true,Math.max(2,getOutlineWidth().get().floatValue()),ColorE.BLUE.green(175)).draw();

        //Draw Text Object
        getDisplayText().draw(scene, mousePos);

    }

    @Override
    protected void tickWidget(Scene scene, Vector mousePos) {
        super.tickWidget(scene, mousePos);

        //Do Cursor Blink
        cursorTimer.start();
        if (cursorTimer.hasTimePassedS(1) && isTyping()) {
            DrawUtil.forceRenderFrame();
            blinked = !blinked;
            cursorTimer.restart();
        }
    }

    @Override
    public void onKeyPress(Scene scene, int key, int scancode, int action, int mods) {
        if (key == Key.KEY_COMMA.getId() && !(Key.KEY_RSHIFT.isKeyDown() || Key.KEY_LSHIFT.isKeyDown())) return;
        if ((Key.KEY_LCONTROL.isKeyDown() || Key.KEY_RCONTROL.isKeyDown()) && action == Key.ACTION_PRESS && (isTyping() || isSelected()) && (key == Key.KEY_B.getId() || key == Key.KEY_I.getId())) {
            if (key == Key.KEY_B.getId()) setTextBold(!isTextBold().get());
            if (key == Key.KEY_I.getId()) setTextItalic(!isTextItalic().get());
            return;
        }
        super.onKeyPress(scene, key, scancode, action, mods);
    }

    @Override
    public void onMouseClick(Scene scene, int button, int action, int mods, Vector mousePos) {
        if (isMouseOver()) {
            if (button == Mouse.BUTTON_LEFT.getId() && action == Mouse.ACTION_CLICK) {
                setTyping(!isTyping());
                setSelected(false);
            }
        } else {
            if (action == Mouse.ACTION_CLICK) {
                if (isTyping()) setTyping(false);
            }
        }
    }

    public void setTextColor(ColorE textColor) {
        getTextColor().set(textColor);
    }

    public void setFillColor(ColorE fillColor) {
        getFillColor().set(fillColor);
    }

    public void setOutlineColor(ColorE outlineColor) {
       getOutlineColor().set(outlineColor);
    }

    public void setOutlineWidth(double outlineWidth) {
        getOutlineWidth().set(outlineWidth);
    }

    public void setTextBold(boolean bold) {
        isTextBold().set(bold);
    }

    public void setTextItalic(boolean italic) {
        isTextItalic().set(italic);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public Setting<ColorE> getTextColor() {
        return textColor;
    }

    public Setting<ColorE> getFillColor() {
        return fillColor;
    }

    public Setting<ColorE> getOutlineColor() {
        return outlineColor;
    }

    public Setting<Double> getOutlineWidth() {
        return outlineWidth;
    }

    public Setting<Boolean> isTextBold() {
        return textBold;
    }

    public Setting<Boolean> isTextItalic() {
        return textItalic;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public FileCSV getFile() {
        return file;
    }

}
