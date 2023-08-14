package com.ejo.csviewer;

import com.ejo.csviewer.scenes.FileSelectScene;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;

public class Main {

    public static Window mainWindow = new Window(
            "CSViewer",
            new Vector(100,100),
            new Vector(1000,600),
            new FileSelectScene(),
            true, 4, 20, 30
    );

    public static void main(String[] args) {
        mainWindow.setEconomic(true);
        mainWindow.run();
        mainWindow.close();
    }
}