package com.here.etc2;

public class Decode{

    private static void decompressBlockDiffFlipC(int block_part1, int block_part2, byte[] img, int width, int height, int startx, int starty, int channels) {
        int[] avg_color = new int[3];
        int[] enc_color1 = new int[3];
        int[] enc_color2 = new int[3];
        byte[] diff = new byte[3];
        int table;
        int index, shift;
        int r, g, b;
        int diffbit;
        int flipbit;

        diffbit = EtcUtils.getBitsHigh(block_part1, 1, 33);
        flipbit = EtcUtils.getBitsHigh(block_part1, 1, 32);

        if (diffbit == 0) {
            // We have diffbit = 0.

            // First decode left part of block.
            avg_color[0] = EtcUtils.getBitsHigh(block_part1, 4, 63);
            avg_color[1] = EtcUtils.getBitsHigh(block_part1, 4, 55);
            avg_color[2] = EtcUtils.getBitsHigh(block_part1, 4, 47);

            // Here, we should really multiply by 17 instead of 16. This can
            // be done by just copying the four lower bits to the upper ones
            // while keeping the lower bits.
            avg_color[0] |= (avg_color[0] << 4);
            avg_color[1] |= (avg_color[1] << 4);
            avg_color[2] |= (avg_color[2] << 4);

            table = EtcUtils.getBitsHigh(block_part1, 3, 39) << 1;

            int pixel_indices_MSB, pixel_indices_LSB;

            pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
            pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

            if (flipbit == 0) {
                // We should not flip
                shift = 0;
                for (int x = startx; x < startx + 2; x++) {
                    for (int y = starty; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                }
            } else {
                // We should flip
                shift = 0;
                for (int x = startx; x < startx + 4; x++) {
                    for (int y = starty; y < starty + 2; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                    shift += 2;
                }
            }

            // Now decode other part of block.
            avg_color[0] = EtcUtils.getBitsHigh(block_part1, 4, 59);
            avg_color[1] = EtcUtils.getBitsHigh(block_part1, 4, 51);
            avg_color[2] = EtcUtils.getBitsHigh(block_part1, 4, 43);

            // Here, we should really multiply by 17 instead of 16. This can
            // be done by just copying the four lower bits to the upper ones
            // while keeping the lower bits.
            avg_color[0] |= (avg_color[0] << 4);
            avg_color[1] |= (avg_color[1] << 4);
            avg_color[2] |= (avg_color[2] << 4);

            table = EtcUtils.getBitsHigh(block_part1, 3, 36) << 1;
            pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
            pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

            if (flipbit == 0) {
                // We should not flip
                shift = 8;
                for (int x = startx + 2; x < startx + 4; x++) {
                    for (int y = starty; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                }
            } else {
                // We should flip
                shift = 2;
                for (int x = startx; x < startx + 4; x++) {
                    for (int y = starty + 2; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                    shift += 2;
                }
            }
        } else {
            // We have diffbit = 1.

            // First decode left part of block.
            enc_color1[0] = EtcUtils.getBitsHigh(block_part1, 5, 63);
            enc_color1[1] = EtcUtils.getBitsHigh(block_part1, 5, 55);
            enc_color1[2] = EtcUtils.getBitsHigh(block_part1, 5, 47);

            // Expand from 5 to 8 bits
            avg_color[0] = (enc_color1[0] << 3) | (enc_color1[0] >> 2);
            avg_color[1] = (enc_color1[1] << 3) | (enc_color1[1] >> 2);
            avg_color[2] = (enc_color1[2] << 3) | (enc_color1[2] >> 2);

            table = EtcUtils.getBitsHigh(block_part1, 3, 39) << 1;

            int pixel_indices_MSB, pixel_indices_LSB;

            pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
            pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

            if (flipbit == 0) {
                // We should not flip
                shift = 0;
                for (int x = startx; x < startx + 2; x++) {
                    for (int y = starty; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                }
            } else {
                // We should flip
                shift = 0;
                for (int x = startx; x < startx + 4; x++) {
                    for (int y = starty; y < starty + 2; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                    shift += 2;
                }
            }

            // Now decode right part of block.
            diff[0] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 58);
            diff[1] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 50);
            diff[2] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 42);

            // Extend sign bit to entire byte.
            diff[0] = (byte) (diff[0] << 5);
            diff[1] = (byte) (diff[1] << 5);
            diff[2] = (byte) (diff[2] << 5);
            diff[0] = (byte) (diff[0] >> 5);
            diff[1] = (byte) (diff[1] >> 5);
            diff[2] = (byte) (diff[2] >> 5);

            // Calculate second color
            enc_color2[0] = enc_color1[0] + diff[0];
            enc_color2[1] = enc_color1[1] + diff[1];
            enc_color2[2] = enc_color1[2] + diff[2];

            // Expand from 5 to 8 bits
            avg_color[0] = (enc_color2[0] << 3) | (enc_color2[0] >> 2);
            avg_color[1] = (enc_color2[1] << 3) | (enc_color2[1] >> 2);
            avg_color[2] = (enc_color2[2] << 3) | (enc_color2[2] >> 2);

            table = EtcUtils.getBitsHigh(block_part1, 3, 36) << 1;
            pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
            pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

            if (flipbit == 0) {
                // We should not flip
                shift = 8;
                for (int x = startx + 2; x < startx + 4; x++) {
                    for (int y = starty; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                }
            } else {
                // We should flip
                shift = 2;
                for (int x = startx; x < startx + 4; x++) {
                    for (int y = starty + 2; y < starty + 4; y++) {
                        index = ((pixel_indices_MSB >> shift) & 1) << 1;
                        index |= ((pixel_indices_LSB >> shift) & 1);
                        shift++;
                        index = EtcTables.unscramble[index];

                        EtcUtils.setRedChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[0] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setGreenChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[1] + EtcTables.compressParams[table][index], 255));
                        EtcUtils.setBlueChannel(img, width, x, y, channels, EtcUtils.clamp(0, avg_color[2] + EtcTables.compressParams[table][index], 255));
                    }
                    shift += 2;
                }
            }
        }
    }

    public static void decompressBlockDiffFlip(int block_part1, int block_part2, byte[] img, int width, int height, int startx, int starty) {
        decompressBlockDiffFlipC(block_part1, block_part2, img, width, height, startx, starty, 3);
    }

    private static void decompressBlockDifferentialWithAlphaC(int block_part1, int block_part2, byte[] img, byte[] alpha, int width, int height, int startx, int starty, int channelsRGB) {
        int[] avg_color = new int[3];
        int[] enc_color1 = new int[3];
        int[] enc_color2 = new int[3];
        byte[] diff = new byte[3];
        int table;
        int index, shift;
        int r, g, b;
        int diffbit;
        int flipbit;
        int channelsA;

        if (channelsRGB == 3) {
            // We will decode the alpha data to a separate memory area.
            channelsA = 1;
        } else {
            // We will decode the RGB data and the alpha data to the same memory area,
            // interleaved as RGBA.
            channelsA = 4;
            alpha = img; // Point alpha to the same array as img
        }

        // The diffbit now encodes whether or not the entire alpha channel is 255.
        diffbit = EtcUtils.getBitsHigh(block_part1, 1, 33);
        flipbit = EtcUtils.getBitsHigh(block_part1, 1, 32);

        // First decode left part of block.
        enc_color1[0] = EtcUtils.getBitsHigh(block_part1, 5, 63);
        enc_color1[1] = EtcUtils.getBitsHigh(block_part1, 5, 55);
        enc_color1[2] = EtcUtils.getBitsHigh(block_part1, 5, 47);

        // Expand from 5 to 8 bits
        avg_color[0] = (enc_color1[0] << 3) | (enc_color1[0] >> 2);
        avg_color[1] = (enc_color1[1] << 3) | (enc_color1[1] >> 2);
        avg_color[2] = (enc_color1[2] << 3) | (enc_color1[2] >> 2);

        table = EtcUtils.getBitsHigh(block_part1, 3, 39) << 1;

        int pixel_indices_MSB, pixel_indices_LSB;

        pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
        pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

        if (flipbit == 0) {
            // We should not flip
            shift = 0;
            for (int x = startx; x < startx + 2; x++) {
                for (int y = starty; y < starty + 4; y++) {
                    index = ((pixel_indices_MSB >> shift) & 1) << 1;
                    index |= ((pixel_indices_LSB >> shift) & 1);
                    shift++;
                    index = EtcTables.unscramble[index];

                    int mod = EtcTables.compressParams[table][index];
                    if (diffbit == 0 && (index == 1 || index == 2)) {
                        mod = 0;
                    }

                    EtcUtils.setRedChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[0] + mod, 255));
                    EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[1] + mod, 255));
                    EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[2] + mod, 255));
                    if (diffbit == 0 && index == 1) {
                        alpha[(y * width + x) * channelsA] = 0;
                        EtcUtils.setRedChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, 0);
                    } else {
                        alpha[(y * width + x) * channelsA] = (byte)255;
                    }
                }
            }
        } else {
            // We should flip
            shift = 0;
            for (int x = startx; x < startx + 4; x++) {
                for (int y = starty; y < starty + 2; y++) {
                    index = ((pixel_indices_MSB >> shift) & 1) << 1;
                    index |= ((pixel_indices_LSB >> shift) & 1);
                    shift++;
                    index = EtcTables.unscramble[index];

                    int mod = EtcTables.compressParams[table][index];
                    if (diffbit == 0 && (index == 1 || index == 2)) {
                        mod = 0;
                    }

                    EtcUtils.setRedChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[0] + mod, 255));
                    EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[1] + mod, 255));
                    EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[2] + mod, 255));
                    if (diffbit == 0 && index == 1) {
                        alpha[(y * width + x) * channelsA] = 0;
                        EtcUtils.setRedChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, 0);
                    } else {
                        alpha[(y * width + x) * channelsA] = (byte)255;
                    }
                }
                shift += 2;
            }
        }

        // Now decode right part of block.
        diff[0] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 58);
        diff[1] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 50);
        diff[2] = (byte) EtcUtils.getBitsHigh(block_part1, 3, 42);

        // Extend sign bit to entire byte.
        diff[0] = (byte) (diff[0] << 5);
        diff[1] = (byte) (diff[1] << 5);
        diff[2] = (byte) (diff[2] << 5);
        diff[0] = (byte) (diff[0] >> 5);
        diff[1] = (byte) (diff[1] >> 5);
        diff[2] = (byte) (diff[2] >> 5);

        // Calculate second color
        enc_color2[0] = enc_color1[0] + diff[0];
        enc_color2[1] = enc_color1[1] + diff[1];
        enc_color2[2] = enc_color1[2] + diff[2];

        // Expand from 5 to 8 bits
        avg_color[0] = (enc_color2[0] << 3) | (enc_color2[0] >> 2);
        avg_color[1] = (enc_color2[1] << 3) | (enc_color2[1] >> 2);
        avg_color[2] = (enc_color2[2] << 3) | (enc_color2[2] >> 2);

        table = EtcUtils.getBitsHigh(block_part1, 3, 36) << 1;
        pixel_indices_MSB = EtcUtils.getBits(block_part2, 16, 31);
        pixel_indices_LSB = EtcUtils.getBits(block_part2, 16, 15);

        if (flipbit == 0) {
            // We should not flip
            shift = 8;
            for (int x = startx + 2; x < startx + 4; x++) {
                for (int y = starty; y < starty + 4; y++) {
                    index = ((pixel_indices_MSB >> shift) & 1) << 1;
                    index |= ((pixel_indices_LSB >> shift) & 1);
                    shift++;
                    index = EtcTables.unscramble[index];

                    int mod = EtcTables.compressParams[table][index];
                    if (diffbit == 0 && (index == 1 || index == 2)) {
                        mod = 0;
                    }

                    EtcUtils.setRedChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[0] + mod, 255));
                    EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[1] + mod, 255));
                    EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[2] + mod, 255));
                    if (diffbit == 0 && index == 1) {
                        alpha[(y * width + x) * channelsA] = 0;
                        EtcUtils.setRedChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, 0);
                    } else {
                        alpha[(y * width + x) * channelsA] = (byte)255;
                    }
                }
            }
        } else {
            // We should flip
            shift = 2;
            for (int x = startx; x < startx + 4; x++) {
                for (int y = starty + 2; y < starty + 4; y++) {
                    index = ((pixel_indices_MSB >> shift) & 1) << 1;
                    index |= ((pixel_indices_LSB >> shift) & 1);
                    shift++;
                    index = EtcTables.unscramble[index];

                    int mod = EtcTables.compressParams[table][index];
                    if (diffbit == 0 && (index == 1 || index == 2)) {
                        mod = 0;
                    }

                    EtcUtils.setRedChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[0] + mod, 255));
                    EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[1] + mod, 255));
                    EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, EtcUtils.clamp(0, avg_color[2] + mod, 255));
                    if (diffbit == 0 && index == 1) {
                        alpha[(y * width + x) * channelsA] = 0;
                        EtcUtils.setRedChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setGreenChannel(img, width, x, y, channelsRGB, 0);
                        EtcUtils.setBlueChannel(img, width, x, y, channelsRGB, 0);
                    } else {
                        alpha[(y * width + x) * channelsA] = (byte)255;
                    }
                }
                shift += 2;
            }
        }
    }

    public static void decompressBlockDifferentialWithAlpha(int block_part1, int block_part2, byte[] img, byte[] alpha, int width, int height, int startx, int starty) {
        decompressBlockDifferentialWithAlphaC(block_part1, block_part2, img, alpha, width, height, startx, starty, 3);
    }
}

