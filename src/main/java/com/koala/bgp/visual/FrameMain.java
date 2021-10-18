package com.koala.bgp.visual;

import javax.swing.JFrame;

import com.koala.bgp.byzantine.*;

public class FrameMain extends JFrame 
{
    private BattlePanel battlePanel;

    public FrameMain(int sizeX, int sizeY, Battle battle) 
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(sizeX, sizeY);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        battlePanel = new BattlePanel(battle);
        this.add(battlePanel);
        this.pack();
    }


    public BattlePanel getBattlePanel() {
        return this.battlePanel;
    }
}
