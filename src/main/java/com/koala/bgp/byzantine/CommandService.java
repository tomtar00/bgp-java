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
    private static volatile List<General> generals;
    private static volatile List<Messenger> messengers;
    private static volatile boolean isRunning;

    private static volatile int votes;
    private static volatile boolean ending = false;

    private static General king = null;

    public static void init(int num_generals, int num_traitors) {
        ending = false;
        generals = Collections.synchronizedList(new ArrayList<>());
        messengers = Collections.synchronizedList(new ArrayList<>());
        votes = 0;
        Messenger.setMessCount(0);

        Stack<Boolean> randomStack = Mathf.randomBoolStack(0, num_generals, num_traitors);

        int paddingLeft = 200;
        int paddingRight = 50;
        int paddingTop = 80;
        int paddingBottom = 80;
        ArrayList<Vector2> gen_coords = new ArrayList<Vector2>();

        // create generals and set their positions
        try {
            Vector2 coords;
            if (SetupPanel.getGenSystem() == "Circle") {

                for (int i = 0; i < num_generals; i++) {
                    double angle = i * 2 * Math.PI / num_generals;
                    int view_x = (BattlePanel.PANEL_SIZE_X - (paddingLeft + paddingRight));
                    int view_y = (ByzantineMain.SCREEN_SIZE_Y - (paddingTop + paddingBottom));
                    int GENERAL_SPAWN_RADIUS = view_x > view_y ? view_y / 2 : view_x / 2;
                    int x = (int) (Math.sin(angle) * GENERAL_SPAWN_RADIUS);
                    int y = (int) (Math.cos(angle) * GENERAL_SPAWN_RADIUS);
                    int offsetX = paddingLeft + view_x / 2;
                    int offsetY = paddingTop + view_y / 2;
                    coords = new Vector2(x + offsetX, y + offsetY);

                    gen_coords.add(coords);
                }

            } else if (SetupPanel.getGenSystem() == "Random") {

                for (int i = 0; i < num_generals; i++) {
                    Random rand = new Random();
                    int x = rand.nextInt(BattlePanel.PANEL_SIZE_X - (paddingLeft + paddingRight)) + paddingLeft;
                    int y = rand.nextInt(ByzantineMain.SCREEN_SIZE_Y - (paddingTop + paddingBottom)) + paddingTop;
                    coords = new Vector2(x, y);

                    gen_coords.add(coords);
                }

            } else if (SetupPanel.getGenSystem() == "PoissonDisc") {

                int num_created_points = 0;
                double minDst = 35;
                while (num_created_points < num_generals) {
                    Random rand = new Random();
                    int x = rand.nextInt(BattlePanel.PANEL_SIZE_X - (paddingLeft + paddingRight)) + paddingLeft;
                    int y = rand.nextInt(ByzantineMain.SCREEN_SIZE_Y - (paddingTop + paddingBottom)) + paddingTop;
                    coords = new Vector2(x, y);

                    boolean too_close = false;
                    for (var point : gen_coords) {
                        double sqrDst = Math.pow((point.getX() - coords.getX()), 2) + Math.pow((point.getY() - coords.getY()), 2);
                        if (sqrDst < minDst * minDst) {
                            too_close = true;
                            break;
                        }
                    }

                    if (too_close)
                        continue;

                    gen_coords.add(coords);
                    num_created_points++;
                }

            }

            int i = 0;
            for (var point : gen_coords) {
                Boolean isTraitor = randomStack.pop();
                String entityName = isTraitor ? "Traitor " : "General ";
                CommandService.getGenerals().add(new General(entityName + i, point, isTraitor, SetupPanel.getAlgorithm()));
                i++;
            }
            
        } catch (NoSuchAlgorithmException ex) {
            SimpleLogger.logWarning("Error creating Generals!");
            ex.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
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

    public static void setKing(General _king) {
        if (king != null) {
            king.setKing(false);
        }
        king = _king;
    }
    public static General getKing() {
        return king;
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
        var mostCommon = Mathf.mostCommon(decisions);
        float mostCommountCount = 0;
        float allCount = decisions.size();
        for (var decision : decisions) {
            if (decision.equals(mostCommon.x)) {
                mostCommountCount++;
            }
        }

        return (allCount - mostCommountCount) / allCount;
    }

    public static synchronized int getCurrentRound() {
        ArrayList<Integer> rounds = new ArrayList<>();
        for (var general : generals) {
            rounds.add(general.getCurrentRound());
        }
        return Mathf.mostCommon(rounds).x;
    }
}