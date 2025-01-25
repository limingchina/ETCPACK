package com.here.etc2;

public class EtcAlphaBlock{

    // Compress an alpha block using fast mode.
    public static void compressBlockAlphaFast(byte[] alphaimg, int startx, int starty, int width, int height, byte[] alphaData) {
        int i, j;
        int a0, a1, a2, a3;
        int best_a0 = 0, best_a1 = 0, best_a2 = 0, best_a3 = 0;
        int best_error = 255 * 255 * 16;
        byte[] orig = new byte[1];
        byte[] approx = new byte[1];

        for (int aa0 = 0; aa0 < 16; aa0++) {
            for (int aa1 = 0; aa1 < 16; aa1++) {
                for (int aa2 = 0; aa2 < 16; aa2++) {
                    for (int aa3 = 0; aa3 < 16; aa3++) {
                        int current_error = 0;
                        for (i = 0; i < 4; i++) {
                            for (j = 0; j < 4; j++) {
                                orig[0] = alphaimg[starty * width + startx + j + i * width];

                                int a;
                                if (i < 2 && j < 2) {
                                    a = aa0;
                                } else if (i < 2 && j >= 2) {
                                    a = aa1;
                                } else if (i >= 2 && j < 2) {
                                    a = aa2;
                                } else {
                                    a = aa3;
                                }

                                approx[0] = (byte) (a << 4);
                                current_error += EtcUtils.square(approx[0] - orig[0]);
                            }
                        }
                        if (current_error < best_error) {
                            best_error = current_error;
                            best_a0 = aa0;
                            best_a1 = aa1;
                            best_a2 = aa2;
                            best_a3 = aa3;
                        }
                    }
                }
            }
        }

        alphaData[0] = (byte) ((best_a0 << 4) | best_a1);
        alphaData[1] = (byte) ((best_a2 << 4) | best_a3);
    }

