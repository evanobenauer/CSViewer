package com.ejo.csviewer.scenes;

import com.ejo.csviewer.data.FileCSV;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.setting.SettingManager;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.TextUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.scene.elements.widget.TextFieldUI;
import com.ejo.glowui.util.render.Fonts;
import com.ejo.glowui.util.render.QuickDraw;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.File;

public class FileSelectScene extends Scene {

    private final SettingManager mainSettingManager = new SettingManager("", "csviewer");

    private final Setting<String> filePath = new Setting<>(mainSettingManager, "filePath", "");
    private final Setting<String> fileName = new Setting<>(mainSettingManager, "fileName", "");

    private final TextFieldUI fieldFilePath = new TextFieldUI(Vector.NULL, new Vector(300, 20), ColorE.BLACK, filePath, "Path", false);
    private final TextFieldUI fieldFileName = new TextFieldUI(Vector.NULL, new Vector(100, 20), ColorE.BLACK, fileName, "Name", false);

    private final TextUI textWarning = new TextUI("File Not Found!", Fonts.getDefaultFont(16),Vector.NULL,new ColorE(255,50,50,255));

    private final ButtonUI buttonEnter = new ButtonUI("Launch!",Vector.NULL,new Vector(200,40), new ColorE(50,150,50,200), ButtonUI.MouseButton.LEFT,() -> {
        mainSettingManager.saveAll();
        File file = new File((filePath.get().equals("") ? "" : filePath.get() + "/") + fileName.get() + ".csv");
        if (file.exists() && !fileName.get().equals("")) {
            getWindow().setScene(new EditorScene(new FileCSV(filePath.get(), fileName.get())));
        }
        else textWarning.setRendered(true);
    });


    public FileSelectScene() {
        super("File Select");

        addElements(fieldFilePath, fieldFileName,buttonEnter,textWarning);

        textWarning.setRendered(false);

        mainSettingManager.loadAll();
    }

    @Override
    public void draw() {
        drawBackground();

        //Draw Title
        QuickDraw.drawTextCentered("CSViewer",new Font("Arial Black",Font.BOLD,80),new Vector(0, -40),getSize(),ColorE.WHITE);

        //Draw File Input
        int yOffset = 30;
        TextUI csvText = new TextUI(".csv", Fonts.getDefaultFont(16), new Vector(0,0), ColorE.WHITE);
        fieldFilePath.setPos(getSize().getMultiplied(.5).getAdded(-(fieldFilePath.getSize().getX() + fieldFileName.getSize().getX() + 15 + csvText.getWidth()) / 2, yOffset));
        fieldFileName.setPos(fieldFilePath.getPos().getAdded(fieldFilePath.getSize().getX() + 15, 0));
        QuickDraw.drawText("/", Fonts.getDefaultFont(20), fieldFileName.getPos().getAdded(-10, -2), ColorE.WHITE);
        csvText.setPos(fieldFileName.getPos().getAdded(fieldFileName.getSize().getX() + 3, 0));
        csvText.draw();

        //Draw Enter Button
        buttonEnter.setPos(getSize().getMultiplied(.5).getAdded(-buttonEnter.getSize().getX()/2,70));

        //Draw Warning Text
        textWarning.setPos(getSize().getMultiplied(.5).getAdded(textWarning.getSize().getMultiplied(-.5)).getAdded(0,130));

        super.draw();

    }

    private void drawBackground() {
        Vector pos = Vector.NULL;
        Vector size = getSize();
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(1,1,1,1);
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY());
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY());
        GL11.glColor4f(.15f, .4f,.15f,1);
        GL11.glVertex2f((float) pos.getX() + (float) size.getX(), (float) pos.getY() + (float) size.getY());
        GL11.glVertex2f((float) pos.getX(), (float) pos.getY() + (float) size.getY());

        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
    }
}
