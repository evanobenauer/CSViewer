package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.element.Cell;
import com.ejo.csviewer.element.TextH;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.Container;
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

public class EditorScene extends Scene {

    private final FileCSV file;
    private int cellStartIndex;

    private final ArrayList<ButtonUI> columnButtonList = new ArrayList<>();
    private final ArrayList<ButtonUI> rowButtonList = new ArrayList<>();

    private final ColorE settingsBarColor = new ColorE(100,100,100);

    private ToggleUI settingToggleBold;
    private ToggleUI settingToggleItalic;
    private final SliderUI<Double> settingSliderOutlineWidth;
    private final SideBarUI settingsBar = new SideBarUI("Cell Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            settingToggleBold = new ToggleUI("B", new Vector(40, 10), new Vector(20, 20), ColorE.BLUE, new Container<>(false)),
            settingToggleItalic = new ToggleUI("I", new Vector(70, 10), new Vector(20, 20), ColorE.BLUE, new Container<>(false)),
            settingSliderOutlineWidth = new SliderUI<>("Outline", new Vector(100, 10), new Vector(100, 20), ColorE.BLUE, new Container<>(0d),.6d,3d,.2d, SliderUI.Type.FLOAT,true)
    );

    private final SliderUI<Integer> settingSliderColumnWidth;
    private final ButtonUI deleteColumnButton;
    private final SideBarUI columnSettingsBar = new SideBarUI("Column Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            settingSliderColumnWidth = new SliderUI<>("Width",new Vector(100,10),new Vector(200,20),ColorE.BLUE,new Container<>(100),20,1000,1, SliderUI.Type.INTEGER,true),
            deleteColumnButton = new ButtonUI("Delete Column", new Vector(400, 10), new Vector(100,20),ColorE.BLUE, ButtonUI.MouseButton.LEFT,null)
    );

    private final SliderUI<Integer> settingSliderRowHeight;
    private final ButtonUI deleteRowButton;
    private final SideBarUI rowSettingsBar = new SideBarUI("Row Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            settingSliderRowHeight = new SliderUI<>("Height",new Vector(100,10),new Vector(200,20),ColorE.BLUE,new Container<>(20),20,500,1, SliderUI.Type.INTEGER,true),
            deleteRowButton = new ButtonUI("Delete Row", new Vector(400, 10), new Vector(100,20),ColorE.BLUE, ButtonUI.MouseButton.LEFT,null)
    );

    private final ButtonUI buttonSave = new ButtonUI(new Vector(20, 50), new Vector(20, 20), ColorE.BLUE, ButtonUI.MouseButton.LEFT, () -> {
        getFile().save();
    });

