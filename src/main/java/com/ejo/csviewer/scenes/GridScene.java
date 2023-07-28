package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.element.Cell;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.Container;
import com.ejo.glowlib.util.NumberUtil;
import com.ejo.glowlib.util.StringUtil;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.SideBarUI;
import com.ejo.glowui.scene.elements.TextUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.scene.elements.widget.SliderUI;
import com.ejo.glowui.scene.elements.widget.ToggleUI;
import com.ejo.glowui.util.Fonts;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.Mouse;
import com.ejo.glowui.util.QuickDraw;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class GridScene extends Scene {

    private final FileCSV file;
    private int cellStartIndex;

    private final ArrayList<ButtonUI> columnButtonList = new ArrayList<>();
    private final ArrayList<ButtonUI> rowButtonList = new ArrayList<>();

    private final ButtonUI buttonSave = new ButtonUI(new Vector(20, 50), new Vector(20, 20), ColorE.BLUE, () -> getFile().save());

    private final ButtonUI buttonAddRow = new ButtonUI(new Vector(30, 10), new Vector(20, 20), ColorE.BLUE, () -> {
        getFile().addRow();
        addRowButton();
    });

    private final ButtonUI buttonAddColumn = new ButtonUI(new Vector(50, 10), new Vector(20, 20), ColorE.BLUE, () -> {
        getFile().addColumn();
        addColumnButton();
    });


    private ToggleUI settingBold;
    private ToggleUI settingItalic;
    private final SliderUI<Double> settingOutlineWidth;

    private final SideBarUI settingsBar = new SideBarUI("Cell Settings", SideBarUI.Type.TOP, 40, false, ColorE.BLACK,
            settingBold = new ToggleUI("B", new Vector(40, 10), new Vector(20, 20), ColorE.BLUE, new Container<>(false)),
            settingItalic = new ToggleUI("I", new Vector(70, 10), new Vector(20, 20), ColorE.BLUE, new Container<>(false)),
            settingOutlineWidth = new SliderUI<>("Outline", new Vector(100, 10), new Vector(100, 20), ColorE.BLUE, new Container<>(0d),0.6d,3d,.1d, SliderUI.Type.FLOAT,false)
    );


    public GridScene(FileCSV file) {
        super(file.getName() + " Scene");
        this.file = file;

        addElements(buttonSave); //Experimental
        addElements(settingsBar);

        //Set Setting Data
        settingBold.getDisplayText().setModifier(Font.BOLD);
        settingItalic.getDisplayText().setModifier(Font.ITALIC);
        settingsBar.getButton().disable(true);

        //Create Grid, Load Settings
        file.createGrid();
        file.load();

        //Create Column Buttons
        for (int column = 0; column < getFile().getColumnCount().get(); column++) addColumnButton();
        for (int row = 0; row < getFile().getRowCount().get(); row++) addRowButton();

        setCellStartIndex(0); //Sets the index to the start
    }

    @Override
    public void draw() {
        //Draw Background
        QuickDraw.drawRect(Vector.NULL, getSize(), new ColorE(150, 150, 150));

        //Draw Sheet Title
        TextUI title = new TextUI(getFile().getName(),Fonts.getDefaultFont(40),Vector.NULL.getAdded(0,22),ColorE.BLACK);
        title.setFont("Verdana");
        title.setModifier(Font.ITALIC);
        title.drawCentered(this,getWindow().getScaledMousePos(),new Vector(getSize().getX(),0));

        try {
            Vector startPos = new Vector(40, 70);
            drawCells(startPos, 1);
            drawButtons(startPos, 1);
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        super.draw();
        //QuickDraw.drawFPSTPS(this, new Vector(2, 2), 10, false);
    }

    @Override
    public void tick() {
        super.tick();
        try {
            //Tick Cells
            for (int i = getCellStartIndex(); i < getCellEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) {
                    cell.tick(this);
                }
            }
            //Tick Column Buttons
            for (ButtonUI button : columnButtonList) {
                button.tick(this);
            }
            //Tick Row Buttons
            for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
                rowButtonList.get(row).tick(this);
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        try {
            //Key Cells
            for (int i = getCellStartIndex(); i < getCellEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) {
                    cell.onKeyPress(this, key, scancode, action, mods);
                }
            }
            //Key Column Buttons
            for (ButtonUI but : columnButtonList) {
                but.onKeyPress(this, key, scancode, action, mods);
            }
            //Key Row Buttons
            for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
                rowButtonList.get(row).onKeyPress(this, key, scancode, action, mods);
            }
        } catch (ConcurrentModificationException ignored) {
        }

        //ARROW KEY SCROLL CODE
        if (action == Key.ACTION_PRESS || action == Key.ACTION_HOLD) {
            if (key == Key.KEY_UP.getId()) setCellStartIndex(getCellStartIndex() - 1);
            if (key == Key.KEY_DOWN.getId()) setCellStartIndex(getCellStartIndex() + 1);
        }
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        try {
            //Mouse Cells
            for (int i = getCellStartIndex(); i < getCellEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) {
                    cell.onMouseClick(this, button, action, mods, mousePos);
                }
            }
            //Mouse Column Buttons
            for (ButtonUI but : columnButtonList) but.onMouseClick(this, button, action, mods, mousePos);

            //Mouse Row Buttons
            for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) rowButtonList.get(row).onMouseClick(this, button, action, mods, mousePos);

        } catch (ConcurrentModificationException ignored) {
        }

        //Close settings bar when clicked outside
        if (getWindow().getScaledMousePos().getY() > settingsBar.getWidth()) {
            if (button == Mouse.BUTTON_LEFT.getId()) {
                settingsBar.setOpen(false);
                for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
                    for (int column = 0; column < getFile().getCellGrid().get(row).size(); column++) {
                        Cell cell = getFile().getCellGrid().get(row).get(column);
                        cell.setSelected(false);
                    }
                }
            }
        }

        //Open Cell Settings
        if (button == Mouse.BUTTON_RIGHT.getId() && action == Mouse.ACTION_RELEASE) {
            for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
                for (int column = 0; column < getFile().getCellGrid().get(row).size(); column++) {
                    Cell cell = getFile().getCellGrid().get(row).get(column);
                    if (cell.isMouseOver()) {
                        cell.setTyping(false);
                        settingBold.setContainer(cell.isTextBold());
                        settingItalic.setContainer(cell.isTextItalic());
                        settingOutlineWidth.setContainer(cell.getOutlineWidth());
                        String title = "Cell: " + String.valueOf(StringUtil.getLetterFromIndex(column)).toUpperCase() + row + " Settings";
                        if (settingsBar.getTitle().equals(title)) {
                            settingsBar.setOpen(!settingsBar.isOpen());
                            cell.setSelected(!cell.isSelected());
                        } else {
                            settingsBar.setOpen(true);
                            cell.setSelected(true);
                        }
                        settingsBar.setTitle(title);
                    } else {
                        cell.setSelected(false);
                        cell.setTyping(false);
                    }
                }
            }
        }
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        setCellStartIndex(getCellStartIndex() - scroll);
    }


    public void drawCells(Vector gridPos, int separation) throws ConcurrentModificationException {
        int x = (int) gridPos.getX();
        int y = (int) gridPos.getY();

        for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
            for (int column = 0; column < getFile().getCellGrid().get(row).size(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);
                cell.setPos(new Vector(x, y));
                cell.setSize(new Vector(getFile().getColumnWidthSettings().get(column).get(), getFile().getRowHeightSettings().get(row).get()));
                cell.draw(this);
                x += getFile().getColumnWidthSettings().get(column).get() + separation;
            }
            x = (int) gridPos.getX();
            y += getFile().getRowHeightSettings().get(row).get() + separation;
        }
    }

    public void drawButtons(Vector gridPos, int separation) throws ConcurrentModificationException {
        int size = 20;
        int x = (int) gridPos.getX();
        int y = (int) gridPos.getY() - size - separation;

        for (int column = 0; column < columnButtonList.size(); column++) {
            ButtonUI button = columnButtonList.get(column);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(getFile().getColumnWidthSettings().get(column).get(), size));
            button.draw(this);
            x += getFile().getColumnWidthSettings().get(column).get() + separation;
        }
        x = (int) gridPos.getX() - size - separation;
        y = (int) gridPos.getY();
        for (int row = getCellStartIndex(); row < getCellEndIndex(); row++) {
            ButtonUI button = rowButtonList.get(row);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(size, getFile().getRowHeightSettings().get(row).get()));
            button.draw(this);
            y += getFile().getRowHeightSettings().get(row).get() + separation;
        }
    }

    private void runRowButtonAction(int index) {
        System.out.println("ROW" + index);
    }

    private void runColumnButtonAction(int index) {
        System.out.println("COLUMN" + index);
    }


    private void addColumnButton() {
        int index = columnButtonList.size();
        columnButtonList.add(new ButtonUI(String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase(), Vector.NULL, Vector.NULL, ColorE.GRAY, () -> {
            runColumnButtonAction(index);
        }));
    }

    private void addRowButton() {
        int index = rowButtonList.size();
        rowButtonList.add(new ButtonUI(String.valueOf(index), Vector.NULL, Vector.NULL, ColorE.GRAY, () -> {
            runRowButtonAction(index);
        }));
    }


    private void setCellStartIndex(int val) {
        this.cellStartIndex = Math.min(val, getFile().getCellGrid().size() - 2);
    }


    public ArrayList<ButtonUI> getColumnButtonList() {
        return columnButtonList;
    }

    public ArrayList<ButtonUI> getRowButtonList() {
        return rowButtonList;
    }

    private int getCellStartIndex() {
        return Math.max(this.cellStartIndex, 0);
    }

    private int getCellEndIndex() {
        int index = getCellStartIndex() + (int) getSize().getY() / 24;
        return Math.min(index, getFile().getCellGrid().size() - 1);
    }

    public FileCSV getFile() {
        return file;
    }
}
