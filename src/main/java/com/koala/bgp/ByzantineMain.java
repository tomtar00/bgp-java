package com.koala.bgp;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.koala.bgp.byzantine.*;
import com.koala.bgp.utils.*;
import com.koala.bgp.visual.BattlePanel;
import com.koala.bgp.visual.FrameMain;
import com.koala.bgp.visual.SetupPanel;

public class ByzantineMain 
{
    public final static int SCREEN_SIZE_X = BattlePanel.PANEL_SIZE_X + SetupPanel.PANEL_SIZE_X;
    public final static int SCREEN_SIZE_Y = 700;
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

            CommandService.init(NUM_GENERALS, NUM_TRAITORS);
            frame = new FrameMain(SCREEN_SIZE_X, SCREEN_SIZE_Y);

            CommandService.sendOriginMessages();           

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

