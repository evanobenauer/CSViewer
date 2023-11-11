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
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.Mouse;
import com.ejo.glowui.util.render.Fonts;
import com.ejo.glowui.util.render.QuickDraw;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class EditorScene extends Scene {

    private static final ColorE BAR_COLOR = new ColorE(100, 100, 100);
    private static final ColorE CSV_GREEN = new ColorE(50, 150, 50, 200);

    private final FileCSV file;

    private final ArrayList<ButtonUI> columnButtonList = new ArrayList<>();
    private final ArrayList<ButtonUI> rowButtonList = new ArrayList<>();

    private int indexSelectedColumn = -1;
    private int indexSelectedRow = -1;

    private int indexRowStart = 0;
    private int indexColumnStart = 0;


    private ToggleUI toggleBold;
    private ToggleUI toggleItalic;
    private final SliderUI<Double> sliderOutlineWidth;
    private final ColorPickerUI colorPickerText;
    private final ColorPickerUI colorPickerFill;
    private final ColorPickerUI colorPickerOutline;
    private final SideBarUI barCellSettings = new SideBarUI("Cell Settings", SideBarUI.Type.TOP, 40, false, BAR_COLOR,
            toggleBold = new ToggleUI("B", new Vector(40, 10), new Vector(20, 20), CSV_GREEN, new Container<>(false)),
            toggleItalic = new ToggleUI("I", new Vector(70, 10), new Vector(20, 20), CSV_GREEN, new Container<>(false)),
            sliderOutlineWidth = new SliderUI<>("Outline", new Vector(100, 10), new Vector(100, 20), CSV_GREEN, new Container<>(0d), .6d, 3d, .2d, SliderUI.Type.FLOAT, true),
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
    private final SideBarUI barColumnSettings = new SideBarUI("Column Settings", SideBarUI.Type.TOP, 40, false, BAR_COLOR,
            sliderColumnWidth = new SliderUI<>("Width", new Vector(40, 10), new Vector(200, 20), CSV_GREEN, new Container<>(100), 20, 1000, 1, SliderUI.Type.INTEGER, true),
            buttonDeleteColumn = new ButtonUI("Delete Column", new Vector(250, 10), new Vector(100, 20), CSV_GREEN, ButtonUI.MouseButton.LEFT, null),
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
    private final SideBarUI barRowSettings = new SideBarUI("Row Settings", SideBarUI.Type.TOP, 40, false, BAR_COLOR,
            settingSliderRowHeight = new SliderUI<>("Height", new Vector(40, 10), new Vector(200, 20), CSV_GREEN, new Container<>(20), 20, 500, 1, SliderUI.Type.INTEGER, true),
            buttonDeleteRow = new ButtonUI("Delete Row", new Vector(250, 10), new Vector(100, 20), CSV_GREEN, ButtonUI.MouseButton.LEFT, null),
            new TextUI("Text", Fonts.getDefaultFont(8), new Vector(212 + 40, 0), ColorE.WHITE),
            colorPickerTextRow = new ColorPickerUI(new Vector(210 + 40, 10), new Container<>(ColorE.BLACK)),
            new TextUI("Fill", Fonts.getDefaultFont(8), new Vector(245 + 40, 0), ColorE.WHITE),
            colorPickerFillRow = new ColorPickerUI(new Vector(240 + 40, 10), new Container<>(new ColorE(200,200,200))),
            new TextUI("Outline", Fonts.getDefaultFont(8), new Vector(268 + 40, 0), ColorE.WHITE),
            colorPickerOutlineRow = new ColorPickerUI(new Vector(270 + 40, 10), new Container<>(ColorE.BLACK))
    );

    @SuppressWarnings("ALL")
    private final ButtonUI buttonSave = new ButtonUI("Save", new Vector(20, 50), new Vector(20, 20), new ColorE(50, 150, 50, 200), ButtonUI.MouseButton.LEFT, () -> getFile().save());

    private final ButtonUI buttonNewRow = new ButtonUI("Add Row", new Vector(30, 10), new Vector(200, 20), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        getFile().addRow();
        addButtonNewRow();
    });

    private final ButtonUI buttonNewColumn = new ButtonUI(new Vector(50, 10), new Vector(20, 200), ColorE.GRAY, ButtonUI.MouseButton.LEFT, () -> {
        getFile().addColumn();
        addButtonNewColumn();
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
        addElements(buttonSave, buttonNewRow, buttonNewColumn, barCellSettings, barColumnSettings, barRowSettings, buttonScrollLeft, buttonScrollRight);

        //Create Grid, Load Settings
        file.createGrid();
        file.load();

        //Create Buttons
        for (int column = 0; column < getFile().getColumnCount().get(); column++) addButtonNewColumn();
        for (int row = 0; row < getFile().getRowCount().get(); row++) addButtonNewRow();

        //Set Setting Data
        toggleBold.getDisplayText().setModifier(Font.BOLD);
        toggleItalic.getDisplayText().setModifier(Font.ITALIC);
        barCellSettings.getButton().setEnabled(false);
        barColumnSettings.getButton().setEnabled(false);
        barRowSettings.getButton().setEnabled(false);

        animationInjection.subscribe();

        setRowStartIndex(0); //Sets the index to the start
    }

    //TODO; add an x button similar to in GravityShapes
    @Override
    public void draw() {
        drawBackgroundGradient();

        //Draw File Name
        TextUI title = new TextUI(getFile().getName(), new Font("Arial Black", Font.ITALIC, 40), Vector.NULL.getAdded(0, 22), ColorE.BLACK);
        title.drawCentered(this, getWindow().getScaledMousePos(), new Vector(getSize().getX(), 0));

        int buttonSize = 20;
        drawButtons(getGridPos(), 1,buttonSize);
        drawCells(getGridPos(), 1);
        drawButtonHoverHighlight();
        drawSelectionRectangles(buttonSize);


        //Update Add Row Button
        buttonNewRow.setEnabled(getRowEndIndex() == (getFile().getCellGrid().size() - 1));
        if (buttonNewRow.shouldRender()) buttonNewRow.setPos(new Vector(Math.max(0,getGridPos().getY() + buttonSize + getGridWidth() / 2 - buttonNewRow.getSize().getX() / 2), getGridEndY() + 10));

        //Update Add Column Button
        buttonNewColumn.setEnabled(buttonNewColumn.getPos().getX() < getSize().getX());
        if (buttonNewColumn.shouldRender()) buttonNewColumn.setPos(new Vector(getGridEndX() + 10,getGridPos().getY() + Math.max(0,getGridHeight() / 2 - buttonNewColumn.getSize().getY() / 2)));

        //Update Scroll Right/Left Buttons
        buttonScrollLeft.setEnabled(getColumnStartIndex() > 0);
        buttonScrollRight.setEnabled(getGridEndX() + buttonNewColumn.getSize().getX() + 10 > getSize().getX());
        if (buttonScrollRight.shouldRender()) buttonScrollRight.setPos(new Vector(getSize().getX() - buttonScrollRight.getSize().getX(), 0));

        //Update Delete Row/Column Buttons
        buttonDeleteRow.setPos(new Vector(getSize().getX() - buttonDeleteRow.getSize().getX() - 30, buttonDeleteRow.getPos().getY()));
        buttonDeleteColumn.setPos(new Vector(getSize().getX() - buttonDeleteColumn.getSize().getX() - 30,buttonDeleteColumn.getPos().getY()));

        //Draw All Elements
        super.draw();

        //Draw Add Column Text
        if (buttonNewColumn.shouldRender())
            new TextH("Add Column", Fonts.getDefaultFont(18), buttonNewColumn.getPos(), ColorE.WHITE).drawCentered(this, getWindow().getScaledMousePos(), buttonNewColumn.getSize());
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

        updateCloseSettingsBar();
        updateColumnColor();
        updateRowColor();
        updateCellSelection(button,action);
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        setRowStartIndex(getRowStartIndex() - scroll);
        //TODO: MAYBE every time a scroll input occurs, you ONLY read the start-end index of the file to conserve memory
    }

    /**
     * Draws the background gradient (gray-green)
     */
    private void drawBackgroundGradient() {
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

    /**
     * Draw All cells from the cell grid in the bounds of the start and end indices
     * @param gridPos
     * @param separation
     * @throws ConcurrentModificationException
     */
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

    /**
     * Draw all buttons from the button lists in the bounds of the start and end indices
     * @param gridPos
     * @param separation
     * @param size
     * @throws ConcurrentModificationException
     */
    public void drawButtons(Vector gridPos, int separation, int size) throws ConcurrentModificationException {
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

    /**
     * Draws the hover rectangle on the column/row buttons of the respective cell when the cell is hovered
     */
    private void drawButtonHoverHighlight() {
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);
                if (cell.isMouseOver()) {
                    QuickDraw.drawRect(getColumnButtonList().get(column).getPos(),getColumnButtonList().get(column).getSize(),ColorE.WHITE.alpha(25));
                    QuickDraw.drawRect(getRowButtonList().get(row).getPos(),getRowButtonList().get(row).getSize(),ColorE.WHITE.alpha(25));
                }
            }
        }
    }

    /**
     * Draws the blue rectangles for a selected column/row when the column/row is selected by pressing the button
     * @param buttonSize
     */
    private void drawSelectionRectangles(int buttonSize) {
        try {
            if (indexSelectedColumn >= getColumnStartIndex() && indexSelectedColumn <= getColumnEndIndex())
                new RectangleUI(getColumnButtonList().get(indexSelectedColumn).getPos(), new Vector(getColumnButtonList().get(indexSelectedColumn).getSize().getX(), getGridEndY() - getGridPos().getY() + buttonSize), true, 2, ColorE.BLUE.green(175)).draw();
            if (indexSelectedRow >= getRowStartIndex() && indexSelectedRow <= getRowEndIndex())
                new RectangleUI(getRowButtonList().get(indexSelectedRow).getPos(), new Vector(getGridEndX() - getGridPos().getX() + buttonSize, getRowButtonList().get(indexSelectedRow).getSize().getY()), true, 2, ColorE.BLUE.green(175)).draw();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }


    /**
     * Updates the color containers of each cell in a column depending on the color pickers
     */
    private void updateColumnColor() {
        if (colorPickerTextColumn.isMouseOver())
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(indexSelectedColumn);
                cell.setTextColor(colorPickerTextColumn.getContainer().get());
            }
        if (colorPickerFillColumn.isMouseOver())
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(indexSelectedColumn);
                cell.setFillColor(colorPickerFillColumn.getContainer().get());
            }
        if (colorPickerOutlineColumn.isMouseOver())
            for (ArrayList<Cell> row : getFile().getCellGrid()) {
                Cell cell = row.get(indexSelectedColumn);
                cell.setOutlineColor(colorPickerOutlineColumn.getContainer().get());
            }
    }

    /**
     * Updates the color containers of each cell in a row depending on the color pickers
     */
    private void updateRowColor() {
        if (colorPickerTextRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(indexSelectedRow)) cell.setTextColor(colorPickerTextRow.getContainer().get());
        if (colorPickerFillRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(indexSelectedRow)) cell.setFillColor(colorPickerFillRow.getContainer().get());
        if (colorPickerOutlineRow.isMouseOver())
            for (Cell cell : getFile().getCellGrid().get(indexSelectedRow)) cell.setOutlineColor(colorPickerOutlineRow.getContainer().get());
    }

    /**
     * Updates whether a settings bar for the Column, Row, or Cell should be closed whenever a click occurs
     */
    private void updateCloseSettingsBar() {
        //Close Column & Row Settings if deleted
        if (barColumnSettings.getTitle().equals(getColumnTitle(getColumnButtonList().size()))) barColumnSettings.setOpen(false);
        if (barRowSettings.getTitle().equals(getRowTitle(getRowButtonList().size()))) barRowSettings.setOpen(false);

        //Close Column Settings Bar When Click Outside
        boolean isColumnOutOfBounds = true;
        for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
            ButtonUI columnButton = getColumnButtonList().get(column);
            if (!columnButton.isMouseOver() && barColumnSettings.getTitle().equals(getColumnTitle(column)) && getWindow().getScaledMousePos().getY() > barColumnSettings.getWidth()) {
                barColumnSettings.setOpen(false);
                indexSelectedColumn = -1;
            }
            if (barColumnSettings.getTitle().equals(getColumnTitle(column))) isColumnOutOfBounds = false;
        }
        if (isColumnOutOfBounds) {
            barColumnSettings.setOpen(false);
            indexSelectedColumn = -1;
        }

        //Close Row Settings Bar When Click Outside
        boolean isRowOutOfBounds = true;
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            ButtonUI rowButton = getRowButtonList().get(row);
            if (!rowButton.isMouseOver() && barRowSettings.getTitle().equals(getRowTitle(row)) && getWindow().getScaledMousePos().getY() > barRowSettings.getWidth()) {
                barRowSettings.setOpen(false);
                indexSelectedRow = -1;
            }
            if (barRowSettings.getTitle().equals(getRowTitle(row))) isRowOutOfBounds = false;
        }
        if (isRowOutOfBounds) {
            barRowSettings.setOpen(false);
            indexSelectedRow = -1;
        }
    }

    /**
     * Updates whether a cell should be selected whenever a button is clicked. This will set a selection or close a selection
     * @param button
     * @param action
     */
    private void updateCellSelection(int button, int action) {
        //Set Cell Selected, Open Cell Settings Bar
        for (int row = getRowStartIndex(); row <= getRowEndIndex(); row++) {
            for (int column = getColumnStartIndex(); column <= getColumnEndIndex(); column++) {
                Cell cell = getFile().getCellGrid().get(row).get(column);

                if (cell.isMouseOver()) {
                    if (button == Mouse.BUTTON_RIGHT.getId() && action == Mouse.ACTION_RELEASE) {
                        updateCellSettingsContainers(cell);
                        cell.setSelected(!cell.isSelected());
                        cell.setTyping(false);

                        barCellSettings.setOpen(cell.isSelected() || !barCellSettings.isOpen());
                        barCellSettings.setTitle(getCellTitle(column, row));
                    }
                } else if (getWindow().getScaledMousePos().getY() > barCellSettings.getWidth()) {
                    if (cell.isSelected()) barCellSettings.setOpen(false);
                    cell.setSelected(false);
                }
                if (cell.isTyping()) barCellSettings.setOpen(false);
            }
        }
    }

    /**
     * Sets all the necessary setting containers to the cell settings containers
     * @param cell
     */
    private void updateCellSettingsContainers(Cell cell) {
        toggleBold.setContainer(cell.isTextBold());
        toggleItalic.setContainer(cell.isTextItalic());
        sliderOutlineWidth.setContainer(cell.getOutlineWidth());
        colorPickerText.setContainer(cell.getTextColor());
        colorPickerOutline.setContainer(cell.getOutlineColor());
        colorPickerFill.setContainer(cell.getFillColor());
    }

    /**
     * Add a new column button to the column button list when the method is called
     */
    private void addButtonNewColumn() {
        int index = getColumnButtonList().size();
        ButtonUI button;
        getColumnButtonList().add(button = new ButtonUI(String.valueOf(StringUtil.getLetterFromIndex(index)).toUpperCase(), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL, () -> {

            //Set Delete Action
            buttonDeleteColumn.setAction(() -> {
                getFile().deleteColumn(index);
                getColumnButtonList().clear(); //TODO: There is probably a more efficient way to do this for the column buttons
                for (int column = 0; column < getFile().getColumnCount().get(); column++) addButtonNewColumn();
            });

            //Set Containers
            sliderColumnWidth.setContainer(getFile().getColumnWidthSettingsList().get(index));

            //Set Column Selected
            indexSelectedColumn = (indexSelectedColumn == index) ? -1 : index;

            //Set Open, Set Title
            barColumnSettings.setOpen(index == indexSelectedColumn || !barColumnSettings.isOpen());
            barColumnSettings.setTitle(getColumnTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }

    /**
     * Add a new row button to the row button list when the method is called
     */
    private void addButtonNewRow() {
        int index = getRowButtonList().size();
        ButtonUI button;
        getRowButtonList().add(button = new ButtonUI(String.valueOf(index), Vector.NULL, Vector.NULL, ColorE.GRAY, ButtonUI.MouseButton.ALL, () -> {

            //Set Delete Action
            buttonDeleteRow.setAction(() -> {
                getFile().deleteRow(index);
                getRowButtonList().clear();
                for (int row = 0; row < getFile().getRowCount().get(); row++) addButtonNewRow();
            });

            //Set Containers
            settingSliderRowHeight.setContainer(getFile().getRowHeightSettingsList().get(index));

            //Set Row Selected
            indexSelectedRow = (indexSelectedRow == index) ? -1 : index;

            //Set Open, Set Title
            barRowSettings.setOpen(index == indexSelectedRow || !barRowSettings.isOpen());
            barRowSettings.setTitle(getRowTitle(index));
        }));
        button.hoverAnimation.unsubscribe();
    }


    private void setRowStartIndex(int val) {
        this.indexRowStart = Math.min(val, getFile().getCellGrid().size() - 1);
    }
    private void setColumnStartIndex(int val) {
        this.indexColumnStart = Math.min(val, getFile().getCellGrid().get(0).size() - 1);
    }

    private int getRowStartIndex() {
        return Math.max(this.indexRowStart, 0);
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
        return Math.max(this.indexColumnStart, 0);
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

    private Vector getGridPos() {
        return new Vector(40, 70);
    }
    private double getGridEndX() {
        return getColumnButtonList().get(getColumnEndIndex()).getPos().getX() + getColumnButtonList().get(getColumnEndIndex()).getSize().getX();
    }
    private double getGridEndY() {
        return getRowButtonList().get(getRowEndIndex()).getPos().getY() + getRowButtonList().get(getRowEndIndex()).getSize().getY();
    }

    private double getGridWidth() {
        double width = 0;
        for (int i = getColumnStartIndex(); i < getColumnEndIndex(); i++) {
            ButtonUI columnButton = getColumnButtonList().get(i);
            width += columnButton.getSize().getX();
        }
        return width;
    }
    private double getGridHeight() {
        double height = 0;
        for (int i = getRowStartIndex(); i < getRowEndIndex(); i++) {
            ButtonUI rowButton = getRowButtonList().get(i);
            height += rowButton.getSize().getY();
        }
        return height;
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