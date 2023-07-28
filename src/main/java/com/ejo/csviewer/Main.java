package com.ejo.csviewer;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.scenes.GridScene;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;

public class Main {

    public static Window mainWindow = new Window(
            "CSViewer",
            new Vector(100,100),
            new Vector(1600,800),
            new GridScene(new FileCSV("data/","sheetName")),
            true, 4, 30, 60
    );

    public static void main(String[] args) {
        mainWindow.setEconomic(true);
        mainWindow.run();
        mainWindow.close();
    }
}