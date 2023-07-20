package com.ejo.csviewer.element;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.Container;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.time.StopWatch;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.TextFieldUI;
import com.ejo.glowui.util.QuickDraw;

import java.awt.*;

public class Cell extends TextFieldUI {

    private final FileCSV file;

    private final Setting<ColorE> fillColor;
    private final Setting<ColorE> outlineColor;

    private final int columnIndex;
    private final int rowIndex;

    public Cell(FileCSV file, int columnIndex, int rowIndex, Vector pos, Vector size, ColorE fill, ColorE outline) {
        super(pos, size, ColorE.WHITE,new Container<>(""),"",false);
        this.file = file;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;

        this.fillColor = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "fillColor",fill);
        this.outlineColor = new Setting<>(file.getSettingManager(),getColumnIndex() + "_" + getRowIndex() + "_" + "outlineColor",outline);
    }

    private final StopWatch cursorTimer = new StopWatch();

    @Override
    protected void drawWidget(Scene scene, Vector mousePos) {
        //Draw Background
        QuickDraw.drawRect(getPos(),getSize(),getFillColor());
        new OutlinedRectangleUI(getPos(),getSize(),getOutlineColor()).draw();

        double border = getSize().getY()/5;

        //Prepare Text
        String msg = (hasTitle() ? getTitle() + ": " : "") + getContainer().get();
        int size = (int) (getSize().getY() / 1.5);
        setUpDisplayText(msg,border,size);
        getDisplayText().setPos(getPos().getAdded(border, -2 + getSize().getY() / 2 - getDisplayText().getHeight() / 2));
        getDisplayText().setColor(ColorE.BLACK);

        //Draw Hint
        if (getContainer().get().equals(""))
            QuickDraw.drawText(getHint(), new Font("Arial", Font.PLAIN, size),
                    getPos().getAdded(border + getDisplayText().getWidth(), -2 + getSize().getY() / 2 - getDisplayText().getHeight() / 2),
                    ColorE.GRAY);

        //Draw Blinking Cursor
        if (isTyping()) {
            new OutlinedRectangleUI(getPos(),getSize(),ColorE.GREEN).draw();
            cursorTimer.start();
            if (cursorTimer.hasTimePassedS(1)) cursorTimer.restart();
            int alpha = cursorTimer.hasTimePassedMS(500) ? 255 : 0;
            double x = getPos().getX() + getDisplayText().getWidth() + border;
            double y = getPos().getY() + border;
            QuickDraw.drawRect(new Vector(x, y), new Vector(2, getSize().getY() - 2 * border), new ColorE(255, 255, 255, alpha));
        }

        //Draw Text Object
        getDisplayText().draw(scene, mousePos);
    }

    @Override
    public void onMouseClick(Scene scene, int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(scene, button, action, mods, mousePos);
        if (isMouseOver()) setOutlineColor(ColorE.WHITE);
    }

    @Override
    public void onKeyPress(Scene scene, int key, int scancode, int action, int mods) {
        super.onKeyPress(scene, key, scancode, action, mods);
    }

    public FileCSV getFile() {
        return file;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setFillColor(ColorE fillColor) {
        this.fillColor.set(fillColor);
    }

    public void setOutlineColor(ColorE outlineColor) {
        this.outlineColor.set(outlineColor);
    }


    public ColorE getFillColor() {
        return fillColor.get();
    }

    public ColorE getOutlineColor() {
        return outlineColor.get();
    }

}
