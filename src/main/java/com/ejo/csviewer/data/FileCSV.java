package com.ejo.csviewer.data;

import com.ejo.csviewer.element.Cell;
import com.ejo.glowlib.file.CSVManager;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.setting.Container;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.setting.SettingManager;
import com.ejo.glowlib.util.Util;

import java.util.ArrayList;

public class FileCSV {

    private final String name;
    private final String path;

    private final SettingManager settingManager;

    private final ArrayList<ArrayList<Cell>> grid = new ArrayList<>();
    private final ArrayList<Setting<Integer>> columnWidthSettings = new ArrayList<>();
    private final ArrayList<Setting<Integer>> rowHeightSettings = new ArrayList<>();

    private final Container<Integer> rowCount;
    private final Container<Integer> columnCount;

    public FileCSV(String path, String name) {
        this.name = name;
        this.path = path;

        this.settingManager = new SettingManager(getPath(),getName() + "_settings");

        ArrayList<String[]> loadedData = CSVManager.getDataFromCSV(getPath(), getName());
        this.columnCount = new Container<>(Util.getMaxRowSize(loadedData.toArray(new Object[0][0])));
        this.rowCount = new Container<>(loadedData.size());

        for (int column = 0; column < Util.getMaxRowSize(loadedData.toArray(new Object[0][0])); column++)
            this.columnWidthSettings.add(new Setting<>(getSettingManager(), getColumnWidthSettingName(column), 100));

        for (int row = 0; row < loadedData.size(); row++)
            this.rowHeightSettings.add(new Setting<>(getSettingManager(), getRowHeightSettingName(row), 20));
    }

    //TODO; The larger the file, the more memory it takes up, the more work Garbage Collection must do, and the worse the performance
    // The memory issue is due to the LARGE amount of cells being created HERE IN THIS METHOD, not the size of the file.
    // In the future, try and update the file every scroll and make new cells using the file
    // For the issue with saving, just save the contents of the cell EVERY time it is edited so you don't have to save all at once, which would not
    // work if you only have so many cells created at a time
    public void createGrid() {
        for (int row = 0; row < getRowCount().get(); row++) {
            getCellGrid().add(new ArrayList<>());
            for (int column = 0; column < getColumnCount().get(); column++) {
                Cell cell = new Cell(this,column,row,Vector.NULL,Vector.NULL,ColorE.BLACK,new ColorE(200,200,200),ColorE.BLACK);
                getCellGrid().get(row).add(cell);
            }
        }
    }

    public void addRow() {
        int rowCount = getCellGrid().size();

        ArrayList<Cell> newRow = new ArrayList<>();
        for (int column = 0; column < getColumnCount().get(); column++) {
            Cell cell = new Cell(this, column, rowCount, Vector.NULL, Vector.NULL ,ColorE.BLACK, new ColorE(200, 200, 200), ColorE.BLACK);
            newRow.add(cell);
        }
        getCellGrid().add(newRow);
        getRowHeightSettingsList().add(new Setting<>(getSettingManager(), getRowHeightSettingName(rowCount), 20));

        getRowCount().set(getRowCount().get() + 1);
    }

    public void addColumn() {
        int columnCount = getCellGrid().get(0).size();

        for (int row = 0; row < getCellGrid().size(); row++) {
            Cell cell = new Cell(this, columnCount, row, Vector.NULL, Vector.NULL, ColorE.BLACK,new ColorE(200, 200, 200), ColorE.BLACK);
            getCellGrid().get(row).add(cell);
            getColumnWidthSettingsList().add(new Setting<>(getSettingManager(), getColumnWidthSettingName(columnCount), 100));
        }

        getColumnCount().set(getColumnCount().get() + 1);
    }

    public void deleteRow(int rowIndex) {
        getCellGrid().remove(rowIndex);
        getRowCount().set(getRowCount().get() - 1);
        //TODO: Do Setting Transfer (Shift Up)
    }

    public void deleteColumn(int columnIndex) {
        for (ArrayList<Cell> row : getCellGrid()) row.remove(columnIndex);
        getColumnCount().set(getColumnCount().get() - 1);
        //TODO; Do Setting Transfer (Shift Left)
    }

    public void save() {
        ArrayList<String[]> saveList = new ArrayList<>();
        for (ArrayList<Cell> row : getCellGrid()) {
            ArrayList<String> rowValues = new ArrayList<>();
            for (Cell cell : row) {
                rowValues.add(cell.getContainer().get());
            }
            saveList.add(rowValues.toArray(new String[0]));
        }
        CSVManager.saveAsCSV(saveList, getPath(), getName());
        getSettingManager().saveAll();
    }

    public void load() {
        getSettingManager().loadAll();
        ArrayList<String[]> loadedData = CSVManager.getDataFromCSV(getPath(), getName());
        for (int i = 0; i < loadedData.size(); i++) {
            for (int j = 0; j < loadedData.get(i).length; j++) {
                try {
                    getCellGrid().get(i).get(j).getContainer().set(loadedData.get(i)[j]);
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    public Container<Integer> getRowCount() {
        return rowCount;
    }

    public Container<Integer> getColumnCount() {
        return columnCount;
    }

    private String getColumnWidthSettingName(int index) {
        return "column" + index + "_" + "width";
    }

    private String getRowHeightSettingName(int index) {
        return "row" + index + "_" + "height";
    }


    public ArrayList<Setting<Integer>> getColumnWidthSettingsList() {
        return columnWidthSettings;
    }

    public ArrayList<Setting<Integer>> getRowHeightSettingsList() {
        return rowHeightSettings;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }


    public ArrayList<ArrayList<Cell>> getCellGrid() {
        return grid;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

}
