package com.koala.bgp;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import com.koala.bgp.byzantine.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.BattlePanel;
import com.koala.bgp.visual.FrameMain;

public class ByzantineMain 
{
    private final static int SCREEN_SIZE_X = 800;
    private final static int SCREEN_SIZE_Y = 800;
    private final static int GENERAL_SPAWN_RADIUS = 320;

    private static int NUM_GENERALS;
    private static int NUM_TRAITORS;
    private static int ENC_LEVEL;

    private static FrameMain frame;
    private static Battle battle;

    private static float fpsCounter = 0;
    public static float fps = 0;
    private static float timeCounter = 0;

    private static Battle createBattle()
    {
        Battle battle = new Battle(NUM_GENERALS, NUM_TRAITORS);

        for (int i = 0; i < NUM_GENERALS; i++) 
        {
            try
            {
                General g = new General("General " + i, battle, ENC_LEVEL);
                
                double angle = i * 2 * Math.PI / NUM_GENERALS;
                int x = (int)(Math.sin(angle) * GENERAL_SPAWN_RADIUS);
                int y = (int)(Math.cos(angle) * GENERAL_SPAWN_RADIUS);
                int offsetX = BattlePanel.PANEL_SIZE_X / 2;
                int offsetY = BattlePanel.PANEL_SIZE_Y / 2;
                g.setCoords(new Coords(x + offsetX, y + offsetY));

                battle.getGenerals().add(g);
            }
            catch (NoSuchAlgorithmException ex)
            {
                SimpleLogger.logWarning("Error creating General! " + ex.getMessage());
            }
        }

        return battle;
    }
    private static void sendOriginMessage() throws NoSuchAlgorithmException
    {
        Message firstMessage = new Message("Attack at dawn!", Decision.ATTACK);

        battle.setOriginMessage(firstMessage);
        battle.setIsRunning(true);

        battle.getGenerals().get(0).sendMessage(firstMessage);  
    }
    private static void summarizeBattle()
    {
        boolean consensusReached = true;
        int numGeneralsOtherDecision = 0;
        for (General general : battle.getGenerals())
        {
            if (general.getDecision() != battle.getOriginMessage().getDecision())
            {
                numGeneralsOtherDecision++;
                consensusReached = false;
            }
        }
        if (consensusReached)
            SimpleLogger.logWarning("CONSENSUS REACHED :) ---- All generals agreed on the same plan");
        else
            SimpleLogger.logWarning("CONSENSUS NOT REACHED :( ---- " + numGeneralsOtherDecision + " generals decided to do something else");
        SimpleLogger.print("\n");
    }

    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);

        SimpleLogger.print("Set number of generals: ");
        NUM_GENERALS = sc.nextInt();
        SimpleLogger.print("Set number of traitors: ");
        NUM_TRAITORS = sc.nextInt();
        SimpleLogger.print("Set encryption level: ");
        ENC_LEVEL = sc.nextInt();

        battle = createBattle();
        frame = new FrameMain(SCREEN_SIZE_X, SCREEN_SIZE_Y, battle);

        try 
        {
            sendOriginMessage();          
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }    

        Time.init();
        while (battle.isRunning())
        {
            try
            {
                Time.record();

                // update messenger position
                ArrayList<Messenger> currentMessengers = new ArrayList<>(battle.getMessengers());
                for (Messenger msger : currentMessengers) {
                    msger.update();
                }

                // update and render battle
                frame.getBattlePanel().repaint();

                if (timeCounter < 1f) {
                    timeCounter += Time.getDeltaTime();
                    fpsCounter++;
                }
                else {  
                    timeCounter = 0;
                    fps = fpsCounter;
                    fpsCounter = 0;
                }

                long sleepTime = 17L - Time.getFrameTimeMillis();
                if (sleepTime < 0) {
                    sleepTime = 0;
                }
                Thread.sleep(sleepTime);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }   

        summarizeBattle(); 

        sc.close();
    }
}
