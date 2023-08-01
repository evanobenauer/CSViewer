package com.ejo.csviewer;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.csviewer.scenes.EditorScene;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;

public class Main {

    public static Window mainWindow = new Window(
            "CSViewer",
            new Vector(100,100),
            new Vector(1600,800),
            new EditorScene(new FileCSV("data/","sheetName")),
            true, 4, 20, 30
    );

    public static void main(String[] args) {
        mainWindow.setEconomic(true);
        mainWindow.run();
        mainWindow.close();
    }
}