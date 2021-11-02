package com.koala.bgp.byzantine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.koala.bgp.utils.SimpleLogger;

public class CommandService
{
    private static volatile List<General> generals;
    private static volatile List<Messenger> messengers;
    private static volatile boolean isRunning;

    private static volatile int votes;
    private static volatile boolean ending = false;

    public static void init(int num_generals, int num_traitors) {
        generals = Collections.synchronizedList(new ArrayList<>());
        messengers = Collections.synchronizedList(new ArrayList<>());
        votes = 0;
    }


    public static List<General> getGenerals() {
        return generals;
    }

    public static List<Messenger> getMessengers() {
        return messengers;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void setIsRunning(boolean running) {
        isRunning = running;
    }

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
}
