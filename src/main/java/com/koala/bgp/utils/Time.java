package com.koala.bgp.utils;

public class Time 
{
    private static long lastTime;
    private static long frameTime;

    public static float timeScale;

    public static void init()
    {
        lastTime = System.currentTimeMillis();
        frameTime = 0;
        timeScale = 1;  
    }

    public static void record()
    {
        long time = System.currentTimeMillis();
        frameTime = time - lastTime;
        lastTime = time;
    }

    public static double getDeltaTime()
    {
        return (double) (getFrameTime() * Time.timeScale);
    }
    public static double getFrameTime()
    {
        return Time.frameTime / 1000.0;
    }
    public static long getFrameTimeMillis()
    {
        return Time.frameTime;
    }
}