    // Compress an alpha block using slow mode.
    public static void compressBlockAlphaSlow(byte[] alphaimg, int startx, int starty, int width, int height, byte[] alphaData) {
        int i, j;
        int a0, a1, a2, a3, a4, a5, a6, a7;
        int best_a0 = 0, best_a1 = 0, best_a2 = 0, best_a3 = 0, best_a4 = 0, best_a5 = 0, best_a6 = 0, best_a7 = 0;
        int best_error = 255 * 255 * 16;
        byte[] orig = new byte[1];
        byte[] approx = new byte[1];

        for (int aa0 = 0; aa0 < 16; aa0++) {
            for (int aa1 = 0; aa1 < 16; aa1++) {
                for (int aa2 = 0; aa2 < 16; aa2++) {
                    for (int aa3 = 0; aa3 < 16; aa3++) {
                        for (int aa4 = 0; aa4 < 16; aa4++) {
                            for (int aa5 = 0; aa5 < 16; aa5++) {
                                for (int aa6 = 0; aa6 < 16; aa6++) {
                                    for (int aa7 = 0; aa7 < 16; aa7++) {
                                        int current_error = 0;
                                        for (i = 0; i < 4; i++) {
                                            for (j = 0; j < 4; j++) {
                                                orig[0] = alphaimg[starty * width + startx + j + i * width];

                                                int a;
                                                if (i == 0 && j == 0) {
                                                    a = aa0;
                                                } else if (i == 0 && j == 1) {
                                                    a = aa1;
                                                } else if (i == 0 && j == 2) {
                                                    a = aa2;
                                                } else if (i == 0 && j == 3) {
                                                    a = aa3;
                                                } else if (i == 1 && j == 0) {
                                                    a = aa4;
                                                } else if (i == 1 && j == 1) {
                                                    a = aa5;
                                                } else if (i == 1 && j == 2) {
                                                    a = aa6;
                                                } else {
                                                    a = aa7;
                                                }

                                                approx[0] = (byte) (a << 4);
                                                current_error += EtcUtils.square(approx[0] - orig[0]);
                                            }
                                        }
                                        if (current_error < best_error) {
                                            best_error = current_error;
                                            best_a0 = aa0;
                                            best_a1 = aa1;
                                            best_a2 = aa2;
                                            best_a3 = aa3;
                                            best_a4 = aa4;
                                            best_a5 = aa5;
                                            best_a6 = aa6;
                                            best_a7 = aa7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        alphaData[0] = (byte) ((best_a0 << 4) | best_a1);
        alphaData[1] = (byte) ((best_a2 << 4) | best_a3);
        alphaData[2] = (byte) ((best_a4 << 4) | best_a5);
        alphaData[3] = (byte) ((best_a6 << 4) | best_a7);
    }

    // Compress an alpha block using 16-bit input.
    public static void compressBlockAlpha16(byte[] alphaimg, int startx, int starty, int width, int height, byte[] alphaData) {
        int i, j;
        int a0, a1, a2, a3;
        int best_a0 = 0, best_a1 = 0, best_a2 = 0, best_a3 = 0;
        int best_error = 255 * 255 * 16;
        byte[] orig = new byte[1];
        byte[] approx = new byte[1];

        for (int aa0 = 0; aa0 < 2048; aa0++) {
            for (int aa1 = 0; aa1 < 2048; aa1++) {
                for (int aa2 = 0; aa2 < 2048; aa2++) {
                    for (int aa3 = 0; aa3 < 2048; aa3++) {
                        int current_error = 0;
                        for (i = 0; i < 4; i++) {
                            for (j = 0; j < 4; j++) {
                                orig[0] = alphaimg[starty * width * 2 + (startx + j + i * width) * 2];

                                int a;
                                if (i < 2 && j < 2) {
                                    a = aa0;
                                } else if (i < 2 && j >= 2) {
                                    a = aa1;
                                } else if (i >= 2 && j < 2) {
                                    a = aa2;
                                } else {
                                    a = aa3;
                                }

                                approx[0] = (byte) (a >> 4);
                                current_error += EtcUtils.square(approx[0] - orig[0]);
                            }
                        }
                        if (current_error < best_error) {
                            best_error = current_error;
                            best_a0 = aa0;
                            best_a1 = aa1;
                            best_a2 = aa2;
                            best_a3 = aa3;
                        }
                    }
                }
            }
        }

        alphaData[0] = (byte) ((best_a0 >> 8) & 0x0f);
        alphaData[1] = (byte) (best_a0 & 0xff);
        alphaData[2] = (byte) ((best_a1 >> 8) & 0x0f);
        alphaData[3] = (byte) (best_a1 & 0xff);
        alphaData[4] = (byte) ((best_a2 >> 8) & 0x0f);
        alphaData[5] = (byte) (best_a2 & 0xff);
        alphaData[6] = (byte) ((best_a3 >> 8) & 0x0f);
        alphaData[7] = (byte) (best_a3 & 0xff);
    }

    // Compress an alpha block using 16-bit input and slow mode.
    public static void compressBlockAlpha16Slow(byte[] alphaimg, int startx, int starty, int width, int height, byte[] alphaData) {
        int i, j;
        int a0, a1, a2, a3, a4, a5, a6, a7;
        int best_a0 = 0, best_a1 = 0, best_a2 = 0, best_a3 = 0, best_a4 = 0, best_a5 = 0, best_a6 = 0, best_a7 = 0;
        int best_error = 255 * 255 * 16;
        byte[] orig = new byte[1];
        byte[] approx = new byte[1];

        for (int aa0 = 0; aa0 < 2048; aa0++) {
            for (int aa1 = 0; aa1 < 2048; aa1++) {
                for (int aa2 = 0; aa2 < 2048; aa2++) {
                    for (int aa3 = 0; aa3 < 2048; aa3++) {
                        for (int aa4 = 0; aa4 < 2048; aa4++) {
                            for (int aa5 = 0; aa5 < 2048; aa5++) {
                                for (int aa6 = 0; aa6 < 2048; aa6++) {
                                    for (int aa7 = 0; aa7 < 2048; aa7++) {
                                        int current_error = 0;
                                        for (i = 0; i < 4; i++) {
                                            for (j = 0; j < 4; j++) {
                                                orig[0] = alphaimg[starty * width * 2 + (startx + j + i * width) * 2];

                                                int a;
                                                if (i == 0 && j == 0) {
                                                    a = aa0;
                                                } else if (i == 0 && j == 1) {
                                                    a = aa1;
                                                } else if (i == 0 && j == 2) {
                                                    a = aa2;
                                                } else if (i == 0 && j == 3) {
                                                    a = aa3;
                                                } else if (i == 1 && j == 0) {
                                                    a = aa4;
                                                } else if (i == 1 && j == 1) {
                                                    a = aa5;
                                                } else if (i == 1 && j == 2) {
                                                    a = aa6;
                                                } else {
                                                    a = aa7;
                                                }

                                                approx[0] = (byte) (a >> 4);
                                                current_error += EtcUtils.square(approx[0] - orig[0]);
                                            }
                                        }
                                        if (current_error < best_error) {
                                            best_error = current_error;
                                            best_a0 = aa0;
                                            best_a1 = aa1;
                                            best_a2 = aa2;
                                            best_a3 = aa3;
                                            best_a4 = aa4;
                                            best_a5 = aa5;
                                            best_a6 = aa6;
                                            best_a7 = aa7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        alphaData[0] = (byte) ((best_a0 >> 8) & 0x0f);
        alphaData[1] = (byte) (best_a0 & 0xff);
        alphaData[2] = (byte) ((best_a1 >> 8) & 0x0f);
        alphaData[3] = (byte) (best_a1 & 0xff);
        alphaData[4] = (byte) ((best_a2 >> 8) & 0x0f);
        alphaData[5] = (byte) (best_a2 & 0xff);
        alphaData[6] = (byte) ((best_a3 >> 8) & 0x0f);
        alphaData[7] = (byte) (best_a3 & 0xff);
        alphaData[8] = (byte) ((best_a4 >> 8) & 0x0f);
        alphaData[9] = (byte) (best_a4 & 0xff);
        alphaData[10] = (byte) ((best_a5 >> 8) & 0x0f);
        alphaData[11] = (byte) (best_a5 & 0xff);
        alphaData[12] = (byte) ((best_a6 >> 8) & 0x0f);
        alphaData[13] = (byte) (best_a6 & 0xff);
        alphaData[14] = (byte) ((best_a7 >> 8) & 0x0f);
        alphaData[15] = (byte) (best_a7 & 0xff);
    }
} 