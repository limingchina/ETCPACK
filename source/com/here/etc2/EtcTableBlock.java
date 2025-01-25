package com.here.etc2;

public class EtcTableBlock {

    // Finds all pixel indices for a 2x4 block.
    public static int compressBlockWithTable2x4(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        int sumError = 0;
        int q, i;

        i = 0;
        for (int x = startx; x < startx + 2; x++) {
            for (int y = starty; y < starty + 4; y++) {
                int err;
                int best = 0;
                int minError = 255 * 255 * 3 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }
                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);

                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.
                sumError += minError;
            }
        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }

    // Finds all pixel indices for a 2x4 block using perceptual weighting of error.
    // Done using fixed point arithmetics where weights are multiplied by 1000.
    public static int compressBlockWithTable2x4percep1000(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        int sumError = 0;
        int q, i;

        i = 0;
        for (int x = startx; x < startx + 2; x++) {
            for (int y = starty; y < starty + 4; y++) {
                int err;
                int best = 0;
                int minError = 1000 * 255 * 255 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = (EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 * EtcUtils.square(approx[0] - orig[0])
                            + EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 * EtcUtils.square(approx[1] - orig[1])
                            + EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 * EtcUtils.square(approx[2] - orig[2]));
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }

                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);

                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.
                sumError += minError;
            }
        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }

    // Finds all pixel indices for a 2x4 block using perceptual weighting of error.
    public static float compressBlockWithTable2x4percep(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        float sumError = 0;
        int q, i;

        double wR2 = EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED;
        double wG2 = EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED;
        double wB2 = EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED;

        i = 0;
        for (int x = startx; x < startx + 2; x++) {
            for (int y = starty; y < starty + 4; y++) {
                float err;
                int best = 0;
                float minError = 255 * 255 * 3 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = (float) (wR2 * EtcUtils.square(approx[0] - orig[0]) + (float) wG2 * EtcUtils.square(approx[1] - orig[1]) + (float) wB2 * EtcUtils.square(approx[2] - orig[2]));
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }

                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);

                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.
                sumError += minError;
            }
        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }

    // Finds all pixel indices for a 4x2 block.
    public static int compressBlockWithTable4x2(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        int sumError = 0;
        int q;
        int i;

        i = 0;
        for (int x = startx; x < startx + 4; x++) {
            for (int y = starty; y < starty + 2; y++) {
                int err;
                int best = 0;
                int minError = 255 * 255 * 3 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }
                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);
                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.

                sumError += minError;
            }
            i += 2;
        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }

    // Finds all pixel indices for a 4x2 block using perceptual weighting of error.
    // Done using fixed point arithmetics where 1000 corresponds to 1.0.
    public static int compressBlockWithTable4x2percep1000(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        int sumError = 0;
        int q;
        int i;

        i = 0;
        for (int x = startx; x < startx + 4; x++) {
            for (int y = starty; y < starty + 2; y++) {
                int err;
                int best = 0;
                int minError = 1000 * 255 * 255 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 * EtcUtils.square(approx[0] - orig[0])
                            + EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 * EtcUtils.square(approx[1] - orig[1])
                            + EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 * EtcUtils.square(approx[2] - orig[2]);
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }
                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);
                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.

                sumError += minError;
            }
            i += 2;

        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }

    // Finds all pixel indices for a 4x2 block using perceptual weighting of error.
    public static float compressBlockWithTable4x2percep(byte[] img, int width, int height, int startx, int starty, byte[] avgColor, int table, int[] pixelIndicesMSB, int[] pixelIndicesLSB) {
        byte[] orig = new byte[3];
        byte[] approx = new byte[3];
        int pixelIndicesMSB_val = 0, pixelIndicesLSB_val = 0, pixelIndices = 0;
        float sumError = 0;
        int q;
        int i;
        float wR2 = (float) EtcUtils.PERCEPTUAL_WEIGHT_R_SQUARED;
        float wG2 = (float) EtcUtils.PERCEPTUAL_WEIGHT_G_SQUARED;
        float wB2 = (float) EtcUtils.PERCEPTUAL_WEIGHT_B_SQUARED;

        i = 0;
        for (int x = startx; x < startx + 4; x++) {
            for (int y = starty; y < starty + 2; y++) {
                float err;
                int best = 0;
                float minError = 255 * 255 * 3 * 16;
                orig[0] = (byte) EtcUtils.RED(img, width, x, y);
                orig[1] = (byte) EtcUtils.GREEN(img, width, x, y);
                orig[2] = (byte) EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = (byte) EtcUtils.clamp(avgColor[0] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[1] = (byte) EtcUtils.clamp(avgColor[1] + source.EtcTables.compressParams[table][q], 0, 255);
                    approx[2] = (byte) EtcUtils.clamp(avgColor[2] + source.EtcTables.compressParams[table][q], 0, 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = (float) wR2 * EtcUtils.square(approx[0] - orig[0]) + (float) wG2 * EtcUtils.square(approx[1] - orig[1]) + (float) wB2 * EtcUtils.square(approx[2] - orig[2]);
                    if (err < minError) {
                        minError = err;
                        best = q;
                    }
                }
                pixelIndices = source.EtcTables.scramble[best];

                // Store the MSB and LSB of the pixel index
                pixelIndicesMSB_val = EtcUtils.putBits(pixelIndicesMSB_val, (pixelIndices >> 1), 1, i);
                pixelIndicesLSB_val = EtcUtils.putBits(pixelIndicesLSB_val, (pixelIndices & 1), 1, i);
                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.

                sumError += minError;
            }
            i += 2;
        }

        pixelIndicesMSB[0] = pixelIndicesMSB_val;
        pixelIndicesLSB[0] = pixelIndicesLSB_val;
        return sumError;
    }
} 