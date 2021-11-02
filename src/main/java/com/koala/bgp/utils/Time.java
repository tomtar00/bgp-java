package com.koala.bgp.utils;

public class Time 
{
    private static long lastTime;
    private static long frameTime;

    public static float timeScale;

    public static void init()
    {
        lastTime = System.nanoTime();
        frameTime = 0;
        timeScale = 1;  
    }

    public static void record()
    {
        long time = System.nanoTime();
        frameTime = time - lastTime;
        lastTime = time;
    }

    public static double getDeltaTime()
    {
        return (double) (getFrameTime() * Time.timeScale);
    }
    public static double getDeltaTimeUnscaled()
    {
        return (double) (getFrameTime());
    }


    private static double getFrameTime()
    {
        return Time.frameTime / 1000000000.0;
    }
    public static long getFrameTimeMillis()
    {
        return Time.frameTime / 1000000;
    }
    public static long getFrameTimeNanos()
    {
        return Time.frameTime;
    }
}
