package com.file.securepack.archive;

import com.file.securepack.vo.CompressionOptions;

import java.io.File;
import java.io.IOException;

/**
 * @author Darren
 * @date 2025-08-22
 */
public interface Archiver {
    void compress(File input, File output, CompressionOptions options) throws IOException;
    void decompress(File input, File output, CompressionOptions options) throws IOException;
}
