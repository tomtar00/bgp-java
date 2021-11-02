package com.koala.bgp.visual;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel
{
    public final static int PANEL_SIZE_X = 600;
    public final static int PANEL_SIZE_Y = 800;

    private JTextArea debugText;

    public DebugPanel() 
    {
        this.setPreferredSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y)); 

        debugText = new JTextArea();
        //debugText.setLineWrap(true);

        JScrollPane scroll = new JScrollPane (debugText, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


        scroll.setPreferredSize(new Dimension(PANEL_SIZE_X - 20, PANEL_SIZE_Y - 20));
        add(scroll);
    }

    public void concatDebugText(String text) {
        debugText.setText(debugText.getText() + text);
    }
    public void clearDebugText() {
        debugText.setText("");
    }
}
