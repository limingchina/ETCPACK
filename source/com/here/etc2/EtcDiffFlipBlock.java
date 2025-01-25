package com.here.etc2;

public class EtcDiffFlipBlock {

    // Compress a block using differential and flip mode (fast version).
    public static int compressBlockDiffFlipFast(byte[] img, byte[] imgdec, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int sumError = 0;
        int i, j, k, l;
        int best_sum_error = 255 * 255 * 3 * 16;
        int best_mult = 0;
        int best_flip = 0;
        int[] color1 = new int[3];
        int[] color2 = new int[3];
        int[] color3 = new int[3];
        int[] color4 = new int[3];
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

        // Try all multipliers and flip modes
        for (int mult = 0; mult < 8; mult++) {
            for (int flip = 0; flip < 2; flip++) {
                int current_sum_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the multiplier and flip mode
                        int diff = source.EtcTables.compressParamsFast[mult];
                        if (flip == 0) {
                            approx[0] = (byte) EtcUtils.clamp(color1[0] + diff, 0, 255);
                            approx[1] = (byte) EtcUtils.clamp(color1[1] + diff, 0, 255);
                            approx[2] = (byte) EtcUtils.clamp(color1[2] + diff, 0, 255);
                        } else {
                            approx[0] = (byte) EtcUtils.clamp(color1[0] - diff, 0, 255);
                            approx[1] = (byte) EtcUtils.clamp(color1[1] - diff, 0, 255);
                            approx[2] = (byte) EtcUtils.clamp(color1[2] - diff, 0, 255);
                        }

                        current_sum_error += EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    }
                }
                if (current_sum_error < best_sum_error) {
                    best_sum_error = current_sum_error;
                    best_mult = mult;
                    best_flip = flip;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_mult, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_flip, 1, 30);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        compressed1[0] = block_part1;
        compressed2[0] = block_part2;
        return best_sum_error;
    }

    // Compress a block using differential and flip mode (fast version) with perceptual error.
    public static int compressBlockDiffFlipFastPerceptual(byte[] img, byte[] imgdec, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int sumError = 0;
        int i, j, k, l;
        int best_sum_error = 255 * 255 * 3 * 16 * 1000;
        int best_mult = 0;
        int best_flip = 0;
        int[] color1 = new int[3];
        int[] color2 = new int[3];
        int[] color3 = new int[3];
        int[] color4 = new int[3];
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

        // Try all multipliers and flip modes
        for (int mult = 0; mult < 8; mult++) {
            for (int flip = 0; flip < 2; flip++) {
                int current_sum_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the multiplier and flip mode
                        int diff = source.EtcTables.compressParamsFast[mult];
                        if (flip == 0) {
                            approx[0] = (byte) EtcUtils.clamp(color1[0] + diff, 0, 255);
                            approx[1] = (byte) EtcUtils.clamp(color1[1] + diff, 0, 255);
                            approx[2] = (byte) EtcUtils.clamp(color1[2] + diff, 0, 255);
                        } else {
                            approx[0] = (byte) EtcUtils.clamp(color1[0] - diff, 0, 255);
                            approx[1] = (byte) EtcUtils.clamp(color1[1] - diff, 0, 255);
                            approx[2] = (byte) EtcUtils.clamp(color1[2] - diff, 0, 255);
                        }

                        current_sum_error += (EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 * EtcUtils.square(approx[0] - orig[0])
                                + EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 * EtcUtils.square(approx[1] - orig[1])
                                + EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 * EtcUtils.square(approx[2] - orig[2]));
                    }
                }
                if (current_sum_error < best_sum_error) {
                    best_sum_error = current_sum_error;
                    best_mult = mult;
                    best_flip = flip;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_mult, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_flip, 1, 30);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        compressed1[0] = block_part1;
        compressed2[0] = block_part2;
        return best_sum_error;
    }
} 