package com.koala.bgp.byzantine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.koala.bgp.utils.SimpleLogger;
import com.koala.bgp.visual.BattlePanel;
import com.koala.bgp.visual.Drawable;

public class Battle implements Drawable
{
    private List<General> generals;
    private List<Messenger> messengers;
    private Message originMessage;
    private boolean isRunning;

    private int votes;

    public Battle(int num_generals, int num_traitors) {
        this.generals = new ArrayList<>();
        this.messengers = new ArrayList<>();
        this.votes = 0;
    }


    public List<General> getGenerals() {
        return this.generals;
    }

    public List<Messenger> getMessengers() {
        return this.messengers;
    }

    public Message getOriginMessage() {
        return this.originMessage;
    }

    public void setOriginMessage(Message originMessage) {
        this.originMessage = originMessage;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void voteToEndSync() {
        votes++;
        if (votes >= generals.size()) {
            isRunning = false;
            getMessengers().clear();
            SimpleLogger.logInfo("All generals know what to do now");
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        
        int sizeX = 50;
        int sizeY = 50;

        g2D.setPaint(Color.GREEN);
        g2D.fillRect(BattlePanel.PANEL_SIZE_X / 2 -sizeX / 2, BattlePanel.PANEL_SIZE_Y / 2 -sizeY / 2, sizeX, sizeY);
    }

}
