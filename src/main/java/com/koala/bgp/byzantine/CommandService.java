package com.koala.bgp.byzantine;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import com.koala.bgp.ByzantineMain;
import com.koala.bgp.blockchain.BlockchainNode;
import com.koala.bgp.visual.SetupPanel;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.*;

public class CommandService
{
    public final static int GENERAL_SPAWN_RADIUS = 320;

    private static volatile List<General> generals;
    private static volatile List<Messenger> messengers;
    private static volatile boolean isRunning;

    private static volatile int votes;
    private static volatile boolean ending = false;

    public static void init(int num_generals, int num_traitors) {
        ending = false;
        generals = Collections.synchronizedList(new ArrayList<>());
        messengers = Collections.synchronizedList(new ArrayList<>());
        votes = 0;
        Messenger.setMessCount(0);
        Stack<Boolean> randomStack = Mathf.randomBoolStack(0, num_generals, num_traitors);

        // create generals and set their positions
        for (int i = 0; i < num_generals; i++) 
        {
            try
            {
                Vector2 coords;
                if(SetupPanel.ChooseAlgorithm == "Circle") {
                 //circle
                     double angle = i * 2 * Math.PI / num_generals;
                     int x = (int)(Math.sin(angle) * GENERAL_SPAWN_RADIUS);
                     int y = (int)(Math.cos(angle) * GENERAL_SPAWN_RADIUS);
                     int offsetX = BattlePanel.PANEL_SIZE_X / 2;
                     int offsetY = ByzantineMain.SCREEN_SIZE_Y / 2;
                     coords = new Vector2(x + offsetX, y + offsetY);
                }
                else {
                    // random
                    Random rand = new Random();
                    int paddingLeft = 200;
                    int paddingRight = 50;
                    int paddingTop = 80;
                    int paddingBottom = 80;
                    int x = rand.nextInt(BattlePanel.PANEL_SIZE_X - (paddingLeft + paddingRight)) + paddingLeft;
                    int y = rand.nextInt(ByzantineMain.SCREEN_SIZE_Y - (paddingTop + paddingBottom)) + paddingTop;
                    coords = new Vector2(x, y);
                }

                Boolean isTraitor = randomStack.pop();
                String entityName = isTraitor ? "Traitor " : "General ";
                CommandService.getGenerals().add(new General(entityName + i, coords, isTraitor));
            }
            catch (NoSuchAlgorithmException ex)
            {
                SimpleLogger.logWarning("Error creating General!");
                ex.printStackTrace();
                SimpleLogger.pressAnyKeyToContinue();
            }
        }
    }
    public static void sendOriginMessages()
    {
        setIsRunning(true);

        for (General general : CommandService.getGenerals()) {
            new Thread(() -> {
                try {
                    general.sendMyDecisionToAllGenerals(general.getDecision(), 1);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }  
            }).start();  
        }  
    }

    public static List<General> getGenerals() { return generals; }
    public static List<Messenger> getMessengers() { return messengers; }
    public static boolean isRunning() { return isRunning; }
    public static void setIsRunning(boolean running) { isRunning = running; }

    public static synchronized void voteToEndSync() {
        if (++votes >= generals.size() && !ending) {
            new Thread(() -> {
                while (getMessengers().size() > 0 || !allGeneralsAddedAllBlocks()) { 
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        SimpleLogger.pressAnyKeyToContinue();
                    }
                }
                isRunning = false;
                SimpleLogger.print("\nCommunication phase ended\n");
            }).start();
            ending = true;
        }
    }
    private static synchronized boolean allGeneralsAddedAllBlocks() {
        for (var g : getGenerals()) {
            if (g.isMiningPendingTransactions()) {
                return false;
            }
        }
        return true;
    }
    public static synchronized BlockchainNode getNode(PublicKey pKey) {
        for (BlockchainNode g : generals) {
            if (pKey.equals(g.getKeyPair().getPublic())) {
                return g;
            }
        }
        return null;
    }

    public static synchronized double getSystemEnergy() {
        List<Decision> decisions = CommandService.getGenerals().stream().filter(g -> !g.isTraitor()).map(General::getDecision).collect(Collectors.toList());
        Decision mostCommon = Mathf.mostCommon(decisions);
        float mostCommountCount = 0;
        float allCount = decisions.size();
        for (var decision : decisions) {
            if (decision.equals(mostCommon)) {
                mostCommountCount++;
            }
        }

        return (allCount - mostCommountCount) / allCount;
    }
}