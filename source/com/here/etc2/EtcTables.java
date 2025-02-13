package com.here.etc2;

public class EtcTables {

    // Tables for compression parameters
    public static final int[][] compressParams = {
        { -8,  -2,   2,   8},
        { -8,  -2,   2,   8},
        {-17,  -5,   5,  17},
        {-17,  -5,   5,  17},
        {-29,  -9,   9,  29},
        {-29,  -9,   9,  29},
        {-42, -13,  13,  42},
        {-42, -13,  13,  42},
        {-60, -18,  18,  60},
        {-60, -18,  18,  60},
        {-80, -24,  24,  80},
        {-80, -24,  24,  80},
        {-106,-33,  33, 106},
        {-106,-33,  33, 106},
        {-183,-47,  47, 183},
        {-183,-47,  47, 183}
    };

    // Scramble table for pixel indices
    public static final int[] scramble = {3, 2, 0, 1};

    // Unscramble table for pixel indices
    public static final int[] unscramble = {2, 3, 1, 0};

    // Table for THUMB59T mode
    public static final int[] table59T = {0, 1, 2, 3, -4, -3, -2, -1};

    // Table for THUMB58H mode
    public static final int[] table58H = {3, 6, 11, 16, 23, 32, 41, 64};

    // Table for fast compression parameters
    public static final int[] compressParamsFast = {
         -8,  -2,  2,   8,
									 -17,  -5,  5,  17,
									 -29,  -9,  9,  29,
									 -42, -13, 13,  42,
 									 -60, -18, 18,  60,
									 -80, -24, 24,  80,
									-106, -33, 33, 106,
									-183, -47, 47, 183
    };
}