package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.element.Cell;
import com.ejo.csviewer.element.TextH;
import com.ejo.glowlib.event.EventAction;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.setting.Container;
import com.ejo.glowlib.util.StringUtil;
import com.ejo.glowui.event.EventRegistry;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.SideBarUI;
import com.ejo.glowui.scene.elements.TextUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
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

    private final ArrayList<ButtonUI> columnButtonList = new ArrayList<>();
    private final ArrayList<ButtonUI> rowButtonList = new ArrayList<>();

    private final Container<Integer> selectedColumnIndex = new Container<>(-1);
    private final Container<Integer> selectedRowIndex = new Container<>(-1);

    private int rowStartIndex;
    private int columnStartIndex;


    private final ColorE settingsBarColor = new ColorE(100,100,100);

    private ToggleUI settingToggleBold;
    private ToggleUI settingToggleItalic;
    private final SliderUI<Double> settingSliderOutlineWidth;
    private final SideBarUI cellSettingsBar = new SideBarUI("Cell Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
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


    @SuppressWarnings("ALL")
    private final ButtonUI buttonSave = new ButtonUI(new Vector(20, 50), new Vector(20, 20), ColorE.BLUE, ButtonUI.MouseButton.LEFT, () -> getFile().save());

    private final ButtonUI buttonAddRow = new ButtonUI("Add Row",new Vector(30, 10), new Vector(200, 20), ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
        getFile().addRow();
        createNewRowButton();
    });

    private final ButtonUI buttonAddColumn = new ButtonUI(new Vector(50, 10), new Vector(20, 200), ColorE.GRAY, ButtonUI.MouseButton.LEFT,() -> {
        getFile().addColumn();
        createNewColumnButton();
    });

    private final ButtonUI buttonScrollLeft = new ButtonUI("<",Vector.NULL,new Vector(20,40),ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        setColumnStartIndex(Math.max(getColumnStartIndex() - 1,0));
    });

    private final ButtonUI buttonScrollRight = new ButtonUI(">",Vector.NULL,new Vector(20,40),ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        setColumnStartIndex(Math.min(getColumnStartIndex() + 1,getFile().getColumnCount().get() - 1));
    });


    /**
     * This maintenance thread injection modifies the current animation injections included in all widgets. This will run the animations ONLY for the selected widgets
     * to accommodate for larger csv files. All widget injections are unsubscribed and routed through this action
     */
    public final EventAction animationInjection = new EventAction(EventRegistry.EVENT_RUN_MAINTENANCE, () -> {
        try {
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
                for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                    Cell cell = getFile().getCellGrid().get(row).get(column);
                    cell.hoverAnimation.run();
                }
                getRowButtonList().get(row).hoverAnimation.run();
            }
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) getColumnButtonList().get(column).hoverAnimation.run();

        } catch (IndexOutOfBoundsException ignored) {
        }
    });


    public EditorScene(FileCSV file) {
        super(file.getName() + " Scene");
        this.file = file;

        addElements(buttonSave,buttonAddRow,buttonAddColumn, cellSettingsBar,columnSettingsBar,rowSettingsBar,buttonScrollLeft,buttonScrollRight);

        //Create Grid, Load Settings
        file.createGrid();
        file.load();

        //Create Buttons
        for (int column = 0; column < getFile().getColumnCount().get(); column++) createNewColumnButton();
        for (int row = 0; row < getFile().getRowCount().get(); row++) createNewRowButton();

        //Set Setting Data
        settingToggleBold.getDisplayText().setModifier(Font.BOLD);
        settingToggleItalic.getDisplayText().setModifier(Font.ITALIC);
        cellSettingsBar.getButton().setEnabled(false);
        columnSettingsBar.getButton().setEnabled(false);
        rowSettingsBar.getButton().setEnabled(false);

        animationInjection.subscribe();

        setRowStartIndex(0); //Sets the index to the start
    }

    @Override
    public void draw() {
        drawBackground(new ColorE(150, 150, 150));

        //Draw Sheet Title
        TextUI title = new TextUI(getFile().getName(), Fonts.getDefaultFont(40), Vector.NULL.getAdded(0, 22), ColorE.BLACK).setFont("Verdana").setModifier(Font.ITALIC);
        title.drawCentered(this, getWindow().getScaledMousePos(), new Vector(getSize().getX(), 0));

        //Draw Cells & Buttons
        Vector startPos = new Vector(40, 70);
        drawCells(startPos, 1);
        drawButtons(startPos, 1);

        //Draw Add Row Button
        buttonAddRow.setEnabled(getRowEndIndex() == (getFile().getCellGrid().size() - 1));
        buttonAddRow.setPos(new Vector(getSize().getX() / 2 - buttonAddRow.getSize().getX() / 2, getFile().getCellGrid().get(getRowEndIndex()).get(0).getPos().getY() + getFile().getCellGrid().get(getRowEndIndex()).get(0).getSize().getY() + 10));

        //Draw Add Column Button
        buttonAddColumn.setPos(new Vector(getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX() + 10, getSize().getY() / 2 - buttonAddColumn.getSize().getY() / 2));
        buttonAddColumn.setEnabled(buttonAddColumn.getPos().getX() < getSize().getX());

        //Draw Scroll Right/Left Buttons
        buttonScrollRight.setPos(new Vector(getSize().getX() - buttonScrollRight.getSize().getX(), 0));
        buttonScrollRight.setEnabled(getColumnButtonList().get(getFile().getColumnCount().get() - 1).getPos().getX() + getColumnButtonList().get(getFile().getColumnCount().get() - 1).getSize().getX() + buttonAddColumn.getSize().getX() + 10 > getSize().getX());
        buttonScrollLeft.setEnabled(getColumnStartIndex() > 0);

        //TODO: When a cell is hovered, create a hover-fade over the row and column buttons

        //Draw Selected Row Rectangle
        try {
            if (selectedRowIndex.get() >= getRowStartIndex() && selectedRowIndex.get() <= getRowEndIndex())
                new RectangleUI(getRowButtonList().get(selectedRowIndex.get()).getPos(), new Vector(getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX() - getRowButtonList().get(selectedRowIndex.get()).getPos().getX(),getRowButtonList().get(selectedRowIndex.get()).getSize().getY()), true, 2, ColorE.BLUE.green(175)).draw();
        } catch (IndexOutOfBoundsException ignored) {
        }

        //Draw Selected Column Rectangle
        try {
            if (selectedColumnIndex.get() >= getColumnStartIndex() && selectedColumnIndex.get() <= getColumnEndIndex())
                new RectangleUI(getColumnButtonList().get(selectedColumnIndex.get()).getPos(), new Vector(getColumnButtonList().get(selectedColumnIndex.get()).getSize().getX(), getRowButtonList().get(getRowEndIndex()).getPos().getY() - getColumnButtonList().get(selectedColumnIndex.get()).getPos().getY() + getRowButtonList().get(getRowEndIndex()).getSize().getY()), true, 2, ColorE.BLUE.green(175)).draw();
        } catch (IndexOutOfBoundsException ignored) {

        }

        super.draw();

        //Draw Add Column Text
        if (buttonAddColumn.shouldRender())
            new TextH("Add Column", Fonts.getDefaultFont(18), buttonAddColumn.getPos(), ColorE.WHITE)
                    .drawCentered(this, getWindow().getScaledMousePos(), buttonAddColumn.getSize());

        QuickDraw.drawFPSTPS(this, new Vector(22, 2), 10, false);
    }

    @Override
    public void tick() {
        super.tick();
        try {
            //Tick Cells
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
                for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                    Cell cell = getFile().getCellGrid().get(row).get(column);
                    cell.tick(this);
                }
            }
            //Tick Column Buttons
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) getColumnButtonList().get(column).tick(this);

            //Tick Row Buttons
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) getRowButtonList().get(row).tick(this);

        } catch (ConcurrentModificationException | IndexOutOfBoundsException ignored) {
        }
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        try {
            //Key Cells
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
                for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                    Cell cell = getFile().getCellGrid().get(row).get(column);
                    cell.onKeyPress(this, key, scancode, action, mods);
                }
            }

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
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
                for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                    Cell cell = getFile().getCellGrid().get(row).get(column);
                    cell.onMouseClick(this, button, action, mods, mousePos);
                }
            }
            //Mouse Column Buttons
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) getColumnButtonList().get(column).onMouseClick(this, button, action, mods, mousePos);

            //Mouse Row Buttons
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) getRowButtonList().get(row).onMouseClick(this, button, action, mods, mousePos);

        } catch (ConcurrentModificationException | IndexOutOfBoundsException ignored) {
        }

        mouseElements(button, action, mods, mousePos);
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        setRowStartIndex(getRowStartIndex() - scroll);
    }


    public void drawCells(Vector gridPos, int separation) throws ConcurrentModificationException {
        int x = (int) gridPos.getX();
        int y = (int) gridPos.getY();

        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
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

        for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
            ButtonUI button = getColumnButtonList().get(column);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(getFile().getColumnWidthSettings().get(column).get(), size));
            button.draw(this);
            x += getFile().getColumnWidthSettings().get(column).get() + separation;
        }
        x = (int) gridPos.getX() - size - separation;
        y = (int) gridPos.getY();
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            ButtonUI button = getRowButtonList().get(row);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(size, getFile().getRowHeightSettings().get(row).get()));
            button.draw(this);
            y += getFile().getRowHeightSettings().get(row).get() + separation;
        }
    }

    private void mouseElements(int button, int action, int mods, Vector mousePos) {
        //Close Column & Row Settings if deleted
        if (columnSettingsBar.getTitle().equals(getColumnTitle(getColumnButtonList().size()))) columnSettingsBar.setOpen(false);
        if (rowSettingsBar.getTitle().equals(getRowTitle(getRowButtonList().size()))) rowSettingsBar.setOpen(false);

        //Close Column Settings Bar When Click Outside
        boolean isColumnOutOfBounds  = true;
        for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
            ButtonUI columnButton = getColumnButtonList().get(column);
            if (!columnButton.isMouseOver() && columnSettingsBar.getTitle().equals(getColumnTitle(column)) && getWindow().getScaledMousePos().getY() > columnSettingsBar.getWidth()) {
                columnSettingsBar.setOpen(false);
                selectedColumnIndex.set(-1);
            }
            if (columnSettingsBar.getTitle().equals(getColumnTitle(column))) isColumnOutOfBounds = false;
        }
        if (isColumnOutOfBounds) {
            columnSettingsBar.setOpen(false);
            selectedColumnIndex.set(-1);
        }


        //Close Row Settings Bar When Click Outside
        boolean isRowOutOfBounds  = true;
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            ButtonUI rowButton = getRowButtonList().get(row);
            if (!rowButton.isMouseOver() && rowSettingsBar.getTitle().equals(getRowTitle(row)) && getWindow().getScaledMousePos().getY() > rowSettingsBar.getWidth()) {
                rowSettingsBar.setOpen(false);
                selectedRowIndex.set(-1);
            }
            if (rowSettingsBar.getTitle().equals(getRowTitle(row))) isRowOutOfBounds = false;
        }
        if (isRowOutOfBounds) {
            rowSettingsBar.setOpen(false);
            selectedRowIndex.set(-1);
        }


        //Set Cell Selected, Open Cell Settings Bar
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);

                if (cell.isMouseOver()) {
                    if (button == Mouse.BUTTON_RIGHT.getId() && action == Mouse.ACTION_RELEASE) {
                        cell.setSelected(!cell.isSelected());
                        cell.setTyping(false);
                        setCellSettingContainers(cell);

                        cellSettingsBar.setOpen(!cellSettingsBar.getTitle().equals(getCellTitle(column,row)) || !cellSettingsBar.isOpen());
                        cellSettingsBar.setTitle(getCellTitle(column,row));
                    }
                } else if (getWindow().getScaledMousePos().getY() > cellSettingsBar.getWidth()) {
                    cell.setSelected(false);
                    if (cellSettingsBar.getTitle().equals(getCellTitle(column,row))) cellSettingsBar.setOpen(false);
                }
                if (cell.isTyping()) cellSettingsBar.setOpen(false);
            }
        }

        //TODO: Create row/column selection using a selection container and an outlined rectangle
    }

    private void createNewColumnButton() {
        int index = getColumnButtonList().size();
        ButtonUI button;
        getColumnButtonList().add(button = new ButtonUI(String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase(), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL,() -> {
            //Set Containers
            settingSliderColumnWidth.setContainer(getFile().getColumnWidthSettings().get(index));
            deleteColumnButton.setAction(() -> {
                getFile().deleteColumn(index);
                getColumnButtonList().clear();
                for (int column = 0; column < getFile().getColumnCount().get(); column++) createNewColumnButton();
            });

            //Set Column Selected
            selectedColumnIndex.set(selectedColumnIndex.get() == index ? -1 : index);

            //Set Open, Set Title
            columnSettingsBar.setOpen(!columnSettingsBar.getTitle().equals(getColumnTitle(index)) || !columnSettingsBar.isOpen());
            columnSettingsBar.setTitle(getColumnTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }

    private void createNewRowButton() {
        int index = getRowButtonList().size();
        ButtonUI button;
        getRowButtonList().add(button = new ButtonUI(String.valueOf(index), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL,() -> {
            //Set Containers
            settingSliderRowHeight.setContainer(getFile().getRowHeightSettings().get(index));
            deleteRowButton.setAction(() -> {
                getFile().deleteRow(index);
                getRowButtonList().clear();
                for (int row = 0; row < getFile().getRowCount().get(); row++) createNewRowButton();
            });

            //Set Row Selected
            selectedRowIndex.set(selectedRowIndex.get() == index ? -1 : index);

            //Set Open, Set Title
            rowSettingsBar.setOpen(!rowSettingsBar.getTitle().equals(getRowTitle(index)) || !rowSettingsBar.isOpen());
            rowSettingsBar.setTitle(getRowTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }


    private void setCellSettingContainers(Cell cell) {
        settingToggleBold.setContainer(cell.isTextBold());
        settingToggleItalic.setContainer(cell.isTextItalic());
        settingSliderOutlineWidth.setContainer(cell.getOutlineWidth());
    }

    private void setRowStartIndex(int val) {
        this.rowStartIndex = Math.min(val, getFile().getCellGrid().size() - 1);
    }
    private void setColumnStartIndex(int val) {
        this.columnStartIndex = Math.min(val, getFile().getCellGrid().get(0).size() - 1);
    }


    private int getRowStartIndex() {
        return Math.max(this.rowStartIndex, 0);
    }
    private int getRowEndIndex() {
        int index = getRowStartIndex() + (int) getSize().getY() / 22;
        return Math.min(index, getFile().getCellGrid().size() - 1);
    }

    private int getColumnStartIndex() {
        return Math.max(this.columnStartIndex, 0);
    }
    private int getColumnEndIndex() {
        int index = getColumnStartIndex() + (int) getSize().getX() / 100;
        return Math.min(index, getFile().getCellGrid().get(0).size() - 1);
    }

    private String getRowTitle(int row) {
        return "Row: " + row + " Settings";
    }
    private String getColumnTitle(int column) {
        return "Column: " + String.valueOf(StringUtil.getLetterFromIndex(column)).toUpperCase() + " Settings";
    }
    private String getCellTitle(int column, int row) {
        return "Cell: " + String.valueOf(StringUtil.getLetterFromIndex(column)).toUpperCase() + row + " Settings";
    }


    public ArrayList<ButtonUI> getColumnButtonList() {
        return columnButtonList;
    }
    public ArrayList<ButtonUI> getRowButtonList() {
        return rowButtonList;
    }

    public FileCSV getFile() {
        return file;
    }

}
