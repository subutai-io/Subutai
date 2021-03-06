package io.subutai.common.util;


/**
 * Provides number utility functions
 */
public class NumUtil
{

    private NumUtil()
    {
    }


    public static boolean isIntBetween( int num, int from, int to )
    {
        return num >= from && num <= to;
    }


    public static boolean isLongBetween( long num, long from, long to )
    {
        return num >= from && num <= to;
    }
}
