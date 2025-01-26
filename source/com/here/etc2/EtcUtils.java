package com.here.etc2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EtcUtils {

    // Constants (using Java equivalents)
    public static final double PERCEPTUAL_WEIGHT_R_SQUARED = 0.299;
    public static final double PERCEPTUAL_WEIGHT_G_SQUARED = 0.587;
    public static final double PERCEPTUAL_WEIGHT_B_SQUARED = 0.114;

    public static final int PERCEPTUAL_WEIGHT_R_SQUARED_TIMES1000 = 299;
    public static final int PERCEPTUAL_WEIGHT_G_SQUARED_TIMES1000 = 587;
    public static final int PERCEPTUAL_WEIGHT_B_SQUARED_TIMES1000 = 114;

    // Helper functions (using Java equivalents)
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clampLeftZero(int x) {
        return (~(x >> 31)) & x;
    }

    public static int clampRight255(int x) {
        return (((((x) << 23) >> 31)) | (x)) & 0x000000ff;
    }

    public static int square(int x) {
        return x * x;
    }
    public static float square(float x) {
        return x * x;
    }

    public static int jasRound(double x) {
        return (x < 0.0) ? (int) (x - 0.5) : (int) (x + 0.5);
    }

    public static int jasMin(int a, int b) {
        return Math.min(a, b);
    }

    public static int jasMax(int a, int b) {
        return Math.max(a, b);
    }

    // Image access macros (using Java equivalents)
    public static int RED(byte[] img, int width, int x, int y) {
        return img[3 * (y * width + x) + 0] & 0xFF; // & 0xFF to treat byte as unsigned
    }

    public static int GREEN(byte[] img, int width, int x, int y) {
        return img[3 * (y * width + x) + 1] & 0xFF;
    }

    public static int BLUE(byte[] img, int width, int x, int y) {
        return img[3 * (y * width + x) + 2] & 0xFF;
    }

    public static void setRedChannel(byte[] img, int width, int x, int y, int channels, int value) {
        img[channels * (y * width + x) + 0] = (byte) value;
    }

    public static void setGreenChannel(byte[] img, int width, int x, int y, int channels, int value) {
        img[channels * (y * width + x) + 1] = (byte) value;
    }

    public static void setBlueChannel(byte[] img, int width, int x, int y, int channels, int value) {
        img[channels * (y * width + x) + 2] = (byte) value;
    }

    // Bit manipulation macros (using Java equivalents)
    public static int shift(int size, int startpos) {
        return (startpos) - (size) + 1;
    }

    public static int mask(int size, int startpos) {
        return (((2 << (size - 1)) - 1) << shift(size, startpos));
    }

    public static int putBits(int dest, int data, int size, int startpos) {
        int mask = (((2 << (size - 1)) - 1) << ((startpos) - (size) + 1));
        return (dest & ~mask) | ((data << ((startpos) - (size) + 1)) & mask);
    }

    public static int shiftHigh(int size, int startpos) {
        return (startpos - 32) - (size) + 1;
    }

    public static int maskHigh(int size, int startpos) {
        return (((2 << (size - 1)) - 1) << shiftHigh(size, startpos));
    }

    public static int putBitsHigh(int dest, int data, int size, int startpos) {
        return (dest & ~maskHigh(size, startpos)) | ((data << shiftHigh(size, startpos)) & maskHigh(size, startpos));
    }

    public static int getBitsHigh(int input, int size, int startpos) {
        return (int) ((input & maskHigh(size, startpos)) >>> shiftHigh(size, startpos));
    }

    public static int getBits(int input, int size, int startpos) {
        return (input & mask(size, startpos)) >>> shift(size, startpos);
    }

    // Function to read a big-endian 2-byte word
    public static void readBigEndian2ByteWord(short[] blockadr, RandomAccessFile f) throws IOException {
        byte[] bytes = new byte[2];
        f.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        blockadr[0] = buffer.getShort();
    }

    // Function to read a big-endian 4-byte word
    public static void readBigEndian4ByteWord(int[] blockadr, RandomAccessFile f) throws IOException {
        byte[] bytes = new byte[4];
        f.read(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        blockadr[0] = buffer.getInt();
    }

    // Expand source image so that it is divisible by a factor of four in the x-dimension.
    public static boolean expandToWidthDivByFour(byte[] img, int width, int height, int[] expandedWidth, int[] expandedHeight, int bitrate) {
        int xx, yy;
        byte[] newImg;

        if (width % 4 != 0) {
            expandedWidth[0] = (width / 4 + 1) * 4;
            expandedHeight[0] = height;
            newImg = new byte[3 * expandedWidth[0] * expandedHeight[0] * bitrate / 8];

            for (yy = 0; yy < height; yy++) {
                for (xx = 0; xx < width; xx++) {
                    newImg[3 * (yy * expandedWidth[0] + xx) + 0] = img[3 * (yy * width + xx) + 0];
                    newImg[3 * (yy * expandedWidth[0] + xx) + 1] = img[3 * (yy * width + xx) + 1];
                    newImg[3 * (yy * expandedWidth[0] + xx) + 2] = img[3 * (yy * width + xx) + 2];
                }
                // Pad the rest of the line with the last pixel
                for (xx = width; xx < expandedWidth[0]; xx++) {
                    newImg[3 * (yy * expandedWidth[0] + xx) + 0] = img[3 * (yy * width + width - 1) + 0];
                    newImg[3 * (yy * expandedWidth[0] + xx) + 1] = img[3 * (yy * width + width - 1) + 1];
                    newImg[3 * (yy * expandedWidth[0] + xx) + 2] = img[3 * (yy * width + width - 1) + 2];
                }
            }
            System.out.println("Image width expanded to " + expandedWidth[0]);
            return true;
        } else {
            System.out.println("Image width already divisible by four");
            expandedWidth[0] = width;
            expandedHeight[0] = height;
            return false;
        }
    }

    // Expand source image so that it is divisible by a factor of four in the y-dimension.
    public static boolean expandToHeightDivByFour(byte[] img, int width, int height, int[] expandedWidth, int[] expandedHeight, int bitrate) {
        int hdiv4;
        int xx, yy;
        int numLinesMissing;
        byte[] newImg;

        hdiv4 = height / 4;

        if (hdiv4 * 4 != height) {
            expandedWidth[0] = width;
            expandedHeight[0] = (hdiv4 + 1) * 4;
            numLinesMissing = expandedHeight[0] - height;
            newImg = new byte[3 * expandedWidth[0] * expandedHeight[0] * bitrate / 8];
            if (newImg == null) {
                System.out.println("Could not allocate memory to expand height");
                return false;
            }

            // First copy image. No need to reformat data.
            System.arraycopy(img, 0, newImg, 0, 3 * width * height * bitrate / 8);

            // Then copy up to three lines.
            for (yy = height; yy < height + numLinesMissing; yy++) {
                for (xx = 0; xx < width; xx++) {
                    for (int i = 0; i < 3 * bitrate / 8; i++) {
                        newImg[(yy * width + xx) * 3 * bitrate / 8 + i] = img[((height - 1) * width + xx) * 3 * bitrate / 8 + i];
                    }
                }
            }

            // Use the new image:
            img = newImg;
            return true;

        } else {
            System.out.println("Image height already divisible by four.");
            expandedWidth[0] = width;
            expandedHeight[0] = height;
            return true;
        }
    }

    public static double calcBlockErrorRGB(byte[] img, byte[] imgdec, int width, int height, int startx, int starty) {
        double err = 0;

        for (int xx = startx; xx < startx + 4; xx++) {
            for (int yy = starty; yy < starty + 4; yy++) {
                err += EtcUtils.square(1.0f * EtcUtils.RED(img, width, xx, yy) - 1.0f * EtcUtils.RED(imgdec, width, xx, yy));
                err += EtcUtils.square(1.0f * EtcUtils.GREEN(img, width, xx, yy) - 1.0f * EtcUtils.GREEN(imgdec, width, xx, yy));
                err += EtcUtils.square(1.0f * EtcUtils.BLUE(img, width, xx, yy) - 1.0f * EtcUtils.BLUE(imgdec, width, xx, yy));
            }
        }

        return err;
    }

    public static double calcBlockErrorRGBA(byte[] img, byte[] imgdec, byte[] alpha, int width, int height, int startx, int starty) {
        int xx, yy;
        double err = 0;

        for (xx = startx; xx < startx + 4; xx++) {
            for (yy = starty; yy < starty + 4; yy++) {
                // Only count non-transparent pixels
                if (alpha[yy * width + xx] > 128) {
                    err += EtcUtils.square(1.0f * EtcUtils.RED(img, width, xx, yy) - 1.0f * EtcUtils.RED(imgdec, width, xx, yy));
                    err += EtcUtils.square(1.0f * EtcUtils.GREEN(img, width, xx, yy) - 1.0f * EtcUtils.GREEN(imgdec, width, xx, yy));
                    err += EtcUtils.square(1.0f * EtcUtils.BLUE(img, width, xx, yy) - 1.0f * EtcUtils.BLUE(imgdec, width, xx, yy));
                }
            }
        }
        return err;
    }
    public static boolean hasAlpha(byte[] alphaimg, int ix, int iy, int width) {
        for (int x = ix; x < ix + 4; x++) {
            for (int y = iy; y < iy + 4; y++) {
                int index = x + y * width;
                if (alphaimg[index] < 128) {
                    return true;
                }
            }
        }
        return false;
    }
} 