    private final ButtonUI buttonAddRow = new ButtonUI("Add Row",new Vector(30, 10), new Vector(200, 20), ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
        getFile().addRow();
        createRowButton();
    });

    private final ButtonUI buttonAddColumn = new ButtonUI(new Vector(50, 10), new Vector(20, 200), ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
        getFile().addColumn();
        createColumnButton();
    });

    public EditorScene(FileCSV file) {
        super(file.getName() + " Scene");
        this.file = file;

        addElements(buttonSave,buttonAddRow,buttonAddColumn,settingsBar,columnSettingsBar,rowSettingsBar);

        //Create Grid, Load Settings
        file.createGrid();
        file.load();

        //Create Buttons
        for (int column = 0; column < getFile().getColumnCount().get(); column++) createColumnButton();
        for (int row = 0; row < getFile().getRowCount().get(); row++) createRowButton();

        //Set Setting Data
        settingToggleBold.getDisplayText().setModifier(Font.BOLD);
        settingToggleItalic.getDisplayText().setModifier(Font.ITALIC);
        settingsBar.getButton().disable(true);
        columnSettingsBar.getButton().disable(true);
        rowSettingsBar.getButton().disable(true);

        setRowStartIndex(0); //Sets the index to the start
    }

    @Override
    public void draw() {
        //Draw Background
        QuickDraw.drawRect(Vector.NULL, getSize(), new ColorE(150, 150, 150));

        //Draw Sheet Title
        TextUI title = new TextUI(getFile().getName(), Fonts.getDefaultFont(40), Vector.NULL.getAdded(0, 22), ColorE.BLACK);
        title.setFont("Verdana");
        title.setModifier(Font.ITALIC);
        title.drawCentered(this, getWindow().getScaledMousePos(), new Vector(getSize().getX(), 0));

        try {
            Vector startPos = new Vector(40, 70);
            drawCells(startPos, 1);
            drawButtons(startPos, 1);
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        //Draw Add Row Button
        if (getRowEndIndex() == getFile().getCellGrid().size() - 1) {
            buttonAddRow.setPos(new Vector(getSize().getX() / 2 - buttonAddRow.getSize().getX() / 2,getFile().getCellGrid().get(getRowEndIndex() - 1).get(0).getPos().getY() + 30));
            buttonAddRow.disable(false);
        } else {
            buttonAddRow.disable(true);
        }

        //Draw Add Column Button
        double gridWidth = 0;
        for (Cell cell : getFile().getCellGrid().get(getRowStartIndex())) gridWidth += cell.getSize().getX();
        buttonAddColumn.setPos(getSize().getAdded(0, -getSize().getY() / 2).getAdded(60 - getSize().getX() + gridWidth, -buttonAddColumn.getSize().getY() / 2));

        super.draw();

        //Draw Add Column Text
        TextH rotText = new TextH("Add Column", Fonts.getDefaultFont(18), buttonAddColumn.getPos(), ColorE.WHITE);
        rotText.drawCentered(this, getWindow().getScaledMousePos(), buttonAddColumn.getSize());

        QuickDraw.drawFPSTPS(this, new Vector(2, 2), 10, false);
    }

    @Override
    public void tick() {
        super.tick();
        try {
            //Tick Cells
            for (int i = getRowStartIndex(); i < getRowEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) cell.tick(this);
            }
            //Tick Column Buttons
            for (ButtonUI button : columnButtonList) button.tick(this);

            //Tick Row Buttons
            for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) rowButtonList.get(row).tick(this);

        } catch (ConcurrentModificationException | IndexOutOfBoundsException ignored) {
        }
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        try {
            //Key Cells
            for (int i = getRowStartIndex(); i < getRowEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) cell.onKeyPress(this, key, scancode, action, mods);
            }
            //Key Column Buttons
            for (ButtonUI but : columnButtonList) but.onKeyPress(this, key, scancode, action, mods);

            //Key Row Buttons
            for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) rowButtonList.get(row).onKeyPress(this, key, scancode, action, mods);

        } catch (ConcurrentModificationException | IndexOutOfBoundsException ignored) {
        }

        //ARROW KEY SCROLL CODE
        if (action == Key.ACTION_PRESS || action == Key.ACTION_HOLD) {
            if (key == Key.KEY_UP.getId()) setRowStartIndex(getRowStartIndex() - 1);
            if (key == Key.KEY_DOWN.getId()) setRowStartIndex(getRowStartIndex() + 1);
        }
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        try {
            //Mouse Cells
            for (int i = getRowStartIndex(); i < getRowEndIndex(); i++) {
                for (Cell cell : getFile().getCellGrid().get(i)) cell.onMouseClick(this, button, action, mods, mousePos);
            }
            //Mouse Column Buttons
            for (ButtonUI but : columnButtonList) but.onMouseClick(this, button, action, mods, mousePos);

            //Mouse Row Buttons
            for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) rowButtonList.get(row).onMouseClick(this, button, action, mods, mousePos);

        } catch (ConcurrentModificationException | IndexOutOfBoundsException ignored) {
        }


        //Close Column Settings if column has been deleted
        String colTitle = "Column: " + String.valueOf(StringUtil.getLetterFromIndex(getColumnButtonList().size())).toUpperCase() + " Settings";
        if (columnSettingsBar.getTitle().equals(colTitle)) columnSettingsBar.setOpen(false);

        //Close row settings if row has been deleted
        String rowTitle = "Row: " + (getRowButtonList().size() - 1) + " Settings";
        if (rowSettingsBar.getTitle().equals(rowTitle)) rowSettingsBar.setOpen(false);

        //Close Cell Settings bar when clicked outside
        if (getWindow().getScaledMousePos().getY() > settingsBar.getWidth()) {
            if (button == Mouse.BUTTON_LEFT.getId()) {
                settingsBar.setOpen(false);
            }
        }

        //Close Column Settings Bar When Click Outside
        for (ButtonUI columnButton : getColumnButtonList()) {
            String title = "Column: " + String.valueOf(StringUtil.getLetterFromIndex(getColumnButtonList().indexOf(columnButton))).toUpperCase() + " Settings";
            if (!columnButton.isMouseOver() && columnSettingsBar.getTitle().equals(title) && getWindow().getScaledMousePos().getY() > columnSettingsBar.getWidth()) {
                columnSettingsBar.setOpen(false);
            }
        }

        //Close Row Settings Bar When Click Outside
        //for (ButtonUI rowButton : getRowButtonList()) {
        for (int row = 0; row < getRowButtonList().size(); row++) {
            ButtonUI rowButton = getRowButtonList().get(row);
            String title = "Row: " + getRowButtonList().indexOf(rowButton) + " Settings";
            if (!rowButton.isMouseOver() && rowSettingsBar.getTitle().equals(title) && getWindow().getScaledMousePos().getY() > rowSettingsBar.getWidth()) {
                rowSettingsBar.setOpen(false);
            }
        }


        //Set Cell Selected, Open Cell Settings Bar
        for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) {
            for (int column = 0; column < getFile().getCellGrid().get(row).size(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);

                if (cell.isMouseOver()) {
                    if (button == Mouse.BUTTON_RIGHT.getId() && action == Mouse.ACTION_RELEASE) {
                        cell.setSelected(!cell.isSelected());
                        cell.setTyping(false);
                        setCellSettingContainers(cell);

                        String title = "Cell: " + String.valueOf(StringUtil.getLetterFromIndex(column)).toUpperCase() + row + " Settings";
                        if (settingsBar.getTitle().equals(title)) {
                            settingsBar.setOpen(!settingsBar.isOpen());
                        } else {
                            settingsBar.setOpen(true);
                        }
                        settingsBar.setTitle(title);
                    }
                } else if (getWindow().getScaledMousePos().getY() > settingsBar.getWidth()) {
                     cell.setSelected(false);
                }

            }
        }

        //TODO: Create row/column selection using a selection container and an outlined rectangle
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        setRowStartIndex(getRowStartIndex() - scroll);
    }


    public void drawCells(Vector gridPos, int separation) throws ConcurrentModificationException {
        int x = (int) gridPos.getX();
        int y = (int) gridPos.getY();

        for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) {
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
        for (int row = getRowStartIndex(); row < getRowEndIndex(); row++) {
            ButtonUI button = rowButtonList.get(row);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(size, getFile().getRowHeightSettings().get(row).get()));
            button.draw(this);
            y += getFile().getRowHeightSettings().get(row).get() + separation;
        }
    }

    private void createColumnButton() {
        int index = columnButtonList.size();
        columnButtonList.add(new ButtonUI(String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase(), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
            //Set Containers
            settingSliderColumnWidth.setContainer(getFile().getColumnWidthSettings().get(index));
            deleteColumnButton.setAction(() -> {
                getFile().deleteColumn(index);
                getColumnButtonList().clear();
                getFile().getColumnCount().set(getFile().getColumnCount().get() - 1); //Shift Column Count On Delete
                for (int column = 0; column < getFile().getColumnCount().get(); column++) createColumnButton();
            });

            //Set Open, Set Title
            String title = "Column: " + String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase() + " Settings";
            if (columnSettingsBar.getTitle().equals(title)) {
                columnSettingsBar.setOpen(!columnSettingsBar.isOpen());
            } else {
                columnSettingsBar.setOpen(true);
            }
            columnSettingsBar.setTitle(title);
        }));
    }

    private void createRowButton() {
        int index = rowButtonList.size();
        rowButtonList.add(new ButtonUI(String.valueOf(index), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
            //Set Containers
            settingSliderRowHeight.setContainer(getFile().getRowHeightSettings().get(index));
            deleteRowButton.setAction(() -> {
                getFile().deleteRow(index);
                getRowButtonList().clear();
                getFile().getRowCount().set(getFile().getRowCount().get() - 1); //Shift Row Count On Delete
                for (int row = 0; row < getFile().getRowCount().get(); row++) createRowButton();
            });

            //Set Open, Set Title
            String title = "Row: " + index + " Settings";
            if (rowSettingsBar.getTitle().equals(title)) {
                rowSettingsBar.setOpen(!rowSettingsBar.isOpen());
            } else {
                rowSettingsBar.setOpen(true);
            }
            rowSettingsBar.setTitle(title);
        }));
    }

    private void setCellSettingContainers(Cell cell) {
        settingToggleBold.setContainer(cell.isTextBold());
        settingToggleItalic.setContainer(cell.isTextItalic());
        settingSliderOutlineWidth.setContainer(cell.getOutlineWidth());
    }

    private void setRowStartIndex(int val) {
        this.cellStartIndex = Math.min(val, getFile().getCellGrid().size() - 2);
    }


    public ArrayList<ButtonUI> getColumnButtonList() {
        return columnButtonList;
    }

    public ArrayList<ButtonUI> getRowButtonList() {
        return rowButtonList;
    }


    private int getRowStartIndex() {
        return Math.max(this.cellStartIndex, 0);
    }

    private int getRowEndIndex() {
        int index = getRowStartIndex() + (int) getSize().getY() / 22;
        return Math.min(index, getFile().getCellGrid().size() - 1);
    }

    public FileCSV getFile() {
        return file;
    }

}
