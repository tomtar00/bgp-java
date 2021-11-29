package com.koala.bgp.visual;

import javax.swing.JPanel;

import java.awt.*;
import java.util.ArrayList;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.byzantine.*;

public class BattlePanel extends JPanel 
{
    public final static int PANEL_SIZE_X = 800;
    public final static int PANEL_SIZE_Y = 800;

    private Graphics2D g2D;

    public BattlePanel() 
    {
        this.setPreferredSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y));
        this.setBackground(Color.black); 
    }

    public void paint (Graphics g)
    {
        super.paint(g);
        g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw messangers
        ArrayList<Messenger> currentMessengers = new ArrayList<>(CommandService.getMessengers());
        for (Messenger messenger : currentMessengers) {
            if (messenger != null)
                messenger.draw(g2D);
        }

        // draw generals
        for (General general : CommandService.getGenerals()) {
            general.draw(g2D);
        };

        // draw fps
        g2D.setColor(Color.WHITE);
        g2D.drawString("FPS: " + String.format("%.2f", ByzantineMain.getFPS()), 5, 15);

        // draw progress bar
        int height = 20;
        float progress = (float)Messenger.getMessengersCount() / (float)ByzantineMain.getTotalNumMessengers();
        int width = (int)(PANEL_SIZE_X * progress);
        g2D.setColor(Color.GREEN);
        g2D.fillRect(0, PANEL_SIZE_Y - height, width, height);
        g2D.setColor(Color.WHITE);
        g2D.drawString((int)(progress * 100)+"%", width - 32, PANEL_SIZE_Y - height - 5);
    }
}
