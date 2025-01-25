package com.here.etc2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.StringTokenizer;

public class EtcFile {

    // Function to check if a file exists
    public static boolean fileExist(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    // Find the position of a file extension such as .ppm or .pkm
    public static int findPosOfExtension(String src) {
        int q = src.length() - 1;
        while (q >= 0) {
            if (src.charAt(q) == '.') break;
            q--;
        }
        return (q < 0) ? -1 : q;
    }

    // Read source file. Does conversion if file format is not .ppm.
    // Will expand file to be divisible by four in the x- and y- dimension.
    public static boolean readSrcFile(String filename, byte[][] img, int[] width, int[] height, int[] expandedWidth, int[] expandedHeight, int format) {
        int[] w1 = new int[1];
        int[] h1 = new int[1];
        String str;

        // Delete temp file if it exists.
        if (fileExist("tmp.ppm")) {
            try {
                // Java equivalent of "del tmp.ppm"
                File tmpFile = new File("tmp.ppm");
                if (!tmpFile.delete()) {
                    System.err.println("Could not delete tmp.ppm");
                }
            } catch (Exception e) {
                System.err.println("Error deleting tmp.ppm: " + e.getMessage());
            }
        }

        int q = findPosOfExtension(filename);
        if (q >= 0 && filename.substring(q).equals(".ppm")) {
            // Already a .ppm file. Just copy.
            str = "copy " + filename + " tmp.ppm";
            System.out.println("Copying source file to tmp.ppm");
        } else {
            // Converting from other format to .ppm
            str = "magick convert " + filename + " tmp.ppm";
            System.out.println("Converting source file from " + filename + " to .ppm");
        }

        // Execute system call
        try {
            Process process = Runtime.getRuntime().exec(str);
            process.waitFor();
            if (process.exitValue() != 0) {
                System.err.println("Error executing command: " + str);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing command: " + e.getMessage());
            return false;
        }

        int bitrate = 8;
        if (format == 6)
            bitrate = 16;
        if (fReadPPM("tmp.ppm", w1, h1, img, bitrate)) {
            width[0] = w1[0];
            height[0] = h1[0];
            try {
                // Java equivalent of "del tmp.ppm"
                File tmpFile = new File("tmp.ppm");
                if (!tmpFile.delete()) {
                    System.err.println("Could not delete tmp.ppm");
                }
            } catch (Exception e) {
                System.err.println("Error deleting tmp.ppm: " + e.getMessage());
            }

            // Width must be divisible by 4 and height must be
            // divisible by 4. Otherwise, we will expand the image
            int wdiv4 = width[0] / 4;
            int hdiv4 = height[0] / 4;

            expandedWidth[0] = width[0];
            expandedHeight[0] = height[0];

            if (wdiv4 * 4 != width[0]) {
                System.out.print(" Width = " + width[0] + " is not divisible by four... ");
                System.out.print(" expanding image in x-dir... ");
                if (EtcUtils.expandToWidthDivByFour(img[0], width[0], height[0], expandedWidth, expandedHeight, bitrate)) {
                    System.out.println("OK.");
                } else {
                    System.out.println("\n Error: could not expand image");
                    return false;
                }
            }
            if (hdiv4 * 4 != height[0]) {
                System.out.print(" Height = " + height[0] + " is not divisible by four... ");
                System.out.print(" expanding image in y-dir...");
                if (EtcUtils.expandToHeightDivByFour(img[0], expandedWidth[0], height[0], expandedWidth, expandedHeight, bitrate)) {
                    System.out.println("OK.");
                } else {
                    System.out.println("\n Error: could not expand image");
                    return false;
                }
            }
            if (expandedWidth[0] != width[0] || expandedHeight[0] != height[0])
                System.out.println("Active pixels: " + width[0] + "x" + height[0] + ". Expanded image: " + expandedWidth[0] + "x" + expandedHeight[0]);
            return true;
        } else {
            System.out.println("Could not read tmp.ppm file");
            return false;
        }
    }

    // Reads a PPM file
    private static boolean fReadPPM(String filename, int[] width, int[] height, byte[][] img, int bitrate) {
        try (RandomAccessFile f = new RandomAccessFile(filename, "r")) {
            String line;
            // Read the magic number
            line = f.readLine();
            if (!line.equals("P6")) {
                System.err.println("Error: Not a valid P6 PPM file.");
                return false;
            }

            // Skip comments
            while ((line = f.readLine()) != null && line.startsWith("#"));

            // Read width and height
            String[] dimensions = line.split(" ");
            width[0] = Integer.parseInt(dimensions[0]);
            height[0] = Integer.parseInt(dimensions[1]);

            // Read max color value
            line = f.readLine();
            if (!line.equals("255")) {
                System.err.println("Error: Not a valid 8-bit PPM file.");
                return false;
            }

            // Read pixel data
            int numPixels = width[0] * height[0];
            img[0] = new byte[numPixels * 3 * bitrate / 8];
            f.readFully(img[0]);
            return true;
        } catch (IOException e) {
            System.err.println("Error reading PPM file: " + e.getMessage());
            return false;
        }
    }

    // Parses the arguments from the command line.
    public static Arguments readArguments(String[] args) {
        Arguments arguments = new Arguments();
        arguments.src = "";
        arguments.dst = "";
        arguments.mode = 0;
        arguments.speed = 0;
        arguments.metric = 0;
        arguments.codec = 0;
        arguments.fileFormat = 0;
        arguments.verbose = false;
        arguments.formatSigned = 0;
        arguments.ktxFile = false;

        boolean srcFound = false;
        boolean dstFound = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (i == args.length - 1) {
                    System.out.println("flag missing argument: " + args[i] + "!");
                    System.exit(1);
                }
                if (args[i].equals("-s")) {
                    if (args[i + 1].equals("slow"))
                        arguments.speed = EtcConstants.SPEED_SLOW;
                    else if (args[i + 1].equals("medium"))
                        arguments.speed = EtcConstants.SPEED_MEDIUM;
                    else if (args[i + 1].equals("fast"))
                        arguments.speed = EtcConstants.SPEED_FAST;
                    else {
                        System.out.println("Error: " + args[i + 1] + " not part of flag " + args[i]);
                        System.exit(1);
                    }
                } else if (args[i].equals("-v")) {
                    if (args[i + 1].equals("off"))
                        arguments.verbose = false;
                    else if (args[i + 1].equals("on"))
                        arguments.verbose = true;
                    else {
                        System.out.println("Error: " + args[i + 1] + " not part of flag " + args[i]);
                        System.exit(1);
                    }
                } else if (args[i].equals("-metric") || args[i].equals("-e")) {
                    if (args[i + 1].equals("perceptual"))
                        arguments.metric = EtcConstants.METRIC_PERCEPTUAL;
                    else if (args[i + 1].equals("nonperceptual"))
                        arguments.metric = EtcConstants.METRIC_NONPERCEPTUAL;
                    else {
                        System.out.println("Error: " + args[i + 1] + " not part of flag " + args[i]);
                        System.exit(1);
                    }
                } else if (args[i].equals("-codec") || args[i].equals("-c")) {
                    if (args[i + 1].equals("etc") || args[i + 1].equals("etc1"))
                        arguments.codec = EtcConstants.CODEC_ETC;
                    else if (args[i + 1].equals("etc2"))
                        arguments.codec = EtcConstants.CODEC_ETC2;
                    else {
                        System.out.println("Error: " + args[i + 1] + " not part of flag " + args[i]);
                        System.exit(1);
                    }
                } else if (args[i].equals("-format") || args[i].equals("-f")) {
                    if (args[i + 1].equals("R"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS;
                    else if (args[i + 1].equals("RG"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS;
                    else if (args[i + 1].equals("R_signed")) {
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS;
                        arguments.formatSigned = 1;
                    } else if (args[i + 1].equals("RG_signed")) {
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS;
                        arguments.formatSigned = 1;
                    } else if (args[i + 1].equals("RGB"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_RGB_NO_MIPMAPS;
                    else if (args[i + 1].equals("sRGB"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_sRGB_NO_MIPMAPS;
                    else if (args[i + 1].equals("RGBA") || args[i + 1].equals("RGBA8"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS;
                    else if (args[i + 1].equals("sRGBA") || args[i + 1].equals("sRGBA8"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS;
                    else if (args[i + 1].equals("RGBA1"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS;
                    else if (args[i + 1].equals("sRGBA1"))
                        arguments.fileFormat = EtcConstants.ETC2PACKAGE_sRGBA1_NO_MIPMAPS;
                    else {
                        System.out.println("Error: " + args[i + 1] + " not part of flag " + args[i]);
                        System.exit(1);
                    }
                } else {
                    System.out.println("Error: cannot interpret flag " + args[i] + " " + args[i + 1]);
                    System.exit(1);
                }
                i++;
            } else {
                if (srcFound && dstFound) {
                    System.out.println("too many arguments! expecting src, dst; found " + arguments.src + ", " + arguments.dst + ", " + args[i]);
                    System.exit(1);
                } else if (srcFound) {
                    arguments.dst = args[i];
                    if(arguments.dst.endsWith(".ktx"))
                        arguments.ktxFile = true;
                    dstFound = true;
                } else {
                    arguments.src = args[i];
                    srcFound = true;
                }
            }
        }
        if (!srcFound && dstFound) {
            System.out.println("too few arguments! expecting src, dst");
            System.exit(1);
        }
        return arguments;
    }

    private static String readLine(RandomAccessFile raf) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = raf.read()) != -1) {
            if (c == '\n') {
                break;
            }
            line.append((char) c);
        }
        return line.toString();
    }

    public static byte[] readPPM(String filename, int[] width, int[] height) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filename, "r");

        // Read the magic number
        String magicNumber = readLine(raf).trim();
        if (!magicNumber.equals("P6")) {
            throw new IOException("Unsupported PPM format: " + magicNumber);
        }

        // Skip comments
        String line;
        do {
            line = readLine(raf).trim();
        } while (line.startsWith("#"));

        // Read width, height, and max color value
        StringTokenizer tokenizer = new StringTokenizer(line);
        width[0] = Integer.parseInt(tokenizer.nextToken());
        height[0] = Integer.parseInt(tokenizer.nextToken());
        line = readLine(raf).trim();
        if (!line.equals("255")) {
            throw new IOException("Unsupported PPM format: " + line);
        }

        byte[] img = new byte[height[0]*width[0]*3];
        int read = raf.read(img);
        raf.close();
        return img;
    }
    
    public static void writeCompressedBlock(int compressed1, int compressed2, String dst, int fileFormat) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        if (fileFormat == EtcConstants.ETC1_RGB_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_RGB_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_sRGB_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_R_SIGNED_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_RG_SIGNED_NO_MIPMAPS) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(compressed1);
            buffer.putInt(compressed2);
            raf.write(buffer.array());
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(compressed1);
            buffer.putInt(compressed2);
            raf.write(buffer.array());
        }
        raf.close();
    }

    public static void writeCompressedAlphaBlock(byte[] alphaData, String dst, int fileFormat) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        if (fileFormat == EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS || fileFormat == EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
            ByteBuffer buffer = ByteBuffer.wrap(alphaData);
            raf.write(buffer.array());
        }
        raf.close();
    }
    
    public static void writePKMHeader(String dst, int fileFormat, int formatSigned, int width, int height) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        byte[] magic = {'P', 'K', 'M', ' '};
        byte[] version = {'2', '0'};
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(magic);
        buffer.put(version);
        
        int textureType = fileFormat;
        if(fileFormat == EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS && formatSigned == 1)
            textureType = EtcConstants.ETC2PACKAGE_RG_SIGNED_NO_MIPMAPS;
        else if(fileFormat == EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS && formatSigned == 1)
            textureType = EtcConstants.ETC2PACKAGE_R_SIGNED_NO_MIPMAPS;
        buffer.putShort((short) textureType);
        buffer.putShort((short) width);
        buffer.putShort((short) height);
        raf.write(buffer.array());
        raf.close();
    }

    public static void writeActivePixels(String dst, int width, int height) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort((short) width);
        buffer.putShort((short) height);
        raf.write(buffer.array());
        raf.close();
    }

    public static void writeKTXHeader(String dst, int fileFormat, int formatSigned, int width, int height) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(new byte[]{(byte) 0xAB, 0x4B, 0x54, 0x58, 0x20, 0x31, 0x31, (byte) 0xBB, 0x0D, 0x0A, 0x1A, 0x0A});
        buffer.putInt(0x04030201); // endianness
        buffer.putInt(0); // glType
        buffer.putInt(1); // glTypeSize
        buffer.putInt(0); // glFormat

        int glInternalFormat = 0;
        int glBaseInternalFormat = 0;
        if (fileFormat == EtcConstants.ETC2PACKAGE_R_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_R;
            glInternalFormat = formatSigned == 1 ? EtcConstants.GL_COMPRESSED_SIGNED_R11_EAC : EtcConstants.GL_COMPRESSED_R11_EAC;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_RG_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_RG;
            glInternalFormat = formatSigned == 1 ? EtcConstants.GL_COMPRESSED_SIGNED_RG11_EAC : EtcConstants.GL_COMPRESSED_RG11_EAC;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_RGB_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_RGB;
            glInternalFormat = EtcConstants.GL_COMPRESSED_RGB8_ETC2;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_sRGB_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_SRGB;
            glInternalFormat = EtcConstants.GL_COMPRESSED_SRGB8_ETC2;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_RGBA_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_RGBA;
            glInternalFormat = EtcConstants.GL_COMPRESSED_RGBA8_ETC2_EAC;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_sRGBA_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_SRGB8_ALPHA8;
            glInternalFormat = EtcConstants.GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_RGBA;
            glInternalFormat = EtcConstants.GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2;
        } else if (fileFormat == EtcConstants.ETC2PACKAGE_sRGBA1_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_SRGB8_ALPHA8;
            glInternalFormat = EtcConstants.GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2;
        } else if (fileFormat == EtcConstants.ETC1_RGB_NO_MIPMAPS) {
            glBaseInternalFormat = EtcConstants.GL_RGB;
            glInternalFormat = EtcConstants.GL_ETC1_RGB8_OES;
        }
        buffer.putInt(glInternalFormat);
        buffer.putInt(glBaseInternalFormat);
        buffer.putInt(width);
        buffer.putInt(height);
        buffer.putInt(0); // pixelDepth
        buffer.putInt(0); // numberOfArrayElements
        buffer.putInt(1); // numberOfFaces
        buffer.putInt(1); // numberOfMipmapLevels
        buffer.putInt(0); // bytesOfKeyValueData
        raf.write(buffer.array());
        raf.close();
    }

    public static void writeKTXImageSize(String dst, int imageSize) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(imageSize);
        raf.write(buffer.array());
        raf.close();
    }
} 