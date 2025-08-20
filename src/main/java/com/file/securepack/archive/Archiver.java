package com.file.securepack.archive;

import com.file.securepack.vo.CompressionOptions;

import java.io.File;
import java.io.IOException;

public interface Archiver {
    void compress(File input, File output, CompressionOptions options) throws IOException;
    void decompress(File input, File output, CompressionOptions options) throws IOException;
}
