package com.koala.bgp.visual;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.koala.bgp.byzantine.*;

public class BattlePanel extends JPanel 
{
    public final static int PANEL_SIZE_X = 800;
    public final static int PANEL_SIZE_Y = 800;
    private Battle battle;

    public BattlePanel(Battle battle) 
    {
        this.battle = battle;

        this.setPreferredSize(new Dimension(PANEL_SIZE_X, PANEL_SIZE_Y));
        this.setBackground(Color.BLACK); 
    }

    public void paint (Graphics g)
    {
        super.paint(g);
        Graphics2D g2D = (Graphics2D) g;

        for (Messenger messenger : battle.getMessengers()) {
            messenger.draw(g2D);
        }

        for (General general : battle.getGenerals()) {
            general.draw(g2D);
        }

        battle.draw(g2D);
    }
}
