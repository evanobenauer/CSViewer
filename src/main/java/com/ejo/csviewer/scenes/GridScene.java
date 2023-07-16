package com.ejo.csviewer.scenes;

import com.ejo.csviewer.element.CellUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.util.QuickDraw;

import java.util.ArrayList;

public class GridScene extends Scene {

    private final ArrayList<ArrayList<CellUI>> grid = new ArrayList<>();

    public GridScene() {
        super("Title");
        int width = 100;
        int height = 20;

        int rows = 10;
        int columns = 5;

        for (int row = 0; row < rows; row++) {
            grid.add(new ArrayList<>());
            for (int column = 0; column < columns; column++) {
                CellUI cell = new CellUI(new Vector(100 + column*(width + 1),100 + row*(height + 1)), new Vector(width,height));
                cell.setFillColor(new ColorE(200,200,200));
                grid.get(row).add(cell);
                addElements(cell);
            }
        }
    }

    @Override
    public void draw() {
        super.draw();
        QuickDraw.drawFPSTPS(this,new Vector(2,2),10,false);
    }

    public ArrayList<ArrayList<CellUI>> getGrid() {
        return grid;
    }

}
