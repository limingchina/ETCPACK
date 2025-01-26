# ETCPACK

Ericsson has developed a texture compression system called "Ericsson Texture Compression". The software for compressing images and textures to that format is called ETCPACK.

The latest version of this software includes the possibility to compress images to the new formats introduced as mandatory in the Khronos standards OpenGL ES 3.0 and OpenGL 4.3. We call this package the ETC2-package of codecs, where ETC stands for Ericsson Texture Compression. For instance the new RGB8 ETC2 codec allows higher-quality compression than ETC1. It is also backward compatible; an old ETC1 texture can be decoded using ETC2-capable handsets. There are also new formats for RGBA textures and single-channel (R) and double-channel (RG) textures. For a complete list of codecs, see Appendix C in the OpenGL ES 3.0 standard. The new software also compresses old ETC1 textures. The software can be used by independent hardware vendors who want to include ETC2-package-compression in the tool chains that they give or sell to game developers. It can also be used directly by game developers who want to create games for OpenGL ES 3.0 capable handsets. The software includes source code for the command-line-program etcpack.

# Java Port

This is a Java port of the ETC2 codec. It is not a complete port, but only the parts needed to generate the ETC2 texture format. The port is based on the C++ code from Ericsson. The code is not optimized for performance. It is intended to be used as a reference implementation for testing and development purposes.

The Java code is located in the `source/com/here/etc2` directory. The code is organized into several classes, each representing a different part of the ETC2 codec. The main class is `etcpack`, which contains the main method for the command-line program.

## How to build and run:
At the root of the code check out. Run
```
./gradlew build
```
The compiled binary is located at `build/libs`. To test if it's working, one can run
```
java -jar ./build/libs/ETCPACK-1.0-SNAPSHOT.jar originals/elina.ppm elina.ktx
```
This will compress the `elina.ppm` image to the `elina.ktx` file. In order to very if the compression is working. One can build the C++ code and run the following command:
```
./etcpack elina.ktx elina_uncompressed.ppm
```
Then one can check if the elina_uncompressed.ppm file is similar as the original elina.ppm file.

## Current Porting Status

RGB format is supported. The compression is using the code path `compressBlockDiffFlipFast`. Inside this implementation, in the C++ code, there are two code paths: `compressBlockDiffFlipAverage` and `compressBlockDiffFlipCombined`. The selection logic is not implemented yet. Currently `compressBlockDiffFlipAverage` is used, though the `compressBlockDiffFlipCombined` is also working. For RGBA image, the porting is partially done by commenting some code path. However, it's not tested yet.

The current files used in the working implementation are etcpack.java, EtcTables.java, EtcUtils.java EtcFile.java, EtcDiffFlipBlock.java. There are also other code originally converted from AI. They should contain quite some bugs. For now, it's just serving as reference if there is need in the future to port more code. 

## Changes in the original C++ code

The majority of the code for EXHAUSTIVE_CODE_ACTIVE is removed. It's made shorter for easier consumption of AI coding assistant. Some minor adaptation is made to make the code working under Linux and Mac instead of windows. It's related to some system command invoke. Some minor compilation fix is also made and it's tested under MacOS.
        
        


