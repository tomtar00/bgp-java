package com.koala.bgp.visual;

import java.awt.Container;

import javax.swing.*;

public class FrameMain extends JFrame 
{
    private BattlePanel battlePanel;
    private SetupPanel setupPanel;

    public FrameMain(int sizeX, int sizeY) 
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(sizeX, sizeY);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setState(JFrame.NORMAL);
        this.setResizable(false);

        battlePanel = new BattlePanel(sizeY);
        setupPanel = new SetupPanel(sizeY);

        Container contentPanel = this.getContentPane();  
        GroupLayout layout = new GroupLayout(contentPanel);
        contentPanel.setLayout(layout);

        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(battlePanel)
                .addComponent(setupPanel)
        );
        layout.setVerticalGroup(  
            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)  
                .addComponent(battlePanel)
                .addComponent(setupPanel)
        );

        this.pack();
        this.setVisible(true);  
    }


    public BattlePanel getBattlePanel() {
        return this.battlePanel;
    }
    public SetupPanel getDebugPanel() {
        return this.setupPanel;
    }
}
