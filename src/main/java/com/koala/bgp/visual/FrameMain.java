package com.koala.bgp.visual;

import java.awt.Container;

import javax.swing.*;

public class FrameMain extends JFrame 
{
    private BattlePanel battlePanel;
    private DebugPanel debugPanel;

    public FrameMain(int sizeX, int sizeY) 
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(sizeX, sizeY);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setState(JFrame.NORMAL);

        battlePanel = new BattlePanel();
        debugPanel = new DebugPanel();

        Container contentPanel = this.getContentPane();  
        GroupLayout layout = new GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(debugPanel)
                .addComponent(battlePanel)
        );
        layout.setVerticalGroup(  
            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)  
                .addComponent(debugPanel)  
                .addComponent(battlePanel)
        );

        this.pack();
        this.setVisible(true);  
    }


    public BattlePanel getBattlePanel() {
        return this.battlePanel;
    }
    public DebugPanel getDebugPanel() {
        return this.debugPanel;
    }
}
