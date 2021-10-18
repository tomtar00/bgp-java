package com.koala.bgp.utils;

public class Mathf 
{
    public static int[] Lerp(int[] startPoint, int[] endPoint, float progress)
    {
        int[] result = new int[2];
        result[0] = (int)((endPoint[0] - startPoint[0]) * progress + startPoint[0]);
        result[1] = (int)((endPoint[1] - startPoint[1]) * progress + startPoint[1]);
        return result;
    }
}
