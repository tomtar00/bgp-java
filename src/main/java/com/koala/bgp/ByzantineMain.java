package com.koala.bgp;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;

import com.koala.bgp.byzantine.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.BattlePanel;
import com.koala.bgp.visual.FrameMain;

public class ByzantineMain 
{
    public final static int SCREEN_SIZE_X = 1400;
    public final static int SCREEN_SIZE_Y = 800;
    public final static int GENERAL_SPAWN_RADIUS = 320;
    public final static float TARGET_FPS = 30f;

    public final static int MIN_GENERALS = 3;
    public final static int MAX_GENERALS = 150;
    public final static int MIN_ENCLEVEL = 1;
    public final static int MAX_ENCLEVEL = 5;

    private static int NUM_GENERALS;
    private static int NUM_TRAITORS;
    private static int ENC_LEVEL;

    private static FrameMain frame;

    private static double fps = 0;

    private static void initCommunication()
    {
        CommandService.init(NUM_GENERALS, NUM_TRAITORS);
        Stack<Boolean> randomStack = Mathf.randomBoolStack(0, NUM_GENERALS, NUM_TRAITORS);

        // create generals and set their positions
        for (int i = 0; i < NUM_GENERALS; i++) 
        {
            try
            { 
                // circle
                //double angle = i * 2 * Math.PI / NUM_GENERALS;
                //int x = (int)(Math.sin(angle) * GENERAL_SPAWN_RADIUS);
                //int y = (int)(Math.cos(angle) * GENERAL_SPAWN_RADIUS);
                //int offsetX = BattlePanel.PANEL_SIZE_X / 2;
                //int offsetY = BattlePanel.PANEL_SIZE_Y / 2;
                //Vector2 coords = new Vector2(x + offsetX, y + offsetY);

                // random
                Random rand = new Random();
                int edgeBoundary = 100;
                int x = rand.nextInt(BattlePanel.PANEL_SIZE_X - 2 * edgeBoundary) + edgeBoundary;
                int y = rand.nextInt(BattlePanel.PANEL_SIZE_Y - 2 * edgeBoundary) + edgeBoundary;
                Vector2 coords = new Vector2(x, y);

                Boolean isTraitor = randomStack.pop();
                String entityName = isTraitor ? "Traitor " : "General ";
                CommandService.getGenerals().add(new General(entityName + i, coords, isTraitor));
            }
            catch (NoSuchAlgorithmException ex)
            {
                SimpleLogger.logWarning("Error creating General! ");
                ex.printStackTrace();
                SimpleLogger.pressAnyKeyToContinue();
            }
        }
    }
    private static void sendOriginMessages()
    {
        CommandService.setIsRunning(true);

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
    private static void summarizeBattle()
    {
        SimpleLogger.print("---------- SUMMARY -----------");

        for (General general : CommandService.getGenerals()) {
            general.makeDecision();
            try {
                SimpleLogger.print(
                    String.format("%-50s", general.getName() + ": Decision - " + general.getDecision()) + 
                    "Blockchain is valid - " + general.getBlockchain().isChainValid());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                SimpleLogger.pressAnyKeyToContinue();
            }
        }
        SimpleLogger.print("\n");

        int numGeneralsInConsensus = 0;
        List<Decision> decisions = CommandService.getGenerals().stream().map(General::getDecision).collect(Collectors.toList());
        Decision decision = Mathf.mostCommon(decisions);
        for (General general : CommandService.getGenerals())
            if (general.getDecision().equals(decision) && !general.isTraitor())
                numGeneralsInConsensus++;

        // if majority have the same decision
        if (numGeneralsInConsensus == NUM_GENERALS - NUM_TRAITORS)
            SimpleLogger.printSameLine("CONSENSUS REACHED :)");
        else
            SimpleLogger.printSameLine("CONSENSUS NOT REACHED :(");
        SimpleLogger.print("       " + numGeneralsInConsensus + "/" + (NUM_GENERALS - NUM_TRAITORS) + " loyal generals agreed on the same plan");
        SimpleLogger.print("\n");
    }

    private static int inputValue(Scanner sc, int from, int to) {
        int input;
        input = sc.nextInt();

        while (input < from || input > to) {
            SimpleLogger.printSameLine("Invalid input. Try again: ");
            input = sc.nextInt();
        }
        return input;
    }

    public static void main(String[] args) 
    {
        try
        {
            Scanner sc = new Scanner(System.in);

            SimpleLogger.printSameLine("Set number of generals " + "(" + MIN_GENERALS + "-" + MAX_GENERALS + "): ");
            NUM_GENERALS = inputValue(sc, MIN_GENERALS, MAX_GENERALS);
            SimpleLogger.printSameLine("Set number of traitors " + "(0-" + (NUM_GENERALS - 1) / 3 + "): ");
            NUM_TRAITORS = inputValue(sc, 0, (NUM_GENERALS - 1) / 3);
            SimpleLogger.printSameLine("Set encryption level " + "(" + MIN_ENCLEVEL + "-" + MAX_ENCLEVEL + "): ");
            ENC_LEVEL = inputValue(sc, MIN_ENCLEVEL, MAX_ENCLEVEL);

            initCommunication();
            frame = new FrameMain(SCREEN_SIZE_X, SCREEN_SIZE_Y);

            sendOriginMessages();      
            debugLogGenerals();        

            Time.init();
            Time.timeScale = 1f;
            double t_fps = 0;
            while (CommandService.isRunning())
            {
                try
                {
                    Time.record();
                    t_fps += Time.getDeltaTimeUnscaled();

                    // update messenger position
                    ArrayList<Messenger> currentMessengers = new ArrayList<>(CommandService.getMessengers());
                    for (Messenger msger : currentMessengers) {
                        if (msger != null)
                            msger.update();
                    }

                    for (General general : CommandService.getGenerals()) {
                        general.update();
                    }

                    // update and render battle
                    frame.getBattlePanel().repaint();

                    // update fps every second
                    if (t_fps > 1) {
                        fps = 1.0 / Time.getDeltaTimeUnscaled();
                        t_fps = 0.0;
                        //debugLogGenerals();
                    }

                    limitFrameRate(TARGET_FPS);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    SimpleLogger.pressAnyKeyToContinue();
                }
            }   

            summarizeBattle(); 
            frame.getBattlePanel().repaint();
            debugLogGenerals();

            sc.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            SimpleLogger.pressAnyKeyToContinue();
        }
    }

    public static double getFPS() {
        return fps;
    }
    private static void limitFrameRate(float targetFPS) throws InterruptedException {
        float delay_ns = 1000000000f / targetFPS;
        busySleep((long)delay_ns);
    }
    public static void busySleep(long nanos)
    {
        long elapsed;
        final long startTime = System.nanoTime();
        do {
            elapsed = System.nanoTime() - startTime;
        } while (elapsed < nanos);
    }

    public static void debugLogGenerals() {
        frame.getDebugPanel().clearDebugText();
        frame.getDebugPanel().concatDebugText("ACTIVE THREADS: " + Thread.activeCount() + "\n");
        frame.getDebugPanel().concatDebugText("ACTIVE MESSENGERS: " + CommandService.getMessengers().size() + "\n");
        frame.getDebugPanel().concatDebugText("TOTAL MESSENGERS: " + Messenger.getMessengersCount() + "\n");
        for (General general : CommandService.getGenerals()) {
            frame.getDebugPanel().concatDebugText(general.toString());
        }
    }

    public static int getNumOfGenerals() {
        return NUM_GENERALS;
    }
    public static int getNumOfTraitors() {
        return NUM_TRAITORS;
    }
    public static int getEncLevel() {
        return ENC_LEVEL;
    }
    public static int getTotalNumMessengers() {
        int numRounds = getNumOfTraitors() + 1;
        int numGen = getNumOfGenerals();
        return numGen * (numGen - 1) * numRounds;
    }
}

