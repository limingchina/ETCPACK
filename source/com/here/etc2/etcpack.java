package com.here.etc2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class etcpack {
        public static void main(String[] args) throws IOException {
       
        Arguments arguments = EtcFile.readArguments(args);
        System.out.println("Source file: " + arguments.src);
        System.out.println("Destination file: " + arguments.dst);
        System.out.println("Mode: " + arguments.mode);
        System.out.println("Speed: " + arguments.speed);
        System.out.println("Metric: " + arguments.metric);
        System.out.println("Codec: " + arguments.codec);
        System.out.println("Format: " + arguments.fileFormat);
        System.out.println("Verbose: " + arguments.verbose);
        System.out.println("Format Signed: " + arguments.formatSigned);
        System.out.println("ktxFile: " + arguments.ktxFile);

        File inputFile = new File(arguments.src);
        RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
        int[] width = new int[]{0};
        int[] height = new int[]{0};
        byte[] img = null;
        byte[] alphaimg = null;
        int[] expandedWidth = new int[1];
        int[] expandedHeight = new int[1];
        int[] compressed1 = new int[1];
        int[] compressed2 = new int[1];
        byte[] alphaData = new byte[16];
        
        int halfbytes = 1;
        if(arguments.fileFormat == EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS)
            halfbytes = 2;

        if (arguments.fileFormat == EtcConstants.ETC1_RGB_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_RGB_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_sRGB_NO_MIPMAPS) {
            img = EtcFile.readPPM(arguments.src, width, height);

            if (EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 24)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 24);
                width[0] = expandedWidth[0];
                height[0] = expandedHeight[0];
            }
            
            if(arguments.ktxFile)
            {
                EtcFile.writeKTXHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                int imagesize = (expandedWidth[0] * expandedHeight[0] * halfbytes) / 2;
                EtcFile.writeKTXImageSize(arguments.dst, imagesize);
            }
            else
            {
                EtcFile.writePKMHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                EtcFile.writeActivePixels(arguments.dst, width[0], height[0]);
            }

            for (int y = 0; y < height[0]; y += 4) {
                for (int x = 0; x < width[0]; x += 4) {
                    if (arguments.codec == EtcConstants.CODEC_ETC) {
                        if (arguments.speed == EtcConstants.SPEED_FAST) {
                            EtcDiffFlipBlock.compressBlockDiffFlipFast(img, null, width[0], height[0], x, y, compressed1, compressed2);
                        }
                    } else if (arguments.codec == EtcConstants.CODEC_ETC2) {
                        if (arguments.speed == EtcConstants.SPEED_FAST) {
                            EtcETC2Block.compressBlockETC2Fast(img, null, null, width[0], height[0], x, y, compressed1, compressed2);
                        }
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], arguments.dst, arguments.fileFormat);
                }
            }
        } else if (arguments.fileFormat == EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS || arguments.fileFormat ==  EtcConstants.ETC2PACKAGE_sRGBA1_NO_MIPMAPS) {
            // ToDo: Read RGBA
            if (EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 32)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                alphaimg = new byte[expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 32);
                EtcUtils.expandToWidthDivByFour(alphaimg, width[0], height[0], expandedWidth, expandedHeight, 8);
                width[0] = expandedWidth[0];
                height[0] = expandedHeight[0];
            }
            
            if(arguments.ktxFile)
            {
                EtcFile.writeKTXHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                int imagesize = (expandedWidth[0] * expandedHeight[0] * halfbytes) / 2;
                EtcFile.writeKTXImageSize(arguments.dst, imagesize);
            }
            else
            {
                EtcFile.writePKMHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                EtcFile.writeActivePixels(arguments.dst, width[0], height[0]);
            }
            
            for (int y = 0; y < height[0]; y += 4) {
                for (int x = 0; x < width[0]; x += 4) {
                    if (arguments.speed == EtcConstants.SPEED_FAST) {
                        EtcETC2Block.compressBlockETC2Fast(img, alphaimg, null, width[0], height[0], x, y, compressed1, compressed2);
                        EtcAlphaBlock.compressBlockAlphaFast(alphaimg, x, y, width[0], height[0], alphaData);
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], arguments.dst, arguments.fileFormat);
                    EtcFile.writeCompressedAlphaBlock(alphaData, arguments.dst, arguments.fileFormat);
                }
            }
        } else if (arguments.fileFormat == EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_R_SIGNED_NO_MIPMAPS || arguments.fileFormat == EtcConstants.ETC2PACKAGE_RG_SIGNED_NO_MIPMAPS) {
            // ToDo: Read R or RG
            if (EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 24)) {
                img = new byte[3 * expandedWidth[0] * expandedHeight[0]];
                EtcUtils.expandToWidthDivByFour(img, width[0], height[0], expandedWidth, expandedHeight, 24);
                width[0] = expandedWidth[0];
                height[0] = expandedHeight[0];
            }
            
            if(arguments.ktxFile)
            {
                EtcFile.writeKTXHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                int imagesize = (expandedWidth[0] * expandedHeight[0] * halfbytes) / 2;
                EtcFile.writeKTXImageSize(arguments.dst, imagesize);
            }
            else
            {
                EtcFile.writePKMHeader(arguments.dst, arguments.fileFormat, arguments.formatSigned, width[0], height[0]);
                EtcFile.writeActivePixels(arguments.dst, width[0], height[0]);
            }
            
            for (int y = 0; y < height[0]; y += 4) {
                for (int x = 0; x < width[0]; x += 4) {
                    if (arguments.speed == EtcConstants.SPEED_FAST) {
                        EtcETC2Block.compressBlockETC2Fast(img, null, null, width[0], height[0], x, y, compressed1, compressed2);
                    }
                    // Write compressed data to file
                    EtcFile.writeCompressedBlock(compressed1[0], compressed2[0], arguments.dst, arguments.fileFormat);
                }
            }
        }
        raf.close();
    }
} 