package com.here.etc2;

public class EtcETC2Block {

    // Compress a block using ETC2 fast mode.
    public static void compressBlockETC2Fast(int format, byte[] img, byte[] alphaimg, byte[] imgdec, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
        int etc1_word1 = 0;
        int etc1_word2 = 0;
        double error_etc1;

        int planar57_word1;
        int planar57_word2;
        int planar_word1;
        int planar_word2;
        double error_planar;

        int thumbT59_word1;
        int thumbT59_word2;
        int thumbT_word1;
        int thumbT_word2;
        double error_thumbT;

        int thumbH58_word1;
        int thumbH58_word2;
        int thumbH_word1;
        int thumbH_word2;
        double error_thumbH;

        double error_best;
        char best_char;
        int best_mode;

        int[] tempword1 = new int[1];
        int[] tempword2 = new int[1];
        if (format == EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS || format == EtcConstants.ETC2PACKAGE_sRGBA1_NO_MIPMAPS) {
            // If we have one-bit alpha, we never use the individual mode,
            // instead that bit flags that one of our four offsets will instead
            // mean transparent (with 0 offset for color channels)

            // The regular ETC individual mode is disabled, but the old T, H and planar modes
            // are kept unchanged and may be used for blocks without transparency.
            // Introduced are old ETC with only differential coding,
            // ETC differential but with 3 offsets and transparent,
            // and T-mode with 3 colors plus transparent.

            // In a fairly hackish manner, error_etc1, etc1_word1 and etc1_word2 will
            // represent the best out of the three introduced modes, to be compared
            // with the three kept modes in the old code


            double temperror;

            // Try regular differential transparent mode
            int testerr = compressBlockDifferentialWithAlpha(true, img, alphaimg, imgdec, width, height, startx, starty, tempword1, tempword2);

            byte[] alphadec = new byte[width * height];
            Decode.decompressBlockDifferentialWithAlpha(tempword1[0], tempword2[0], imgdec, alphadec, width, height, startx, starty);
            error_etc1 = EtcUtils.calcBlockErrorRGBA(img, imgdec, alphaimg, width, height, startx, starty);
            if (error_etc1 != testerr) {
                System.out.println("testerr: " + testerr + ", etcerr: " + error_etc1);
            }

            /*  // ----- Disable unimplemented functions -----  

            // Try T-mode with transparencies
            // For now, skip this...
            compressBlockTHUMB59TAlpha(img, alphaimg, width, height, startx, starty, tempword1, tempword2);
            decompressBlockTHUMB59TAlpha(tempword1[0], tempword2[0], imgdec, alphadec, width, height, startx, starty);
            temperror = calcBlockErrorRGBA(img, imgdec, alphaimg, width, height, startx, starty);
            if (temperror < error_etc1) {
                error_etc1 = temperror;
                stuff59bitsDiffFalse(tempword1[0], tempword2[0], tempword1, tempword2);
            }

            compressBlockTHUMB58HAlpha(img, alphaimg, width, height, startx, starty, tempword1, tempword2);
            decompressBlockTHUMB58HAlpha(tempword1[0], tempword2[0], imgdec, alphadec, width, height, startx, starty);
            temperror = calcBlockErrorRGBA(img, imgdec, alphaimg, width, height, startx, starty);
            if (temperror < error_etc1) {
                error_etc1 = temperror;
                stuff58bitsDiffFalse(tempword1[0], tempword2[0], tempword1, tempword2);
            }
            */
            // If we have transparency in this pixel, we know that one of these two modes was best..
            if (EtcUtils.hasAlpha(alphaimg, startx, starty, width)) {
                compressed1[0] = tempword1[0];
                compressed2[0] = tempword2[0];
                return;
            }

            // Otherwise, they MIGHT have been the best, although that's unlikely.. anyway, try old differential mode now
            compressBlockDifferentialWithAlpha(false, img, alphaimg, imgdec, width, height, startx, starty, tempword1, tempword2);
            Decode.decompressBlockDiffFlip(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
            temperror = EtcUtils.calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
            //This doesn't seem to be needed since afterwards there is no block error calculation.
            //decompressBlockDifferentialWithAlpha(tempword1[0], tempword2[0], imgdec, alphadec, width, height, startx, starty);
            if (temperror < error_etc1) {
                error_etc1 = temperror;
                etc1_word1 = tempword1[0];
                etc1_word2 = tempword2[0];
            }
        } else {
            // This includes individual mode, and therefore doesn't apply in case of punch-through alpha.
            EtcDiffFlipBlock.compressBlockDiffFlipFast(img, imgdec, width, height, startx, starty, tempword1, tempword2);
            Decode.decompressBlockDiffFlip(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
            error_etc1 = EtcUtils.calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
            etc1_word1 = tempword1[0];
            etc1_word2 = tempword2[0];
        }

        error_best = error_etc1;
        compressed1[0] = etc1_word1;
        compressed2[0] = etc1_word2;
        best_char = '.';
        best_mode = EtcConstants.MODE_ETC1;

        /*  // ----- Disable unimplemented functions -----  
        // These modes apply regardless of whether we want punch-through alpha or not.
        // error etc_1 and etc1_word1/etc1_word2 contain previous best candidate.
        compressBlockPlanar57(img, width, height, startx, starty, tempword1, tempword2);
        decompressBlockPlanar57(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
        error_planar = calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
        stuff57bits(tempword1[0], tempword2[0], planar_word1, planar_word2);

        compressBlockTHUMB59TFastest(img, width, height, startx, starty, tempword1, tempword2);
        decompressBlockTHUMB59T(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
        error_thumbT = calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
        stuff59bits(tempword1[0], tempword2[0], thumbT_word1, thumbT_word2);

        compressBlockTHUMB58HFastest(img, width, height, startx, starty, tempword1, tempword2);
        decompressBlockTHUMB58H(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
        error_thumbH = calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
        stuff58bits(tempword1[0], tempword2[0], thumbH_word1, thumbH_word2);



        if (error_planar < error_best) {
            compressed1[0] = planar_word1;
            compressed2[0] = planar_word2;
            best_char = 'p';
            error_best = error_planar;
            best_mode = MODE_PLANAR;
        }
        if (error_thumbT < error_best) {
            compressed1[0] = thumbT_word1;
            compressed2[0] = thumbT_word2;
            best_char = 'T';
            error_best = error_thumbT;
            best_mode = MODE_THUMB_T;
        }
        if (error_thumbH < error_best) {
            compressed1[0] = thumbH_word1;
            compressed2[0] = thumbH_word2;
            best_char = 'H';
            error_best = error_thumbH;
            best_mode = MODE_THUMB_H;
        }

        switch (best_mode) {
            // Now see which mode won and compress that a little bit harder
            case MODE_THUMB_T:
                compressBlockTHUMB59TFast(img, width, height, startx, starty, tempword1, tempword2);
                decompressBlockTHUMB59T(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
                error_thumbT = calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
                stuff59bits(tempword1[0], tempword2[0], thumbT_word1, thumbT_word2);
                if (error_thumbT < error_best) {
                    compressed1[0] = thumbT_word1;
                    compressed2[0] = thumbT_word2;
                }
                break;
            case MODE_THUMB_H:
                compressBlockTHUMB58HFast(img, width, height, startx, starty, tempword1, tempword2);
                decompressBlockTHUMB58H(tempword1[0], tempword2[0], imgdec, width, height, startx, starty);
                error_thumbH = calcBlockErrorRGB(img, imgdec, width, height, startx, starty);
                stuff58bits(tempword1[0], tempword2[0], thumbH_word1, thumbH_word2);
                if (error_thumbH < error_best) {
                    compressed1[0] = thumbH_word1;
                    compressed2[0] = thumbH_word2;
                }
                break;
            default:
                break;
        }
        */
    }

    private static int compressBlockDifferentialWithAlpha(boolean isTransparent, byte[] img, byte[] alphaimg, byte[] imgdec,
        int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
    
        int compressed1_norm = 0, compressed2_norm = 0;
        int compressed1_flip = 0, compressed2_flip = 0;
        int compressed1_temp = 0, compressed2_temp = 0;
        int[] avg_color_quant1 = new int[3];
        int[] avg_color_quant2 = new int[3];

        float[] avg_color_float1 = new float[3];
        float[] avg_color_float2 = new float[3];
        int[] enc_color1 = new int[3];
        int[] enc_color2 = new int[3];
        int[] diff = new int[3];
        int min_error = 255 * 255 * 8 * 3;

        int norm_err = 0;
        int flip_err = 0;
        int temp_err = 0;

        for (int flipbit = 0; flipbit < 2; flipbit++) {
            // Compute average color for each half.
            for (int c = 0; c < 3; c++) {
                avg_color_float1[c] = 0;
                avg_color_float2[c] = 0;
                float sum1 = 0;
                float sum2 = 0;
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        float fac = 1;
                        int index = x + startx + (y + starty) * width;
                        // Transparent pixels are only barely figured into the average.
                        if (alphaimg[index] < 128) {
                            fac = 0.0001f;
                        }
                        float col = fac * (img[index * 3 + c] & 0xFF);
                        if ((flipbit == 0 && x < 2) || (flipbit == 1 && y < 2)) {
                            sum1 += fac;
                            avg_color_float1[c] += col;
                        } else {
                            sum2 += fac;
                            avg_color_float2[c] += col;
                        }
                    }
                }
                avg_color_float1[c] /= sum1;
                avg_color_float2[c] /= sum2;
            }

            int[] dummy = new int[3];
            EtcDiffFlipBlock.quantize555ColorCombined(avg_color_float1, enc_color1, dummy);
            EtcDiffFlipBlock.quantize555ColorCombined(avg_color_float2, enc_color2, dummy);

            diff[0] = enc_color2[0] - enc_color1[0];
            diff[1] = enc_color2[1] - enc_color1[1];
            diff[2] = enc_color2[2] - enc_color1[2];

            // Make sure diff is small enough for diff-coding
            for (int c = 0; c < 3; c++) {
                if (diff[c] < -4) diff[c] = -4;
                if (diff[c] > 3) diff[c] = 3;
                enc_color2[c] = enc_color1[c] + diff[c];
            }

            avg_color_quant1[0] = (enc_color1[0] << 3) | (enc_color1[0] >> 2);
            avg_color_quant1[1] = (enc_color1[1] << 3) | (enc_color1[1] >> 2);
            avg_color_quant1[2] = (enc_color1[2] << 3) | (enc_color1[2] >> 2);
            avg_color_quant2[0] = (enc_color2[0] << 3) | (enc_color2[0] >> 2);
            avg_color_quant2[1] = (enc_color2[1] << 3) | (enc_color2[1] >> 2);
            avg_color_quant2[2] = (enc_color2[2] << 3) | (enc_color2[2] >> 2);

            // Pack bits into the first word.
            compressed1_temp = 0;
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, !isTransparent ? 1 : 0, 1, 33);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, enc_color1[0], 5, 63);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, enc_color1[1], 5, 55);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, enc_color1[2], 5, 47);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, diff[0], 3, 58);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, diff[1], 3, 50);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, diff[2], 3, 42);

            temp_err = 0;

            int[] besterror = new int[]{255 * 255 * 3 * 16, 255 * 255 * 3 * 16};
            int[] besttable = new int[2];
            int[] best_indices_LSB = new int[16];
            int[] best_indices_MSB = new int[16];

            for (int table = 0; table < 8; table++) {
                int[] taberror = new int[2];
                int[] pixel_indices_LSB = new int[16];
                int[] pixel_indices_MSB = new int[16];

                for (int i = 0; i < 2; i++) {
                    taberror[i] = 0;
                }

                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        int index = x + startx + (y + starty) * width;
                        int[] basecol = new int[3];
                        boolean transparentPixel = alphaimg[index] < 128;
                        int half = 0;
                        if ((flipbit == 0 && x < 2) || (flipbit != 0 && y < 2)) {
                            basecol[0] = avg_color_quant1[0];
                            basecol[1] = avg_color_quant1[1];
                            basecol[2] = avg_color_quant1[2];
                        } else {
                            half = 1;
                            basecol[0] = avg_color_quant2[0];
                            basecol[1] = avg_color_quant2[1];
                            basecol[2] = avg_color_quant2[2];
                        }

                        int besterri = 255 * 255 * 3 * 2;
                        int besti = 0;
                        int erri;
                        for (int i = 0; i < 4; i++) {
                            if (i == 1 && isTransparent) continue;
                            erri = 0;
                            for (int c = 0; c < 3; c++) {
                                int col = EtcUtils.clamp(0, basecol[c] + EtcTables.compressParams[table * 2][i], 255);
                                if (i == 2 && isTransparent) {
                                    col = basecol[c];
                                }
                                int errcol = col - (img[index * 3 + c] & 0xFF);
                                erri += (errcol * errcol);
                            }
                            if (erri < besterri) {
                                besterri = erri;
                                besti = i;
                            }
                        }
                        if (transparentPixel) {
                            besterri = 0;
                            besti = 1;
                        }

                        taberror[half] += besterri;

                        int pixel_index = EtcTables.scramble[besti];
                        pixel_indices_MSB[x * 4 + y] = (pixel_index >> 1);
                        pixel_indices_LSB[x * 4 + y] = (pixel_index & 1);
                    }
                }

                for (int half = 0; half < 2; half++) {
                    if (taberror[half] < besterror[half]) {
                        besterror[half] = taberror[half];
                        besttable[half] = table;
                        for (int i = 0; i < 16; i++) {
                            int thishalf = 0;
                            int y = i % 4;
                            int x = i / 4;
                            if (!((flipbit == 0 && x < 2) || (flipbit != 0 && y < 2))) {
                                thishalf = 1;
                            }
                            if (half != thishalf) continue;
                            best_indices_MSB[i] = pixel_indices_MSB[i];
                            best_indices_LSB[i] = pixel_indices_LSB[i];
                        }
                    }
                }
            }

            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, besttable[0], 3, 39);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, besttable[1], 3, 36);
            compressed1_temp = EtcUtils.putBitsHigh(compressed1_temp, 0, 1, 32);

            compressed2_temp = 0;
            for (int i = 0; i < 16; i++) {
                compressed2_temp = EtcUtils.putBits(compressed2_temp, best_indices_MSB[i], 1, 16 + i);
                compressed2_temp = EtcUtils.putBits(compressed2_temp, best_indices_LSB[i], 1, i);
            }

            if (flipbit != 0) {
                flip_err = besterror[0] + besterror[1];
                compressed1_flip = compressed1_temp;
                compressed2_flip = compressed2_temp;
            } else {
                norm_err = besterror[0] + besterror[1];
                compressed1_norm = compressed1_temp;
                compressed2_norm = compressed2_temp;
            }
        }

        if (norm_err <= flip_err) {
            compressed1[0] = compressed1_norm | 0;
            compressed2[0] = compressed2_norm;
            return norm_err;
        } else {
            compressed1[0] = compressed1_flip | 1;
            compressed2[0] = compressed2_flip;
            return flip_err;
        }
    }

    // Compress a block using ETC2 fast mode with perceptual error.
    public static void compressBlockETC2FastPerceptual(byte[] img, byte[] imgdec, int width, int height, int startx, int starty, int[] compressed1, int[] compressed2) {
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

                        int index = EtcTables.table58H[p];
                        int diff = EtcTables.compressParamsFast[d];

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

        compressed1[0] = block_part1;
        compressed2[0] = block_part2;
    }
} 