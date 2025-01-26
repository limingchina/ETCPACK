package com.here.etc2;

public class EtcDiffFlipBlock {

    // Compress a block using differential and flip mode (fast version).
    public static int compressBlockDiffFlipFast(byte[] img, byte[] imgdec, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
        // For simplicity, we'll only use the average mode instead of comparing with combined mode

        
        // Call the average mode compression
        compressBlockDiffFlipAverage(img, width, height, startx, starty, compressed1, compressed2);
        
        return 0;
    }

    public static void compressBlockDiffFlipAverage(byte[] img, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
        int compressed1_norm, compressed2_norm;
        int compressed1_flip, compressed2_flip;
        int[] avg_color_quant1 = new int[3];
        int[] avg_color_quant2 = new int[3];

        float[] avg_color_float1 = new float[3];
        float[] avg_color_float2 = new float[3];
        int[] enc_color1 = new int[3];
        int[] enc_color2 = new int[3];
        int[] diff = new int[3];
        int min_error = 255 * 255 * 8 * 3;
        int best_table_indices1 = 0;
        int best_table_indices2 = 0;
        int[] best_table1 = new int[]{0};
        int[] best_table2 = new int[]{0};
        int diffbit;

        int norm_err = 0;
        int flip_err = 0;

        // First try normal blocks 2x4:
        computeAverageColor2x4noQuantFloat(img, width, height, startx, starty, avg_color_float1);
        computeAverageColor2x4noQuantFloat(img, width, height, startx + 2, starty, avg_color_float2);

        // First test if avg_color1 is similar enough to avg_color2 so that
        // we can use differential coding of colors.

        float eps;

        enc_color1[0] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[0] / 255.0));
        enc_color1[1] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[1] / 255.0));
        enc_color1[2] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[2] / 255.0));
        enc_color2[0] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[0] / 255.0));
        enc_color2[1] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[1] / 255.0));
        enc_color2[2] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[2] / 255.0));

        diff[0] = enc_color2[0] - enc_color1[0];
        diff[1] = enc_color2[1] - enc_color1[1];
        diff[2] = enc_color2[2] - enc_color1[2];

        if ((diff[0] >= -4) && (diff[0] <= 3) && (diff[1] >= -4) && (diff[1] <= 3) && (diff[2] >= -4) && (diff[2] <= 3)) {
            diffbit = 1;

            // The difference to be coded:

            diff[0] = enc_color2[0] - enc_color1[0];
            diff[1] = enc_color2[1] - enc_color1[1];
            diff[2] = enc_color2[2] - enc_color1[2];

            avg_color_quant1[0] = (enc_color1[0] << 3 | (enc_color1[0] >> 2));
            avg_color_quant1[1] = (enc_color1[1] << 3 | (enc_color1[1] >> 2));
            avg_color_quant1[2] = (enc_color1[2] << 3 | (enc_color1[2] >> 2));
            avg_color_quant2[0] = (enc_color2[0] << 3 | (enc_color2[0] >> 2));
            avg_color_quant2[1] = (enc_color2[1] << 3 | (enc_color2[1] >> 2));
            avg_color_quant2[2] = (enc_color2[2] << 3 | (enc_color2[2] >> 2));
            // Pack bits into the first word.

            compressed1_norm = 0;
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, diffbit, 1, 33);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[0], 5, 63);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[1], 5, 55);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[2], 5, 47);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, diff[0], 3, 58);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, diff[1], 3, 50);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, diff[2], 3, 42);

            int[] best_pixel_indices1_MSB = new int[]{0};
            int[] best_pixel_indices1_LSB = new int[]{0};
            int[] best_pixel_indices2_MSB = new int[]{0};
            int[] best_pixel_indices2_LSB = new int[]{0};

            norm_err = 0;

            // left part of block
            norm_err = tryalltables_3bittable2x4(img, width, height, startx, starty, avg_color_quant1, best_table1, best_pixel_indices1_MSB, best_pixel_indices1_LSB);

            // right part of block
            norm_err += tryalltables_3bittable2x4(img, width, height, startx + 2, starty, avg_color_quant2, best_table2, best_pixel_indices2_MSB, best_pixel_indices2_LSB);

            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, best_table1[0], 3, 39);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, best_table2[0], 3, 36);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, 0, 1, 32);

            compressed2_norm = 0;
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices1_MSB[0], 8, 23);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices2_MSB[0], 8, 31);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices1_LSB[0], 8, 7);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices2_LSB[0], 8, 15);

        } else {
            diffbit = 0;
            // The difference is bigger than what fits in 555 plus delta-333, so we will have
            // to deal with 444 444.

            eps = 0.0001f;

            enc_color1[0] = (int) (((float) avg_color_float1[0] / 17.0f) + 0.5f + eps);
            enc_color1[1] = (int) (((float) avg_color_float1[1] / 17.0f) + 0.5f + eps);
            enc_color1[2] = (int) (((float) avg_color_float1[2] / 17.0f) + 0.5f + eps);
            enc_color2[0] = (int) (((float) avg_color_float2[0] / 17.0f) + 0.5f + eps);
            enc_color2[1] = (int) (((float) avg_color_float2[1] / 17.0f) + 0.5f + eps);
            enc_color2[2] = (int) (((float) avg_color_float2[2] / 17.0f) + 0.5f + eps);
            avg_color_quant1[0] = (enc_color1[0] << 4 | enc_color1[0]);
            avg_color_quant1[1] = (enc_color1[1] << 4 | enc_color1[1]);
            avg_color_quant1[2] = (enc_color1[2] << 4 | enc_color1[2]);
            avg_color_quant2[0] = (enc_color2[0] << 4 | enc_color2[0]);
            avg_color_quant2[1] = (enc_color2[1] << 4 | enc_color2[1]);
            avg_color_quant2[2] = (enc_color2[2] << 4 | enc_color2[2]);

            // Pack bits into the first word.

            compressed1_norm = 0;
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, diffbit, 1, 33);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[0], 4, 63);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[1], 4, 55);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color1[2], 4, 47);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color2[0], 4, 59);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color2[1], 4, 51);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, enc_color2[2], 4, 43);

            int[] best_pixel_indices1_MSB = new int[]{0};
            int[] best_pixel_indices1_LSB = new int[]{0};
            int[] best_pixel_indices2_MSB = new int[]{0};
            int[] best_pixel_indices2_LSB = new int[]{0};

            // left part of block
            norm_err = tryalltables_3bittable2x4(img, width, height, startx, starty, avg_color_quant1, best_table1, best_pixel_indices1_MSB, best_pixel_indices1_LSB);

            // right part of block
            norm_err += tryalltables_3bittable2x4(img, width, height, startx + 2, starty, avg_color_quant2, best_table2, best_pixel_indices2_MSB, best_pixel_indices2_LSB);

            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, best_table1[0], 3, 39);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, best_table2[0], 3, 36);
            compressed1_norm = EtcUtils.putBitsHigh(compressed1_norm, 0, 1, 32);

            compressed2_norm = 0;
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices1_MSB[0], 8, 23);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices2_MSB[0], 8, 31);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices1_LSB[0], 8, 7);
            compressed2_norm = EtcUtils.putBits(compressed2_norm, best_pixel_indices2_LSB[0], 8, 15);
        }

        // Now try flipped blocks 4x2:

        computeAverageColor4x2noQuantFloat(img, width, height, startx, starty, avg_color_float1);
        computeAverageColor4x2noQuantFloat(img, width, height, startx, starty + 2, avg_color_float2);

        // First test if avg_color1 is similar enough to avg_color2 so that
        // we can use differential coding of colors.

        enc_color1[0] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[0] / 255.0));
        enc_color1[1] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[1] / 255.0));
        enc_color1[2] = (int) (EtcUtils.jasRound(31.0 * avg_color_float1[2] / 255.0));
        enc_color2[0] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[0] / 255.0));
        enc_color2[1] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[1] / 255.0));
        enc_color2[2] = (int) (EtcUtils.jasRound(31.0 * avg_color_float2[2] / 255.0));

        diff[0] = enc_color2[0] - enc_color1[0];
        diff[1] = enc_color2[1] - enc_color1[1];
        diff[2] = enc_color2[2] - enc_color1[2];

        if ((diff[0] >= -4) && (diff[0] <= 3) && (diff[1] >= -4) && (diff[1] <= 3) && (diff[2] >= -4) && (diff[2] <= 3)) {
            diffbit = 1;

            // The difference to be coded:

            diff[0] = enc_color2[0] - enc_color1[0];
            diff[1] = enc_color2[1] - enc_color1[1];
            diff[2] = enc_color2[2] - enc_color1[2];

            avg_color_quant1[0] = (enc_color1[0] << 3 | (enc_color1[0] >> 2));
            avg_color_quant1[1] = (enc_color1[1] << 3 | (enc_color1[1] >> 2));
            avg_color_quant1[2] = (enc_color1[2] << 3 | (enc_color1[2] >> 2));
            avg_color_quant2[0] = (enc_color2[0] << 3 | (enc_color2[0] >> 2));
            avg_color_quant2[1] = (enc_color2[1] << 3 | (enc_color2[1] >> 2));
            avg_color_quant2[2] = (enc_color2[2] << 3 | (enc_color2[2] >> 2));

            // Pack bits into the first word.

            compressed1_flip = 0;
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, diffbit, 1, 33);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[0], 5, 63);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[1], 5, 55);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[2], 5, 47);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, diff[0], 3, 58);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, diff[1], 3, 50);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, diff[2], 3, 42);

            int[] best_pixel_indices1_MSB = new int[]{0};
            int[] best_pixel_indices1_LSB = new int[]{0};
            int[] best_pixel_indices2_MSB = new int[]{0};
            int[] best_pixel_indices2_LSB = new int[]{0};

            // upper part of block
            flip_err = tryalltables_3bittable4x2(img, width, height, startx, starty, avg_color_quant1, best_table1, best_pixel_indices1_MSB, best_pixel_indices1_LSB);
            // lower part of block
            flip_err += tryalltables_3bittable4x2(img, width, height, startx, starty + 2, avg_color_quant2, best_table2, best_pixel_indices2_MSB, best_pixel_indices2_LSB);

            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, best_table1[0], 3, 39);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, best_table2[0], 3, 36);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, 1, 1, 32);

            best_pixel_indices1_MSB[0] |= (best_pixel_indices2_MSB[0] << 2);
            best_pixel_indices1_LSB[0] |= (best_pixel_indices2_LSB[0] << 2);

            compressed2_flip = ((best_pixel_indices1_MSB[0] & 0xffff) << 16) | (best_pixel_indices1_LSB[0] & 0xffff);
        } else {
            diffbit = 0;
            // The difference is bigger than what fits in 555 plus delta-333, so we will have
            // to deal with 444 444.
            eps = 0.0001f;

            enc_color1[0] = (int) (((float) avg_color_float1[0] / 17.0f) + 0.5f + eps);
            enc_color1[1] = (int) (((float) avg_color_float1[1] / 17.0f) + 0.5f + eps);
            enc_color1[2] = (int) (((float) avg_color_float1[2] / 17.0f) + 0.5f + eps);
            enc_color2[0] = (int) (((float) avg_color_float2[0] / 17.0f) + 0.5f + eps);
            enc_color2[1] = (int) (((float) avg_color_float2[1] / 17.0f) + 0.5f + eps);
            enc_color2[2] = (int) (((float) avg_color_float2[2] / 17.0f) + 0.5f + eps);

            avg_color_quant1[0] = (enc_color1[0] << 4 | enc_color1[0]);
            avg_color_quant1[1] = (enc_color1[1] << 4 | enc_color1[1]);
            avg_color_quant1[2] = (enc_color1[2] << 4 | enc_color1[2]);
            avg_color_quant2[0] = (enc_color2[0] << 4 | enc_color2[0]);
            avg_color_quant2[1] = (enc_color2[1] << 4 | enc_color2[1]);
            avg_color_quant2[2] = (enc_color2[2] << 4 | enc_color2[2]);

            // Pack bits into the first word.

            compressed1_flip = 0;
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, diffbit, 1, 33);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[0], 4, 63);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[1], 4, 55);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color1[2], 4, 47);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color2[0], 4, 59);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color2[1], 4, 51);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, enc_color2[2], 4, 43);

            int[] best_pixel_indices1_MSB = new int[1];
            int[] best_pixel_indices1_LSB = new int[1];
            int[] best_pixel_indices2_MSB = new int[1];
            int[] best_pixel_indices2_LSB = new int[1];

            // upper part of block
            flip_err = tryalltables_3bittable4x2(img, width, height, startx, starty, avg_color_quant1, best_table1, best_pixel_indices1_MSB, best_pixel_indices1_LSB);
            // lower part of block
            flip_err += tryalltables_3bittable4x2(img, width, height, startx, starty + 2, avg_color_quant2, best_table2, best_pixel_indices2_MSB, best_pixel_indices2_LSB);

            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, best_table1[0], 3, 39);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, best_table2[0], 3, 36);
            compressed1_flip = EtcUtils.putBitsHigh(compressed1_flip, 1, 1, 32);

            best_pixel_indices1_MSB[0] |= (best_pixel_indices2_MSB[0] << 2);
            best_pixel_indices1_LSB[0] |= (best_pixel_indices2_LSB[0] << 2);

            compressed2_flip = ((best_pixel_indices1_MSB[0] & 0xffff) << 16) | (best_pixel_indices1_LSB[0] & 0xffff);
        }

        // Now lets see which is the best table to use. Only 8 tables are possible.

        if (norm_err <= flip_err) {
            compressed1[0] = compressed1_norm | 0;
            compressed2[0] = compressed2_norm;
        } else {
            compressed1[0] = compressed1_flip | 1;
            compressed2[0] = compressed2_flip;
        }
        //System.out.println("norm_err = " + norm_err + ", flip_err = " + flip_err + ", compressed1 = " + compressed1[0] + ", compressed2 = " + compressed2[0]);
    }

    private static void computeAverageColor2x4noQuantFloat(byte[] img, int width, int height, int startx, int starty, float[] avg_color) {
        avg_color[0] = avg_color[1] = avg_color[2] = 0.0f;
        
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                avg_color[0] += EtcUtils.RED(img, width, startx + x, starty + y);
                avg_color[1] += EtcUtils.GREEN(img, width, startx + x, starty + y);
                avg_color[2] += EtcUtils.BLUE(img, width, startx + x, starty + y);
            }
        }
        avg_color[0] /= 8.0f;
        avg_color[1] /= 8.0f;
        avg_color[2] /= 8.0f;
    }

    private static void computeAverageColor4x2noQuantFloat(byte[] img, int width, int height, int startx, int starty, float[] avg_color) {
        int r = 0, g = 0, b = 0;
        for (int y = starty; y < starty + 2; y++) {
            for (int x = startx; x < startx + 4; x++) {
                r += EtcUtils.RED(img, width, x, y);
                g += EtcUtils.GREEN(img, width, x, y);
                b += EtcUtils.BLUE(img, width, x, y);
            }
        }

        avg_color[0] = (float) (r / 8.0);
        avg_color[1] = (float) (g / 8.0);
        avg_color[2] = (float) (b / 8.0);
    }

    private static int compressBlockWithTable2x4(byte[] img, int width, int height, int startx, int starty, int[] avg_color, int table, int[] pixel_indices_MSBp, int[] pixel_indices_LSBp) {
        int[] orig = new int[3];
        int[] approx = new int[3];
        int pixel_indices_MSB = 0;
        int pixel_indices_LSB = 0;
        int pixel_indices = 0;
        int sum_error = 0;
        int q, i;

        i = 0;
        for (int x = startx; x < startx + 2; x++) {
            for (int y = starty; y < starty + 4; y++) {
                int err;
                int best = 0;
                int min_error = 255 * 255 * 3 * 16;
                orig[0] = EtcUtils.RED(img, width, x, y);
                orig[1] = EtcUtils.GREEN(img, width, x, y);
                orig[2] = EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][q], 255);
                    approx[1] = EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][q], 255);
                    approx[2] = EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][q], 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    if (err < min_error) {
                        min_error = err;
                        best = q;
                    }
                }
                pixel_indices = EtcTables.scramble[best];

                pixel_indices_MSB = EtcUtils.putBits(pixel_indices_MSB, (pixel_indices >> 1), 1, i);
                pixel_indices_LSB = EtcUtils.putBits(pixel_indices_LSB, (pixel_indices & 1), 1, i);

                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.
                sum_error += min_error;
            }
        }

        pixel_indices_MSBp[0] = pixel_indices_MSB;
        pixel_indices_LSBp[0] = pixel_indices_LSB;
        return sum_error;
    }

    private static int tryalltables_3bittable2x4(byte[] img, int width, int height, int startx, int starty, int[] avg_color, int[] best_table, int[] best_pixel_indices_MSB, int[] best_pixel_indices_LSB) {
        int min_error = 3 * 255 * 255 * 16;
        int q;
        int err;
        int[] pixel_indices_MSB = new int[1];
        int[] pixel_indices_LSB = new int[1];

        for (q = 0; q < 16; q += 2)  // try all the 8 tables.
        {
            err = compressBlockWithTable2x4(img, width, height, startx, starty, avg_color, q, pixel_indices_MSB, pixel_indices_LSB);

            if (err < min_error) {
                min_error = err;
                best_pixel_indices_MSB[0] = pixel_indices_MSB[0];
                best_pixel_indices_LSB[0] = pixel_indices_LSB[0];
                best_table[0] = q >> 1;
            }
        }
        return min_error;
    }

    private static int tryalltables_3bittable4x2(byte[] img, int width, int height, int startx, int starty, int[] avg_color, int[] best_table, int[] best_pixel_indices_MSB, int[] best_pixel_indices_LSB) {
        int min_error = 3 * 255 * 255 * 16;
        int q;
        int err;
        int[] pixel_indices_MSB = new int[1];
        int[] pixel_indices_LSB = new int[1];

        for (q = 0; q < 16; q += 2)  // try all the 8 tables.
        {
            err = compressBlockWithTable4x2(img, width, height, startx, starty, avg_color, q, pixel_indices_MSB, pixel_indices_LSB);

            if (err < min_error) {
                min_error = err;
                best_pixel_indices_MSB[0] = pixel_indices_MSB[0];
                best_pixel_indices_LSB[0] = pixel_indices_LSB[0];
                best_table[0] = q >> 1;
            }
        }
        return min_error;
    }

    private static int compressBlockWithTable4x2(byte[] img, int width, int height, int startx, int starty, int[] avg_color, int table, int[] pixel_indices_MSBp, int[] pixel_indices_LSBp) {
        int[] orig = new int[3];
        int[] approx = new int[3];
        int pixel_indices_MSB = 0;
        int pixel_indices_LSB = 0;
        int pixel_indices = 0;
        int sum_error = 0;
        int q;
        int i;

        i = 0;
        for (int x = startx; x < startx + 4; x++) {
            for (int y = starty; y < starty + 2; y++) {
                int err;
                int best = 0;
                int min_error = 255 * 255 * 3 * 16;
                orig[0] = EtcUtils.RED(img, width, x, y);
                orig[1] = EtcUtils.GREEN(img, width, x, y);
                orig[2] = EtcUtils.BLUE(img, width, x, y);

                for (q = 0; q < 4; q++) {
                    approx[0] = EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][q], 255);
                    approx[1] = EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][q], 255);
                    approx[2] = EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][q], 255);

                    // Here we just use equal weights to R, G and B. Although this will
                    // give visually worse results, it will give a better PSNR score.
                    err = EtcUtils.square(approx[0] - orig[0]) + EtcUtils.square(approx[1] - orig[1]) + EtcUtils.square(approx[2] - orig[2]);
                    if (err < min_error) {
                        min_error = err;
                        best = q;
                    }
                }
                pixel_indices = EtcTables.scramble[best];

                pixel_indices_MSB = EtcUtils.putBits(pixel_indices_MSB, (pixel_indices >> 1), 1, i);
                pixel_indices_LSB = EtcUtils.putBits(pixel_indices_LSB, (pixel_indices & 1), 1, i);
                i++;

                // In order to simplify hardware, the table {-12, -4, 4, 12} is indexed {11, 10, 00, 01}
                // so that first bit is sign bit and the other bit is size bit (4 or 12).
                // This means that we have to scramble the bits before storing them.

                sum_error += min_error;
            }
            i += 2;
        }

        pixel_indices_MSBp[0] = pixel_indices_MSB;
        pixel_indices_LSBp[0] = pixel_indices_LSB;

        return sum_error;
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
                        int diff = EtcTables.compressParamsFast[mult];
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