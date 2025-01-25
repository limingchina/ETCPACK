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
                if (etcpack.expandToEvenWidth(img[0], width[0], height[0], expandedWidth, expandedHeight, bitrate)) {
                    System.out.println("OK.");
                } else {
                    System.out.println("\n Error: could not expand image");
                    return false;
                }
            }
            if (hdiv4 * 4 != height[0]) {
                System.out.print(" Height = " + height[0] + " is not divisible by four... ");
                System.out.print(" expanding image in y-dir...");
                if (etcpack.expandToHeightDivByFour(img[0], expandedWidth[0], height[0], expandedWidth, expandedHeight, bitrate)) {
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
    public static void readArguments(String[] argv, String[] src, String[] dst, int[] mode, int[] speed, int[] metric, int[] codec, int[] format, boolean[] verbose, int[] formatSigned, boolean[] ktxFile) {
        boolean srcFound = false, dstFound = false;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].startsWith("-")) {
                if (i == argv.length - 1) {
                    System.out.println("flag missing argument: " + argv[i] + "!");
                    System.exit(1);
                }
                if (argv[i].equals("-s")) {
                    if (argv[i + 1].equals("slow"))
                        speed[0] = 0; // SPEED_SLOW
                    else if (argv[i + 1].equals("medium"))
                        speed[0] = 1; // SPEED_MEDIUM
                    else if (argv[i + 1].equals("fast"))
                        speed[0] = 2; // SPEED_FAST
                    else {
                        System.out.println("Error: " + argv[i + 1] + " not part of flag " + argv[i]);
                        System.exit(1);
                    }
                } else if (argv[i].equals("-v")) {
                    if (argv[i + 1].equals("off"))
                        verbose[0] = false;
                    else if (argv[i + 1].equals("on"))
                        verbose[0] = true;
                    else {
                        System.out.println("Error: " + argv[i + 1] + " not part of flag " + argv[i]);
                        System.exit(1);
                    }
                } else if (argv[i].equals("-e")) {
                    if (argv[i + 1].equals("perceptual"))
                        metric[0] = 0; // METRIC_PERCEPTUAL
                    else if (argv[i + 1].equals("nonperceptual"))
                        metric[0] = 1; // METRIC_NONPERCEPTUAL
                    else {
                        System.out.println("Error: " + argv[i + 1] + " not part of flag " + argv[i]);
                        System.exit(1);
                    }
                } else if (argv[i].equals("-c")) {
                    if (argv[i + 1].equals("etc") || argv[i + 1].equals("etc1"))
                        codec[0] = 0; // CODEC_ETC
                    else if (argv[i + 1].equals("etc2"))
                        codec[0] = 1; // CODEC_ETC2
                    else {
                        System.out.println("Error: " + argv[i + 1] + " not part of flag " + argv[i]);
                        System.exit(1);
                    }
                } else if (argv[i].equals("-f")) {
                    if (argv[i + 1].equals("R"))
                        format[0] = 5; // ETC2PACKAGE_R_NO_MIPMAPS
                    else if (argv[i + 1].equals("RG"))
                        format[0] = 6; // ETC2PACKAGE_RG_NO_MIPMAPS
                    else if (argv[i + 1].equals("R_signed")) {
                        format[0] = 5; // ETC2PACKAGE_R_NO_MIPMAPS
                        formatSigned[0] = 1;
                    } else if (argv[i + 1].equals("RG_signed")) {
                        format[0] = 6; // ETC2PACKAGE_RG_NO_MIPMAPS
                        formatSigned[0] = 1;
                    } else if (argv[i + 1].equals("RGB"))
                        format[0] = 1; // ETC2PACKAGE_RGB_NO_MIPMAPS
                    else if (argv[i + 1].equals("sRGB"))
                        format[0] = 9; // ETC2PACKAGE_sRGB_NO_MIPMAPS
                    else if (argv[i + 1].equals("RGBA") || argv[i + 1].equals("RGBA8"))
                        format[0] = 3; // ETC2PACKAGE_RGBA_NO_MIPMAPS
                    else if (argv[i + 1].equals("sRGBA") || argv[i + 1].equals("sRGBA8"))
                        format[0] = 10; // ETC2PACKAGE_sRGBA_NO_MIPMAPS
                    else if (argv[i + 1].equals("RGBA1"))
                        format[0] = 4; // ETC2PACKAGE_RGBA1_NO_MIPMAPS
                    else if (argv[i + 1].equals("sRGBA1"))
                        format[0] = 11; // ETC2PACKAGE_sRGBA1_NO_MIPMAPS
                    else {
                        System.out.println("Error: " + argv[i + 1] + " not part of flag " + argv[i]);
                        System.exit(1);
                    }
                } else if (argv[i].equals("-p")) {
                    mode[0] = 2; // MODE_PSNR
                    i--; //ugly way of negating the increment of i done later because -p doesn't have an argument.
                } else {
                    System.out.println("Error: cannot interpret flag " + argv[i] + " " + argv[i + 1]);
                    System.exit(1);
                }
                i++;
            } else {
                if (srcFound && dstFound) {
                    System.out.println("too many arguments! expecting src, dst; found " + src[0] + ", " + dst[0] + ", " + argv[i]);
                    System.exit(1);
                } else if (srcFound) {
                    dst[0] = argv[i];
                    dstFound = true;
                } else {
                    src[0] = argv[i];
                    srcFound = true;
                }
            }
        }
        if (!srcFound && dstFound) {
            System.out.println("too few arguments! expecting src, dst");
            System.exit(1);
        }
        if (mode[0] == 2)
            return;
        int q = findPosOfExtension(src[0]);
        if (q < 0) {
            System.out.println("invalid source file: " + src[0]);
            System.exit(1);
        }
        if (src[0].substring(q).equals(".pkm")) {
            mode[0] = 1; // MODE_UNCOMPRESS
        } else if (src[0].substring(q).equals(".ktx")) {
            mode[0] = 1; // MODE_UNCOMPRESS
            ktxFile[0] = true;
            System.out.println("decompressing ktx");
        } else {
            q = findPosOfExtension(dst[0]);
            if (q < 0) {
                System.out.println("invalid destination file: " + src[0]);
                System.exit(1);
            }
            if (dst[0].substring(q).equals(".pkm")) {
                mode[0] = 0; // MODE_COMPRESS
            } else if (dst[0].substring(q).equals(".ktx")) {
                ktxFile[0] = true;
                mode[0] = 0; // MODE_COMPRESS
                System.out.println("compressing to ktx");
            } else {
                System.out.println("source or destination must be a .pkm or .ktx file");
                System.exit(1);
            }
        }
        if (codec[0] == 0 && format[0] != 1) {
            System.out.println("ETC1 codec only supports RGB format");
            System.exit(1);
        } else if (codec[0] == 0)
            format[0] = 0; // ETC1_RGB_NO_MIPMAPS
    }

    public static void readPPM(RandomAccessFile raf, int width, int height, byte[] img) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Read PPM header
        line = reader.readLine();
        if (!line.equals("P6")) {
            System.out.println("Wrong file format");
            return;
        }
        line = reader.readLine();
        String[] dimensions = line.split(" ");
        width = Integer.parseInt(dimensions[0]);
        height = Integer.parseInt(dimensions[1]);
        line = reader.readLine();
        if (!line.equals("255")) {
            System.out.println("Wrong file format");
            return;
        }

        img = new byte[width * height * 3];
        raf.read(img);
    }

    public static void readKTX(RandomAccessFile raf, int width, int height, byte[] img) throws IOException {
        readKTX(raf, width, height, img, null);
    }

    public static void readKTX(RandomAccessFile raf, int width, int height, byte[] img, byte[] alphaimg) throws IOException {
        int[] endian = new int[1];
        int[] glType = new int[1];
        int[] glTypeSize = new int[1];
        int[] glFormat = new int[1];
        int[] glInternalFormat = new int[1];
        int[] glBaseInternalFormat = new int[1];
        int[] pixelWidth = new int[1];
        int[] pixelHeight = new int[1];
        int[] pixelDepth = new int[1];
        int[] numberOfArrayElements = new int[1];
        int[] numberOfFaces = new int[1];
        int[] numberOfMipmapLevels = new int[1];
        int[] bytesOfKeyValueData = new int[1];

        // Read KTX header
        byte[] identifier = new byte[12];
        raf.read(identifier);
        if (!Arrays.equals(identifier, new byte[]{(byte) 0xAB, 0x4B, 0x54, 0x58, 0x20, 0x31, 0x31, (byte) 0xBB, 0x0D, 0x0A, 0x1A, 0x0A})) {
            System.out.println("Wrong file format");
            return;
        }

        EtcUtils.readBigEndian4ByteWord(endian, raf);
        EtcUtils.readBigEndian4ByteWord(glType, raf);
        EtcUtils.readBigEndian4ByteWord(glTypeSize, raf);
        EtcUtils.readBigEndian4ByteWord(glFormat, raf);
        EtcUtils.readBigEndian4ByteWord(glInternalFormat, raf);
        EtcUtils.readBigEndian4ByteWord(glBaseInternalFormat, raf);
        EtcUtils.readBigEndian4ByteWord(pixelWidth, raf);
        EtcUtils.readBigEndian4ByteWord(pixelHeight, raf);
        EtcUtils.readBigEndian4ByteWord(pixelDepth, raf);
        EtcUtils.readBigEndian4ByteWord(numberOfArrayElements, raf);
        EtcUtils.readBigEndian4ByteWord(numberOfFaces, raf);
        EtcUtils.readBigEndian4ByteWord(numberOfMipmapLevels, raf);
        EtcUtils.readBigEndian4ByteWord(bytesOfKeyValueData, raf);

        width = pixelWidth[0];
        height = pixelHeight[0];

        // Skip key/value data
        raf.skipBytes(bytesOfKeyValueData[0]);

        // Read image data
        int imageSize = 0;
        if (glBaseInternalFormat[0] == source.EtcTables.GL_RGB8) {
            imageSize = width * height * 3;
            img = new byte[imageSize];
            raf.read(img);
        } else if (glBaseInternalFormat[0] == source.EtcTables.GL_RGBA8) {
            imageSize = width * height * 4;
            img = new byte[width * height * 3];
            alphaimg = new byte[width * height];
            byte[] rgba = new byte[imageSize];
            raf.read(rgba);
            for (int i = 0; i < width * height; i++) {
                img[i * 3 + 0] = rgba[i * 4 + 0];
                img[i * 3 + 1] = rgba[i * 4 + 1];
                img[i * 3 + 2] = rgba[i * 4 + 2];
                alphaimg[i] = rgba[i * 4 + 3];
            }
        } else if (glBaseInternalFormat[0] == source.EtcTables.GL_R8 || glBaseInternalFormat[0] == source.EtcTables.GL_RG8) {
            imageSize = width * height * (glBaseInternalFormat[0] == source.EtcTables.GL_R8 ? 1 : 2);
            img = new byte[width * height * 3];
            byte[] rorg = new byte[imageSize];
            raf.read(rorg);
            for (int i = 0; i < width * height; i++) {
                img[i * 3 + 0] = rorg[i * (glBaseInternalFormat[0] == source.EtcTables.GL_R8 ? 1 : 2) + 0];
                img[i * 3 + 1] = (byte) (glBaseInternalFormat[0] == source.EtcTables.GL_R8 ? 0 : rorg[i * 2 + 1]);
                img[i * 3 + 2] = 0;
            }
        }
    }

    public static void writeCompressedBlock(int compressed1, int compressed2, String dst, int fileFormat) throws IOException {
        File outputFile = new File(dst);
        RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
        FileChannel channel = raf.getChannel();
        long fileSize = channel.size();
        raf.seek(fileSize);

        if (fileFormat == source.EtcTables.ETC1_RGB_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_RGB_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_sRGB_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_R_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_RG_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_R_SIGNED_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_RG_SIGNED_NO_MIPMAPS) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(compressed1);
            buffer.putInt(compressed2);
            raf.write(buffer.array());
        } else if (fileFormat == source.EtcTables.ETC2PACKAGE_RGBA_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_sRGBA_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
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

        if (fileFormat == source.EtcTables.ETC2PACKAGE_RGBA_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_sRGBA_NO_MIPMAPS || fileFormat == source.EtcTables.ETC2PACKAGE_RGBA1_NO_MIPMAPS) {
            ByteBuffer buffer = ByteBuffer.wrap(alphaData);
            raf.write(buffer.array());
        }
        raf.close();
    }
} 