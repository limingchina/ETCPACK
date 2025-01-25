package com.here.etc2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class etcpack {

    public static void main(String[] args) throws IOException {
        String[] src = new String[1];
        String[] dst = new String[1];
        int[] mode = new int[1];
        int[] speed = new int[1];
        int[] metric = new int[1];
        int[] codec = new int[1];
        int[] fileFormat = new int[1];
        boolean[] verbose = new boolean[1];
        int[] formatSigned = new int[1];
        boolean[] ktxFile = new boolean[1];
        EtcFile.readArguments(args, src, dst, mode, speed, metric, codec, fileFormat, verbose, formatSigned, ktxFile);
        System.out.println("Source file: " + src[0]);
        System.out.println("Destination file: " + dst[0]);
        System.out.println("Mode: " + mode[0]);
        System.out.println("Speed: " + speed[0]);
        System.out.println("Metric: " + metric[0]);
        System.out.println("Codec: " + codec[0]);
        System.out.println("Format: " + fileFormat[0]);
        System.out.println("Verbose: " + verbose[0]);
        System.out.println("Format Signed: " + formatSigned[0]);
        System.out.println("ktxFile: " + ktxFile[0]);

        File inputFile = new File(src[0]);
        RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
        int width = 0;
        int height = 0;
        byte[] img;
        byte[] alphaimg = null;
        int[] expandedWidth = new int[1];
        int[] expandedHeight = new int[1];
        int[] compressed1 = new int[1];
        int[] compressed2 = new int[1];
        byte[] alphaData = new byte[16];

        if (fileFormat[0] == source.EtcTables.ETC1_RGB_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_RGB_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_sRGB_NO_MIPMAPS) {
            if (fileFormat[0] == source.EtcTables.ETC1_RGB_NO_MIPMAPS) {
                EtcFile.readPPM(raf, width, height, img);
            } else {
                EtcFile.readKTX(raf, width, height, img);
            }
            if (EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 24)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 24);
                width = expandedWidth[0];
                height = expandedHeight[0];
            }
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    if (codec[0] == source.EtcTables.CODEC_ETC) {
                        if (speed[0] == source.EtcTables.SPEED_FAST) {
                            EtcDiffFlipBlock.compressBlockDiffFlipFast(img, null, width, height, x, y, compressed1, compressed2);
                        }
                    } else if (codec[0] == source.EtcTables.CODEC_ETC2) {
                        if (speed[0] == source.EtcTables.SPEED_FAST) {
                            EtcETC2Block.compressBlockETC2Fast(img, null, null, width, height, x, y, compressed1, compressed2);
                        }
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], dst[0], fileFormat[0]);
                }
            }
        } else if (fileFormat[0] == source.EtcTables.ETC2PACKAGE_RGBA_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_sRGBA_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
            EtcFile.readKTX(raf, width, height, img, alphaimg);
            if (EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 32)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                alphaimg = new byte[expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 32);
                EtcUtils.expandToWidthDivByFour(alphaimg, width, height, expandedWidth, expandedHeight, 8);
                width = expandedWidth[0];
                height = expandedHeight[0];
            }
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    if (speed[0] == source.EtcTables.SPEED_FAST) {
                        EtcETC2Block.compressBlockETC2Fast(img, alphaimg, null, width, height, x, y, compressed1, compressed2);
                        EtcAlphaBlock.compressBlockAlphaFast(alphaimg, x, y, width, height, alphaData);
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], dst[0], fileFormat[0]);
                    EtcFile.writeCompressedAlphaBlock(alphaData, dst[0], fileFormat[0]);
                }
            }
        } else if (fileFormat[0] == source.EtcTables.ETC2PACKAGE_R_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_RG_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_R_SIGNED_NO_MIPMAPS || fileFormat[0] == source.EtcTables.ETC2PACKAGE_RG_SIGNED_NO_MIPMAPS) {
            EtcFile.readKTX(raf, width, height, img);
            if (EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 24)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width, height, expandedWidth, expandedHeight, 24);
                width = expandedWidth[0];
                height = expandedHeight[0];
            }
            for (int y = 0; y < height; y += 4) {
                for (int x = 0; x < width; x += 4) {
                    if (speed[0] == source.EtcTables.SPEED_FAST) {
                        EtcETC2Block.compressBlockETC2Fast(img, null, null, width, height, x, y, compressed1, compressed2);
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], dst[0], fileFormat[0]);
                }
            }
        }
        raf.close();
    }
} 