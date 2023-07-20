package com.ejo.csviewer.data;

import com.ejo.csviewer.element.Cell;
import com.ejo.csviewer.util.Util;
import com.ejo.glowlib.file.CSVManager;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.Container;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.setting.SettingManager;

import java.util.ArrayList;

public class FileCSV {

    private final SettingManager settingManager;

    private final ArrayList<ArrayList<Cell>> grid = new ArrayList<>();

    private final Vector gridPos = new Vector(20,60);

    private final Container<Integer> rowCount;
    private final Container<Integer> columnCount;

    private final String name;
    private final String path;

    private final ArrayList<Setting<Integer>> columnWidthSettings;
    private final ArrayList<Setting<Integer>> rowHeightSettings;

    public FileCSV(String path, String name) {
        this.name = name;
        this.path = path;

        this.settingManager = new SettingManager(getPath(),getName() + "_settings");

        ArrayList<String[]> loadedData = CSVManager.getDataFromCSV(getPath(), getName());
        this.columnCount = new Container<>(Util.getMaxRowSize(loadedData));
        this.rowCount = new Container<>(loadedData.size());

        //TODO: add settings depending on the column count and row count
        this.columnWidthSettings = new ArrayList<>();
        this.rowHeightSettings = new ArrayList<>();
    }

    //TODO: have positions constantly set so that the widths can work properly
    public void createGrid() {
        int width = 100; //TODO: make these defaults loadable settings for each cell in a column
        int height = 20;
        int separation = 1;

        for (int row = 0; row < getRowCount().get(); row++) {
            getCellGrid().add(new ArrayList<>());
            for (int column = 0; column < getColumnCount().get(); column++) {
                Cell cell = new Cell(this,column,row,new Vector(gridPos.getX() + column*(width + separation),gridPos.getY() + row*(height + separation)), new Vector(width,height),new ColorE(200,200,200),ColorE.BLACK);
                getCellGrid().get(row).add(cell);
            }
        }
    }

    public void addRow() {
        int width = 100;
        int height = 20;
        int separation = 1;

        int rowCount = getCellGrid().size();

        ArrayList<Cell> newRow = new ArrayList<>();
        for (int column = 0; column < getColumnCount().get(); column++) {
            Cell cell = new Cell(this, column, rowCount, new Vector(gridPos.getX() + column * (width + separation), gridPos.getY() + rowCount * (height + separation)), new Vector(width, height), new ColorE(200, 200, 200), ColorE.BLACK);
            newRow.add(cell);
        }
        getCellGrid().add(newRow);

        getRowCount().set(getRowCount().get() + 1);
    }

    public void addColumn() {
        int width = 100;
        int height = 20;
        int separation = 1;

        int columnCount = getCellGrid().get(0).size();

        for(ArrayList<Cell> row : getCellGrid()) {
            Cell cell = new Cell(this, columnCount, getCellGrid().indexOf(row), gridPos.getAdded(new Vector(columnCount * (width + separation), getCellGrid().indexOf(row) * (height + separation))), new Vector(width, height), new ColorE(200, 200, 200), ColorE.BLACK);
            row.add(cell);
        }

        getColumnCount().set(getColumnCount().get() + 1);
    }

    public void deleteRow(int rowIndex) {
        getCellGrid().remove(rowIndex);
    }

    public void deleteColumn(int columnIndex) {
        for (ArrayList<Cell> row : getCellGrid()) {
            row.remove(columnIndex);
        }
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
                } catch (IndexOutOfBoundsException e) {
                    continue;
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


    public SettingManager getSettingManager() {
        return settingManager;
    }

    public ArrayList<ArrayList<Cell>> getCellGrid() {
        return grid;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

}
