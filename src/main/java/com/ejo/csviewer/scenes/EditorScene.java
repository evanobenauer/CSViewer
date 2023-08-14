package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.element.Cell;
import com.ejo.csviewer.element.ColorPickerUI;
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
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class EditorScene extends Scene {

    private final FileCSV file;

    private final ArrayList<ButtonUI> columnButtonList = new ArrayList<>();
    private final ArrayList<ButtonUI> rowButtonList = new ArrayList<>();

    private int selectedColumnIndex = -1;
    private int selectedRowIndex = -1;

    private int rowStartIndex = 0;
    private int columnStartIndex = 0;


    private final ColorE settingsBarColor = new ColorE(100, 100, 100);
    private final ColorE green = new ColorE(50, 150, 50, 200);

    private ToggleUI toggleBold;
    private ToggleUI toggleItalic;
    private final SliderUI<Double> sliderOutlineWidth;
    private final ColorPickerUI colorPickerText;
    private final ColorPickerUI colorPickerFill;
    private final ColorPickerUI colorPickerOutline;
    private final SideBarUI cellSettingsBar = new SideBarUI("Cell Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            toggleBold = new ToggleUI("B", new Vector(40, 10), new Vector(20, 20), green, new Container<>(false)),
            toggleItalic = new ToggleUI("I", new Vector(70, 10), new Vector(20, 20), green, new Container<>(false)),
            sliderOutlineWidth = new SliderUI<>("Outline", new Vector(100, 10), new Vector(100, 20), green, new Container<>(0d), .6d, 3d, .2d, SliderUI.Type.FLOAT, true),
            new TextUI("Text", Fonts.getDefaultFont(8), new Vector(212, 0), ColorE.WHITE),
            colorPickerText = new ColorPickerUI(new Vector(210, 10), new Container<>(ColorE.NULL)),
            new TextUI("Fill", Fonts.getDefaultFont(8), new Vector(245, 0), ColorE.WHITE),
            colorPickerFill = new ColorPickerUI(new Vector(240, 10), new Container<>(ColorE.NULL)),
            new TextUI("Outline", Fonts.getDefaultFont(8), new Vector(268, 0), ColorE.WHITE),
            colorPickerOutline = new ColorPickerUI(new Vector(270, 10), new Container<>(ColorE.NULL))
    );

    private final SliderUI<Integer> sliderColumnWidth;
    private final ColorPickerUI colorPickerTextColumn;
    private final ColorPickerUI colorPickerFillColumn;
    private final ColorPickerUI colorPickerOutlineColumn;
    private final ButtonUI buttonDeleteColumn;
    private final SideBarUI columnSettingsBar = new SideBarUI("Column Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            sliderColumnWidth = new SliderUI<>("Width", new Vector(40, 10), new Vector(200, 20), green, new Container<>(100), 20, 1000, 1, SliderUI.Type.INTEGER, true),
            buttonDeleteColumn = new ButtonUI("Delete Column", new Vector(250, 10), new Vector(100, 20), green, ButtonUI.MouseButton.LEFT, null),
            new TextUI("Text", Fonts.getDefaultFont(8), new Vector(212 + 40, 0), ColorE.WHITE),
            colorPickerTextColumn = new ColorPickerUI(new Vector(210 + 40, 10), new Container<>(ColorE.BLACK)),
            new TextUI("Fill", Fonts.getDefaultFont(8), new Vector(245 + 40, 0), ColorE.WHITE),
            colorPickerFillColumn = new ColorPickerUI(new Vector(240 + 40, 10), new Container<>(new ColorE(200,200,200))),
            new TextUI("Outline", Fonts.getDefaultFont(8), new Vector(268 + 40, 0), ColorE.WHITE),
            colorPickerOutlineColumn = new ColorPickerUI(new Vector(270 + 40, 10), new Container<>(ColorE.BLACK))
    );

    private final SliderUI<Integer> settingSliderRowHeight;
    private final ColorPickerUI colorPickerTextRow;
    private final ColorPickerUI colorPickerFillRow;
    private final ColorPickerUI colorPickerOutlineRow;
    private final ButtonUI buttonDeleteRow;
    private final SideBarUI rowSettingsBar = new SideBarUI("Row Settings", SideBarUI.Type.TOP, 40, false, settingsBarColor,
            settingSliderRowHeight = new SliderUI<>("Height", new Vector(40, 10), new Vector(200, 20), green, new Container<>(20), 20, 500, 1, SliderUI.Type.INTEGER, true),
            buttonDeleteRow = new ButtonUI("Delete Row", new Vector(250, 10), new Vector(100, 20), green, ButtonUI.MouseButton.LEFT, null),
            new TextUI("Text", Fonts.getDefaultFont(8), new Vector(212 + 40, 0), ColorE.WHITE),
            colorPickerTextRow = new ColorPickerUI(new Vector(210 + 40, 10), new Container<>(ColorE.BLACK)),
            new TextUI("Fill", Fonts.getDefaultFont(8), new Vector(245 + 40, 0), ColorE.WHITE),
            colorPickerFillRow = new ColorPickerUI(new Vector(240 + 40, 10), new Container<>(new ColorE(200,200,200))),
            new TextUI("Outline", Fonts.getDefaultFont(8), new Vector(268 + 40, 0), ColorE.WHITE),
            colorPickerOutlineRow = new ColorPickerUI(new Vector(270 + 40, 10), new Container<>(ColorE.BLACK))
    );

    @SuppressWarnings("ALL")
    private final ButtonUI buttonSave = new ButtonUI("Save", new Vector(20, 50), new Vector(20, 20), new ColorE(50, 150, 50, 200), ButtonUI.MouseButton.LEFT, () -> getFile().save());

    private final ButtonUI buttonAddRow = new ButtonUI("Add Row", new Vector(30, 10), new Vector(200, 20), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        getFile().addRow();
        createNewRowButton();
    });

    private final ButtonUI buttonAddColumn = new ButtonUI(new Vector(50, 10), new Vector(20, 200), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        getFile().addColumn();
        createNewColumnButton();
    });

    private final ButtonUI buttonScrollLeft = new ButtonUI("<", Vector.NULL, new Vector(20, 40), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        setColumnStartIndex(Math.max(getColumnStartIndex() - 1, 0));
    });

    private final ButtonUI buttonScrollRight = new ButtonUI(">", Vector.NULL, new Vector(20, 40), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        setColumnStartIndex(Math.min(getColumnStartIndex() + 1, getFile().getColumnCount().get() - 1));
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
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++)
                getColumnButtonList().get(column).hoverAnimation.run();

        } catch (IndexOutOfBoundsException ignored) {
        }
    });


    public EditorScene(FileCSV file) {
        super(file.getName() + " Scene");
        this.file = file;

        addElements(buttonSave, buttonAddRow, buttonAddColumn, cellSettingsBar, columnSettingsBar, rowSettingsBar, buttonScrollLeft, buttonScrollRight);

        //Create Grid, Load Settings
        file.createGrid();
        file.load();

        //Create Buttons
        for (int column = 0; column < getFile().getColumnCount().get(); column++) createNewColumnButton();
        for (int row = 0; row < getFile().getRowCount().get(); row++) createNewRowButton();

        //Set Setting Data
        toggleBold.getDisplayText().setModifier(Font.BOLD);
        toggleItalic.getDisplayText().setModifier(Font.ITALIC);
        cellSettingsBar.getButton().setEnabled(false);
        columnSettingsBar.getButton().setEnabled(false);
        rowSettingsBar.getButton().setEnabled(false);

        animationInjection.subscribe();

        setRowStartIndex(0); //Sets the index to the start
    }

    @Override
    public void draw() {
        drawBackgroundFade();

        //Draw Sheet Title
        TextUI title = new TextUI(getFile().getName(), new Font("Arial Black", Font.ITALIC, 40), Vector.NULL.getAdded(0, 22), ColorE.BLACK);
        title.drawCentered(this, getWindow().getScaledMousePos(), new Vector(getSize().getX(), 0));

        //Draw Cells & Buttons
        Vector startPos = new Vector(40, 70);
        drawButtons(startPos, 1);
        drawCells(startPos, 1);

        //Draw Add Row Button
        buttonAddRow.setEnabled(getRowEndIndex() == (getFile().getCellGrid().size() - 1));
        buttonAddRow.setPos(new Vector(getSize().getX() / 2 - buttonAddRow.getSize().getX() / 2, getFile().getCellGrid().get(getRowEndIndex()).get(0).getPos().getY() + getFile().getCellGrid().get(getRowEndIndex()).get(0).getSize().getY() + 10));

        //Draw Add Column Button
        buttonAddColumn.setPos(new Vector(getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX() + 10, getSize().getY() / 2 - buttonAddColumn.getSize().getY() / 2));
        buttonAddColumn.setEnabled(buttonAddColumn.getPos().getX() < getSize().getX());

        //Draw Scroll Right/Left Buttons
        buttonScrollRight.setPos(new Vector(getSize().getX() - buttonScrollRight.getSize().getX(), 0));
        buttonScrollRight.setEnabled(getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX() + buttonAddColumn.getSize().getX() + 10 > getSize().getX());
        buttonScrollLeft.setEnabled(getColumnStartIndex() > 0);

        //Draw Delete Row Button
        buttonDeleteRow.setPos(new Vector(getSize().getX() - buttonDeleteRow.getSize().getX() - 30, buttonDeleteRow.getPos().getY()));

        //Draw Delete Column Button
        buttonDeleteColumn.setPos(new Vector(getSize().getX() - buttonDeleteColumn.getSize().getX() - 30,buttonDeleteColumn.getPos().getY()));

        //Draw Column/Row Selection Rectangles
        try {
            if (selectedColumnIndex >= getColumnStartIndex() && selectedColumnIndex <= getColumnEndIndex())
                new RectangleUI(getColumnButtonList().get(selectedColumnIndex).getPos(), new Vector(getColumnButtonList().get(selectedColumnIndex).getSize().getX(), getRowButtonList().get(getRowEndIndex()).getPos().getY() - getColumnButtonList().get(selectedColumnIndex).getPos().getY() + getRowButtonList().get(getRowEndIndex()).getSize().getY()), true, 2, ColorE.BLUE.green(175)).draw();
            if (selectedRowIndex >= getRowStartIndex() && selectedRowIndex <= getRowEndIndex())
                new RectangleUI(getRowButtonList().get(selectedRowIndex).getPos(), new Vector(getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX() - getRowButtonList().get(selectedRowIndex).getPos().getX(), getRowButtonList().get(selectedRowIndex).getSize().getY()), true, 2, ColorE.BLUE.green(175)).draw();
        } catch (IndexOutOfBoundsException ignored) {
        }

        //Draw Button Hover Fade for Columns and Rows when a cell is hovered
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);
                if (cell.isMouseOver()) {
                    QuickDraw.drawRect(getColumnButtonList().get(column).getPos(),getColumnButtonList().get(column).getSize(),ColorE.WHITE.alpha(50));
                    QuickDraw.drawRect(getRowButtonList().get(row).getPos(),getRowButtonList().get(row).getSize(),ColorE.WHITE.alpha(50));
                }
            }
        }

        super.draw();

        //Draw Add Column Text
        if (buttonAddColumn.shouldRender())
            new TextH("Add Column", Fonts.getDefaultFont(18), buttonAddColumn.getPos(), ColorE.WHITE)
                    .drawCentered(this, getWindow().getScaledMousePos(), buttonAddColumn.getSize());

        QuickDraw.drawFPSTPS(this, new Vector(2, getSize().getY() - 28), 10, false);
    }

    private void drawBackgroundFade() {
        Vector pos = Vector.NULL;
        Vector size = getSize();
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(.6f, .6f, .6f, 1);
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY());
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY());
        GL11.glColor4f(.3f, .4f, .3f, 1);
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY() + (float) size.getY());
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY() + (float) size.getY());

        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
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
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++)
                getColumnButtonList().get(column).tick(this);

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
            if (key == Key.KEY_RIGHT.getId()) setColumnStartIndex(getColumnStartIndex() + 1);
            if (key == Key.KEY_LEFT.getId()) setColumnStartIndex(getColumnStartIndex() - 1);

            if (key == Key.KEY_EQUALS.getId()) getWindow().setUIScale(getWindow().getUIScale() + .05);
            if (key == Key.KEY_MINUS.getId()) getWindow().setUIScale(Math.max(.05,getWindow().getUIScale() - .05));
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
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++)
                getColumnButtonList().get(column).onMouseClick(this, button, action, mods, mousePos);

            //Mouse Row Buttons
            for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++)
                getRowButtonList().get(row).onMouseClick(this, button, action, mods, mousePos);

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
                cell.setSize(new Vector(getFile().getColumnWidthSettingsList().get(column).get(), getFile().getRowHeightSettingsList().get(row).get()));
                cell.draw(this);
                x += getFile().getColumnWidthSettingsList().get(column).get() + separation;
            }
            x = (int) gridPos.getX();
            y += getFile().getRowHeightSettingsList().get(row).get() + separation;
        }
    }

    public void drawButtons(Vector gridPos, int separation) throws ConcurrentModificationException {
        int size = 20;
        int x = (int) gridPos.getX();
        int y = (int) gridPos.getY() - size - separation;

        for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
            ButtonUI button = getColumnButtonList().get(column);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(getFile().getColumnWidthSettingsList().get(column).get(), size));
            button.draw(this);
            x += getFile().getColumnWidthSettingsList().get(column).get() + separation;
        }
        x = (int) gridPos.getX() - size - separation;
        y = (int) gridPos.getY();
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            ButtonUI button = getRowButtonList().get(row);
            button.setPos(new Vector(x, y));
            button.setSize(new Vector(size, getFile().getRowHeightSettingsList().get(row).get()));
            button.draw(this);
            y += getFile().getRowHeightSettingsList().get(row).get() + separation;
        }
    }

    //TODO: Add JavaDOC Comments
    private void mouseElements(int button, int action, int mods, Vector mousePos) {
        updateCloseSettingsBar();

        setColumnColor();
        setRowColor();

        updateCellSelection(button,action);
    }

    private void updateCloseSettingsBar() {
        //Close Column & Row Settings if deleted
        if (columnSettingsBar.getTitle().equals(getColumnTitle(getColumnButtonList().size()))) columnSettingsBar.setOpen(false);
        if (rowSettingsBar.getTitle().equals(getRowTitle(getRowButtonList().size()))) rowSettingsBar.setOpen(false);

        //Close Column Settings Bar When Click Outside
        boolean isColumnOutOfBounds = true;
        for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
            ButtonUI columnButton = getColumnButtonList().get(column);
            if (!columnButton.isMouseOver() && columnSettingsBar.getTitle().equals(getColumnTitle(column)) && getWindow().getScaledMousePos().getY() > columnSettingsBar.getWidth()) {
                columnSettingsBar.setOpen(false);
                selectedColumnIndex = -1;
            }
            if (columnSettingsBar.getTitle().equals(getColumnTitle(column))) isColumnOutOfBounds = false;
        }
        if (isColumnOutOfBounds) {
            columnSettingsBar.setOpen(false);
            selectedColumnIndex = -1;
        }

        //Close Row Settings Bar When Click Outside
        boolean isRowOutOfBounds = true;
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            ButtonUI rowButton = getRowButtonList().get(row);
            if (!rowButton.isMouseOver() && rowSettingsBar.getTitle().equals(getRowTitle(row)) && getWindow().getScaledMousePos().getY() > rowSettingsBar.getWidth()) {
                rowSettingsBar.setOpen(false);
                selectedRowIndex = -1;
            }
            if (rowSettingsBar.getTitle().equals(getRowTitle(row))) isRowOutOfBounds = false;
        }
        if (isRowOutOfBounds) {
            rowSettingsBar.setOpen(false);
            selectedRowIndex = -1;
        }
    }

    private void setColumnColor() {
        if (colorPickerTextColumn.isMouseOver()) {
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(selectedColumnIndex);
                cell.setTextColor(colorPickerTextColumn.getContainer().get());
            }
        }
        if (colorPickerFillColumn.isMouseOver()) {
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(selectedColumnIndex);
                cell.setFillColor(colorPickerFillColumn.getContainer().get());
            }
        }
        if (colorPickerOutlineColumn.isMouseOver()) {
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(selectedColumnIndex);
                cell.setOutlineColor(colorPickerOutlineColumn.getContainer().get());
            }
        }
    }

    private void setRowColor() {
        if (colorPickerTextRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(selectedRowIndex)) cell.setTextColor(colorPickerTextRow.getContainer().get());

        if (colorPickerFillRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(selectedRowIndex)) cell.setFillColor(colorPickerFillRow.getContainer().get());

        if (colorPickerOutlineRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(selectedRowIndex)) cell.setOutlineColor(colorPickerOutlineRow.getContainer().get());
    }

    private void updateCellSelection(int button, int action) {
        //Set Cell Selected, Open Cell Settings Bar
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);

                if (cell.isMouseOver()) {
                    if (button == Mouse.BUTTON_RIGHT.getId() && action == Mouse.ACTION_RELEASE) {
                        cell.setSelected(!cell.isSelected());
                        cell.setTyping(false);
                        setCellSettingContainers(cell);

                        cellSettingsBar.setOpen(!cellSettingsBar.getTitle().equals(getCellTitle(column, row)) || !cellSettingsBar.isOpen());
                        cellSettingsBar.setTitle(getCellTitle(column, row));
                    }
                } else if (getWindow().getScaledMousePos().getY() > cellSettingsBar.getWidth()) {
                    cell.setSelected(false);
                    if (cellSettingsBar.getTitle().equals(getCellTitle(column, row))) cellSettingsBar.setOpen(false);
                }
                if (cell.isTyping()) cellSettingsBar.setOpen(false);
            }
        }
    }


    private void createNewColumnButton() {
        int index = getColumnButtonList().size();
        ButtonUI button;
        getColumnButtonList().add(button = new ButtonUI(String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase(), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL, () -> {
            //Set Containers
            sliderColumnWidth.setContainer(getFile().getColumnWidthSettingsList().get(index));
            buttonDeleteColumn.setAction(() -> {
                getFile().deleteColumn(index);
                getColumnButtonList().clear(); //TODO: There is probably a more efficient way to do this for the column buttons
                for (int column = 0; column < getFile().getColumnCount().get(); column++) createNewColumnButton();
            });

            //Set Column Selected
            selectedColumnIndex = (selectedColumnIndex == index) ? -1 : index;

            //Set Open, Set Title
            columnSettingsBar.setOpen(!columnSettingsBar.getTitle().equals(getColumnTitle(index)) || !columnSettingsBar.isOpen());
            columnSettingsBar.setTitle(getColumnTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }

    private void createNewRowButton() {
        int index = getRowButtonList().size();
        ButtonUI button;
        getRowButtonList().add(button = new ButtonUI(String.valueOf(index), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL, () -> {
            //Set Containers
            settingSliderRowHeight.setContainer(getFile().getRowHeightSettingsList().get(index));
            buttonDeleteRow.setAction(() -> {
                getFile().deleteRow(index);
                getRowButtonList().clear();
                for (int row = 0; row < getFile().getRowCount().get(); row++) createNewRowButton();
            });

            //Set Row Selected
            selectedRowIndex = (selectedRowIndex == index) ? -1 : index;

            //Set Open, Set Title
            rowSettingsBar.setOpen(!rowSettingsBar.getTitle().equals(getRowTitle(index)) || !rowSettingsBar.isOpen());
            rowSettingsBar.setTitle(getRowTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }


    private void setCellSettingContainers(Cell cell) {
        toggleBold.setContainer(cell.isTextBold());
        toggleItalic.setContainer(cell.isTextItalic());
        sliderOutlineWidth.setContainer(cell.getOutlineWidth());
        colorPickerText.setContainer(cell.getTextColor());
        colorPickerOutline.setContainer(cell.getOutlineColor());
        colorPickerFill.setContainer(cell.getFillColor());
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
        int index = getRowStartIndex();
        for (int i = getRowStartIndex(); i < getRowButtonList().size(); i++) {
            ButtonUI rowButton = getRowButtonList().get(i);
            if (rowButton.getPos().getY() + rowButton.getSize().getY() >= getSize().getY()) break;
            index += 1;
        }
        return Math.min(index, getFile().getCellGrid().size() - 1);
    }

    private int getColumnStartIndex() {
        return Math.max(this.columnStartIndex, 0);
    }
    private int getColumnEndIndex() {
        int index = getColumnStartIndex();
        for (int i = getColumnStartIndex(); i < getColumnButtonList().size(); i++) {
            ButtonUI columnButton = getColumnButtonList().get(i);
            if (columnButton.getPos().getX() + columnButton.getSize().getX() >= getSize().getX()) break;
            index += 1;
        }
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