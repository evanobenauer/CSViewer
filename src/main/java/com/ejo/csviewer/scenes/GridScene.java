package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.element.Cell;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.QuickDraw;

import java.util.ArrayList;

public class GridScene extends Scene {

    private final FileCSV file;

    private final ButtonUI saveButton = new ButtonUI(new Vector(10,10),new Vector(20,20),ColorE.BLUE,() -> {
        getFile().save();
    });

    private final ButtonUI addRowButton = new ButtonUI(new Vector(30,10),new Vector(20,20),ColorE.BLUE,() -> {
        getFile().addRow();
    });

    private final ButtonUI addColumnButton = new ButtonUI(new Vector(50,10),new Vector(20,20),ColorE.BLUE,() -> {
        getFile().addColumn();
    });

    public GridScene(FileCSV file) {
        super("Title");
        this.file = file;
        addElements(saveButton,addRowButton,addColumnButton);

        file.createGrid();
        file.load();

    }

    @Override
    public void draw() {
        //Draw Background
        QuickDraw.drawRect(Vector.NULL,getSize(),new ColorE(150,150,150));

        super.draw();
        drawCells(getCellStartIndex(),getCellEndIndex());
        QuickDraw.drawFPSTPS(this,new Vector(2,2),10,false);
    }

    @Override
    public void tick() {
        super.tick();
        tickCells(getCellStartIndex(),getCellEndIndex());
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        mouseCells(getCellStartIndex(),getCellEndIndex(),button,action,mods,mousePos);
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        keyCells(getCellStartIndex(),getCellEndIndex(),key,scancode,action,mods);
    }


    public void drawCells(int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            for (Cell cell : getFile().getCellGrid().get(i)) {
                cell.draw(this);
            }
        }
    }

    public void tickCells(int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            for (Cell cell : getFile().getCellGrid().get(i)) {
                cell.tick(this);
            }
        }
    }

    public void mouseCells(int startRow, int endRow, int button, int action, int mods, Vector mousePos) {
        for (int i = startRow; i < endRow; i++) {
            for (Cell cell : getFile().getCellGrid().get(i)) {
                cell.onMouseClick(this,button,action,mods,mousePos);
            }
        }
    }

    public void keyCells(int startRow, int endRow, int key, int scancode, int action, int mods) {
        for (int i = startRow; i < endRow; i++) {
            for (Cell cell : getFile().getCellGrid().get(i)) {
                cell.onKeyPress(this,key,scancode,action,mods);
            }
        }
    }


    private int getCellStartIndex() {
        return 0;
    }

    private int getCellEndIndex() {
        return getFile().getCellGrid().size();
    }

    public FileCSV getFile() {
        return file;
    }
}
