package com.here.etc2;

public class EtcIndividualBlock {

    // Compress a block using individual color mode exhaustively.
    public static int compressBlockIndividualExhaustive(byte[] img, int width, int height, int startx, int starty, int[] etc1IndividualWord1, int[] etc1IndividualWord2, int errorCurrentlyBest) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = errorCurrentlyBest;
        int best_r1 = 0, best_g1 = 0, best_b1 = 0, best_r2 = 0, best_g2 = 0, best_b2 = 0;
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];

        // Try all color combinations
        for (int rr1 = 0; rr1 < 32; rr1++) {
            for (int gg1 = 0; gg1 < 32; gg1++) {
                for (int bb1 = 0; bb1 < 32; bb1++) {
                    for (int rr2 = 0; rr2 < 32; rr2++) {
                        for (int gg2 = 0; gg2 < 32; gg2++) {
                            for (int bb2 = 0; bb2 < 32; bb2++) {
                                int current_error = 0;
                                for (i = 0; i < 4; i++) {
                                    for (j = 0; j < 4; j++) {
                                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                                        // Calculate the color based on the individual color mode
                                        int r, g, b;
                                        if ((i < 2) && (j < 2)) {
                                            r = rr1;
                                            g = gg1;
                                            b = bb1;
                                        } else {
                                            r = rr2;
                                            g = gg2;
                                            b = bb2;
                                        }

                                        approx[0] = (byte) EtcUtils.clamp(r << 3, 0, 255);
                                        approx[1] = (byte) EtcUtils.clamp(g << 3, 0, 255);
                                        approx[2] = (byte) EtcUtils.clamp(b << 3, 0, 255);

                                        current_error += EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                                    }
                                }
                                if (current_error < best_error) {
                                    best_error = current_error;
                                    best_r1 = rr1;
                                    best_g1 = gg1;
                                    best_b1 = bb1;
                                    best_r2 = rr2;
                                    best_g2 = gg2;
                                    best_b2 = bb2;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Store the best colors in the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_r1, 5, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_g1, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, best_b1, 5, 16);

        block_part2 = EtcUtils.putBits(block_part2, best_r2, 5, 31);
        block_part2 = EtcUtils.putBits(block_part2, best_g2, 5, 24);
        block_part2 = EtcUtils.putBits(block_part2, best_b2, 5, 16);

        etc1IndividualWord1[0] = block_part1;
        etc1IndividualWord2[0] = block_part2;
        return best_error;
    }

    // Compress a block using individual color mode exhaustively with perceptual error.
    public static int compressBlockIndividualExhaustivePerceptual(byte[] img, int width, int height, int startx, int starty, int[] etc1IndividualWord1, int[] etc1IndividualWord2, int errorCurrentlyBest) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = errorCurrentlyBest;
        int best_r1 = 0, best_g1 = 0, best_b1 = 0, best_r2 = 0, best_g2 = 0, best_b2 = 0;
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];

        // Try all color combinations
        for (int rr1 = 0; rr1 < 32; rr1++) {
            for (int gg1 = 0; gg1 < 32; gg1++) {
                for (int bb1 = 0; bb1 < 32; bb1++) {
                    for (int rr2 = 0; rr2 < 32; rr2++) {
                        for (int gg2 = 0; gg2 < 32; gg2++) {
                            for (int bb2 = 0; bb2 < 32; bb2++) {
                                int current_error = 0;
                                for (i = 0; i < 4; i++) {
                                    for (j = 0; j < 4; j++) {
                                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                                        // Calculate the color based on the individual color mode
                                        int r, g, b;
                                        if ((i < 2) && (j < 2)) {
                                            r = rr1;
                                            g = gg1;
                                            b = bb1;
                                        } else {
                                            r = rr2;
                                            g = gg2;
                                            b = bb2;
                                        }

                                        approx[0] = (byte) EtcUtils.clamp(r << 3, 0, 255);
                                        approx[1] = (byte) EtcUtils.clamp(g << 3, 0, 255);
                                        approx[2] = (byte) EtcUtils.clamp(b << 3, 0, 255);

                                        current_error += (EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 * EtcUtils.square(approx[0] - orig[0])
                                                + EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 * EtcUtils.square(approx[1] - orig[1])
                                                + EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 * EtcUtils.square(approx[2] - orig[2]));
                                    }
                                }
                                if (current_error < best_error) {
                                    best_error = current_error;
                                    best_r1 = rr1;
                                    best_g1 = gg1;
                                    best_b1 = bb1;
                                    best_r2 = rr2;
                                    best_g2 = gg2;
                                    best_b2 = bb2;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Store the best colors in the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_r1, 5, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_g1, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, best_b1, 5, 16);

        block_part2 = EtcUtils.putBits(block_part2, best_r2, 5, 31);
        block_part2 = EtcUtils.putBits(block_part2, best_g2, 5, 24);
        block_part2 = EtcUtils.putBits(block_part2, best_b2, 5, 16);

        etc1IndividualWord1[0] = block_part1;
        etc1IndividualWord2[0] = block_part2;
        return best_error;
    }
} 