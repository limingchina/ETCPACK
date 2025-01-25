package com.here.etc2;

public class EtcDifferentialBlock {

    // Compress a block using differential mode exhaustively.
    public static int compressBlockDifferentialExhaustive(byte[] img, int width, int height, int startx, int starty, int[] etc1DifferentialWord1, int[] etc1DifferentialWord2, int errorCurrentlyBest) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = errorCurrentlyBest;
        int best_mult1 = 0, best_mult2 = 0, best_mult3 = 0, best_mult4 = 0;
        int[] color1 = new int[3];
        int[] color2 = new int[3];
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];

        // Compute average color of the block
        color1[0] = 0;
        color1[1] = 0;
        color1[2] = 0;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                color1[0] += EtcUtils.RED(img, width, startx + j, starty + i);
                color1[1] += EtcUtils.GREEN(img, width, startx + j, starty + i);
                color1[2] += EtcUtils.BLUE(img, width, startx + j, starty + i);
            }
        }
        color1[0] = (color1[0] + 8) / 16;
        color1[1] = (color1[1] + 8) / 16;
        color1[2] = (color1[2] + 8) / 16;

        // Try all multipliers
        for (int mult1 = 0; mult1 < 8; mult1++) {
            for (int mult2 = 0; mult2 < 8; mult2++) {
                for (int mult3 = 0; mult3 < 8; mult3++) {
                    for (int mult4 = 0; mult4 < 8; mult4++) {
                        int current_error = 0;
                        for (i = 0; i < 4; i++) {
                            for (j = 0; j < 4; j++) {
                                orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                                orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                                orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                                // Calculate the color based on the multipliers
                                int r = color1[0];
                                int g = color1[1];
                                int b = color1[2];

                                if (i == 0 && j == 0) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                } else if (i == 0 && j == 1) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                } else if (i == 1 && j == 0) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                } else if (i == 1 && j == 1) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                }

                                approx[0] = (byte) r;
                                approx[1] = (byte) g;
                                approx[2] = (byte) b;

                                current_error += EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                            }
                        }
                        if (current_error < best_error) {
                            best_error = current_error;
                            best_mult1 = mult1;
                            best_mult2 = mult2;
                            best_mult3 = mult3;
                            best_mult4 = mult4;
                        }
                    }
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_mult1, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_mult2, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, best_mult3, 3, 25);
        block_part1 = EtcUtils.putBits(block_part1, best_mult4, 3, 22);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 8);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 0);

        etc1DifferentialWord1[0] = block_part1;
        etc1DifferentialWord2[0] = block_part2;
        return best_error;
    }

    // Compress a block using differential mode exhaustively with perceptual error.
    public static int compressBlockDifferentialExhaustivePerceptual(byte[] img, int width, int height, int startx, int starty, int[] etc1DifferentialWord1, int[] etc1DifferentialWord2, int errorCurrentlyBest) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = errorCurrentlyBest;
        int best_mult1 = 0, best_mult2 = 0, best_mult3 = 0, best_mult4 = 0;
        int[] color1 = new int[3];
        int[] color2 = new int[3];
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];

        // Compute average color of the block
        color1[0] = 0;
        color1[1] = 0;
        color1[2] = 0;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                color1[0] += EtcUtils.RED(img, width, startx + j, starty + i);
                color1[1] += EtcUtils.GREEN(img, width, startx + j, starty + i);
                color1[2] += EtcUtils.BLUE(img, width, startx + j, starty + i);
            }
        }
        color1[0] = (color1[0] + 8) / 16;
        color1[1] = (color1[1] + 8) / 16;
        color1[2] = (color1[2] + 8) / 16;

        // Try all multipliers
        for (int mult1 = 0; mult1 < 8; mult1++) {
            for (int mult2 = 0; mult2 < 8; mult2++) {
                for (int mult3 = 0; mult3 < 8; mult3++) {
                    for (int mult4 = 0; mult4 < 8; mult4++) {
                        int current_error = 0;
                        for (i = 0; i < 4; i++) {
                            for (j = 0; j < 4; j++) {
                                orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                                orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                                orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                                // Calculate the color based on the multipliers
                                int r = color1[0];
                                int g = color1[1];
                                int b = color1[2];

                                if (i == 0 && j == 0) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult1], 0, 255);
                                } else if (i == 0 && j == 1) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult2], 0, 255);
                                } else if (i == 1 && j == 0) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult3], 0, 255);
                                } else if (i == 1 && j == 1) {
                                    r = EtcUtils.clamp(r + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                    g = EtcUtils.clamp(g + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                    b = EtcUtils.clamp(b + source.EtcTables.compressParamsFast[mult4], 0, 255);
                                }

                                approx[0] = (byte) r;
                                approx[1] = (byte) g;
                                approx[2] = (byte) b;

                                current_error += (EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 * EtcUtils.square(approx[0] - orig[0])
                                        + EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 * EtcUtils.square(approx[1] - orig[1])
                                        + EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 * EtcUtils.square(approx[2] - orig[2]));
                            }
                        }
                        if (current_error < best_error) {
                            best_error = current_error;
                            best_mult1 = mult1;
                            best_mult2 = mult2;
                            best_mult3 = mult3;
                            best_mult4 = mult4;
                        }
                    }
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_mult1, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_mult2, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, best_mult3, 3, 25);
        block_part1 = EtcUtils.putBits(block_part1, best_mult4, 3, 22);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 8);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 0);

        etc1DifferentialWord1[0] = block_part1;
        etc1DifferentialWord2[0] = block_part2;
        return best_error;
    }
} 