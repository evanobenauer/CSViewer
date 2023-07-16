package com.ejo.csviewer;

import com.ejo.csviewer.scenes.TitleScene;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;

public class Main {

    public static Window mainWindow = new Window(
            "CSViewer",
            new Vector(100,100),
            new Vector(800,600),
            new TitleScene(),
            true, 4, 60, 60
    );

    public static void main(String[] args) {
        mainWindow.run();
        mainWindow.close();
    }
}