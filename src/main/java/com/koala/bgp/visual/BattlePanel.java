package com.koala.bgp.visual;

import javax.swing.JPanel;

import java.awt.*;
import java.util.ArrayList;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.byzantine.*;
import com.koala.bgp.utils.Time;

public class BattlePanel extends JPanel 
{
    public final static int PANEL_SIZE_X = 800;
    private int PANEL_SIZE_Y;

    private Graphics2D g2D;
    private double t = 0.0;

    public BattlePanel(int PANEL_SIZE_Y) 
    {
        this.PANEL_SIZE_Y = PANEL_SIZE_Y;
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

        // DEBUG
        g2D.setColor(Color.WHITE);
        // fps
        g2D.drawString("FPS: " + String.format("%.2f", ByzantineMain.getFPS()), 5, 15);
        // additional info
        g2D.drawString("Threads: " + Thread.activeCount(), 5, 45);
        g2D.drawString("Active messengers: " + CommandService.getMessengers().size(), 5, 60);
        g2D.drawString("Total messengers: " + Messenger.getMessengersCount(), 5, 75);

        // draw boltzmann
        t += Time.getDeltaTime();
        double energy = CommandService.getSystemEnergy();
        double boltz = Math.exp(-energy/t);
        int boltz_perc = (int)(boltz * 100);
        g2D.setColor(Color.WHITE);
        g2D.drawString("Boltzmann: " + boltz_perc + "%", 5, 105);

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
