package com.here.etc2;

public class EtcPlanarBlock {

    // Compress a block using planar mode.
    public static void compressBlockPlanar57(byte[] img, int width, int height, int startx, int starty, int[] planar57Word1, int[] planar57Word2) {
        int block_part1 = 0;
        int block_part2 = 0;
        int i, j;
        int r0, g0, b0, r1, g1, b1, r2, g2, b2;

        // Get the colors from the corners of the block
        r0 = EtcUtils.RED(img, width, startx, starty);
        g0 = EtcUtils.GREEN(img, width, startx, starty);
        b0 = EtcUtils.BLUE(img, width, startx, starty);

        r1 = EtcUtils.RED(img, width, startx + 3, starty);
        g1 = EtcUtils.GREEN(img, width, startx + 3, starty);
        b1 = EtcUtils.BLUE(img, width, startx + 3, starty);

        r2 = EtcUtils.RED(img, width, startx, starty + 3);
        g2 = EtcUtils.GREEN(img, width, startx, starty + 3);
        b2 = EtcUtils.BLUE(img, width, startx, starty + 3);

        // Store the colors in the compressed data
        block_part1 = EtcUtils.putBits(block_part1, r0 >> 3, 5, 31);
        block_part1 = EtcUtils.putBits(block_part1, g0 >> 3, 5, 24);
        block_part1 = EtcUtils.putBits(block_part1, b0 >> 3, 5, 16);

        block_part1 = EtcUtils.putBits(block_part1, r1 >> 3, 5, 15);
        block_part1 = EtcUtils.putBits(block_part1, g1 >> 3, 5, 8);
        block_part1 = EtcUtils.putBits(block_part1, b1 >> 3, 5, 0);

        block_part2 = EtcUtils.putBits(block_part2, r2 >> 3, 5, 31);
        block_part2 = EtcUtils.putBits(block_part2, g2 >> 3, 5, 24);
        block_part2 = EtcUtils.putBits(block_part2, b2 >> 3, 5, 16);

        planar57Word1[0] = block_part1;
        planar57Word2[0] = block_part2;
    }
} 