package com.koala.bgp.utils;

import java.util.logging.*;

public class SimpleLogger 
{
    private final static Logger logger = Logger.getLogger(SimpleLogger.class.getName());

    public static void print(Object msg)
    {
        System.out.println(msg.toString());
    }
    public static void logInfo(String msg)
    {
        System.out.println();
        logger.log(Level.INFO, msg);
    }
    public static void logWarning(String msg)
    {
        System.out.println();
        logger.log(Level.WARNING, msg);
    }
}
