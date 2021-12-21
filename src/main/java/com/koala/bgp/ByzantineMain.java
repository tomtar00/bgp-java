package com.koala.bgp;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
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
    private static volatile boolean interrupted = false;
    private static volatile boolean rendering = false;

    private static void summarizeBattle()
    {
        SimpleLogger.print("---------- SUMMARY -----------");

        for (General general : CommandService.getGenerals()) {
            //general.makeDecision();
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
        var decision = Mathf.mostCommon(decisions);
        for (General general : CommandService.getGenerals())
            if (general.getDecision().equals(decision.x) && !general.isTraitor())
                numGeneralsInConsensus++;

        // if majority have the same decision
        if (numGeneralsInConsensus == NUM_GENERALS - NUM_TRAITORS)
            SimpleLogger.printSameLine("CONSENSUS REACHED :)");
        else
            SimpleLogger.printSameLine("CONSENSUS NOT REACHED :(");
        SimpleLogger.print("       " + numGeneralsInConsensus + "/" + (NUM_GENERALS - NUM_TRAITORS) + " loyal generals agreed on the same plan");
        SimpleLogger.print("\n");
    }
    public static void Rendering(int generals, int traitors, int enc_level) {
        NUM_GENERALS = generals;
        NUM_TRAITORS = traitors;
        ENC_LEVEL = enc_level;

        CommandService.init(NUM_GENERALS, NUM_TRAITORS);
        CommandService.sendOriginMessages();

        Time.init();
        double t_fps = 0;

        interrupted = false;
        rendering = true;
        while (CommandService.isRunning() && !interrupted)
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
                frame.getBattlePanel().validate();
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

        if (!interrupted) {
            summarizeBattle();
        }

        rendering = false;
    }

    public static void main(String[] args) 
    {
        try
        {
            frame = new FrameMain(SCREEN_SIZE_X, SCREEN_SIZE_Y);
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
    public static synchronized void interruptRenderThread() {
        interrupted = true;
        CommandService.setIsRunning(false);
    }
    public static boolean isRendering() {
        return rendering;
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
        if (SetupPanel.getAlgorithm() == "Lamport") {
            return numGen * (numGen - 1) * numRounds;
        }
        else if (SetupPanel.getAlgorithm() == "King") {
            return numRounds * (numGen * numGen - 1);
        }
        else {
            SimpleLogger.logWarning("Wrong algorithm specified!");
            return 0;
        }
    }
}

