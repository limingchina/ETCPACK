package com.here.etc2;

public class EtcThumbBlock {

    // Compress a block using THUMB58H mode (fastest version).
    public static int compressBlockTHUMB58HFastest(byte[] img, int width, int height, int startx, int starty, int[] thumbH58Word1, int[] thumbH58Word2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = 255 * 255 * 3 * 16;
        int best_d = 0, best_p = 0;
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

        // Try all d and p values
        for (int d = 0; d < 8; d++) {
            for (int p = 0; p < 8; p++) {
                int current_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the d and p values
                        int r = color1[0];
                        int g = color1[1];
                        int b = color1[2];

                        int index = source.EtcTables.table58H[p];
                        int diff = source.EtcTables.compressParamsFast[d];

                        r = EtcUtils.clamp(r + diff * ((index >> 0) & 1), 0, 255);
                        g = EtcUtils.clamp(g + diff * ((index >> 1) & 1), 0, 255);
                        b = EtcUtils.clamp(b + diff * ((index >> 2) & 1), 0, 255);

                        approx[0] = (byte) r;
                        approx[1] = (byte) g;
                        approx[2] = (byte) b;

                        current_error += EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    }
                }
                if (current_error < best_error) {
                    best_error = current_error;
                    best_d = d;
                    best_p = p;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_d, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_p, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        thumbH58Word1[0] = block_part1;
        thumbH58Word2[0] = block_part2;
        return best_error;
    }

    // Compress a block using THUMB58H mode (fastest version) with perceptual error.
    public static int compressBlockTHUMB58HFastestPerceptual1000(byte[] img, int width, int height, int startx, int starty, int[] thumbH58Word1, int[] thumbH58Word2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = 255 * 255 * 3 * 16 * 1000;
        int best_d = 0, best_p = 0;
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

        // Try all d and p values
        for (int d = 0; d < 8; d++) {
            for (int p = 0; p < 8; p++) {
                int current_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the d and p values
                        int r = color1[0];
                        int g = color1[1];
                        int b = color1[2];

                        int index = source.EtcTables.table58H[p];
                        int diff = source.EtcTables.compressParamsFast[d];

                        r = EtcUtils.clamp(r + diff * ((index >> 0) & 1), 0, 255);
                        g = EtcUtils.clamp(g + diff * ((index >> 1) & 1), 0, 255);
                        b = EtcUtils.clamp(b + diff * ((index >> 2) & 1), 0, 255);

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
                    best_d = d;
                    best_p = p;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_d, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_p, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        thumbH58Word1[0] = block_part1;
        thumbH58Word2[0] = block_part2;
        return best_error;
    }

    // Compress a block using THUMB59T mode (fastest version).
    public static int compressBlockTHUMB59TFastest(byte[] img, int width, int height, int startx, int starty, int[] thumbT59Word1, int[] thumbT59Word2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = 255 * 255 * 3 * 16;
        int best_d = 0, best_p = 0;
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

        // Try all d and p values
        for (int d = 0; d < 8; d++) {
            for (int p = 0; p < 8; p++) {
                int current_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the d and p values
                        int r = color1[0];
                        int g = color1[1];
                        int b = color1[2];

                        int index = source.EtcTables.table59T[p];
                        int diff = source.EtcTables.compressParamsFast[d];

                        r = EtcUtils.clamp(r + diff * ((index >> 0) & 1), 0, 255);
                        g = EtcUtils.clamp(g + diff * ((index >> 1) & 1), 0, 255);
                        b = EtcUtils.clamp(b + diff * ((index >> 2) & 1), 0, 255);

                        approx[0] = (byte) r;
                        approx[1] = (byte) g;
                        approx[2] = (byte) b;

                        current_error += EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    }
                }
                if (current_error < best_error) {
                    best_error = current_error;
                    best_d = d;
                    best_p = p;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_d, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_p, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        thumbT59Word1[0] = block_part1;
        thumbT59Word2[0] = block_part2;
        return best_error;
    }

    // Compress a block using THUMB59T mode (fastest version) with perceptual error.
    public static int compressBlockTHUMB59TFastestPerceptual1000(byte[] img, int width, int height, int startx, int starty, int[] thumbT59Word1, int[] thumbT59Word2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int best_error = 255 * 255 * 3 * 16 * 1000;
        int best_d = 0, best_p = 0;
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

        // Try all d and p values
        for (int d = 0; d < 8; d++) {
            for (int p = 0; p < 8; p++) {
                int current_error = 0;
                for (i = 0; i < 4; i++) {
                    for (j = 0; j < 4; j++) {
                        orig[0] = (byte) EtcUtils.RED(img, width, startx + j, starty + i);
                        orig[1] = (byte) EtcUtils.GREEN(img, width, startx + j, starty + i);
                        orig[2] = (byte) EtcUtils.BLUE(img, width, startx + j, starty + i);

                        // Calculate the color based on the d and p values
                        int r = color1[0];
                        int g = color1[1];
                        int b = color1[2];

                        int index = source.EtcTables.table59T[p];
                        int diff = source.EtcTables.compressParamsFast[d];

                        r = EtcUtils.clamp(r + diff * ((index >> 0) & 1), 0, 255);
                        g = EtcUtils.clamp(g + diff * ((index >> 1) & 1), 0, 255);
                        b = EtcUtils.clamp(b + diff * ((index >> 2) & 1), 0, 255);

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
                    best_d = d;
                    best_p = p;
                }
            }
        }

        // Store the compressed data
        block_part1 = EtcUtils.putBits(block_part1, best_d, 3, 31);
        block_part1 = EtcUtils.putBits(block_part1, best_p, 3, 28);
        block_part1 = EtcUtils.putBits(block_part1, color1[0] >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, color1[1] >> 3, 5, 16);
        block_part1 = EtcUtils.putBits(block_part1, color1[2] >> 3, 5, 8);

        thumbT59Word1[0] = block_part1;
        thumbT59Word2[0] = block_part2;
        return best_error;
    }
} 