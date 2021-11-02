package com.koala.bgp.utils;

import java.util.logging.*;

public class SimpleLogger 
{
    private final static Logger logger = Logger.getLogger(SimpleLogger.class.getName());

    public static void print(Object msg)
    {
        System.out.println(msg.toString());
    }
    public static void printSameLine(Object msg)
    {
        System.out.print(msg.toString());
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

    public static void pressAnyKeyToContinue()
    { 
        print("Press Enter key to continue...");
        try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}  
    }
    public static void pressAnyKeyToContinue(Object msg)
    { 
        print(msg);
        print("Press Enter key to continue...");
        try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}  
    }
    
}
