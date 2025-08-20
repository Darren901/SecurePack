package com.file.securepack.archive;

public class ArchiverFactory {
    public static Archiver getArchiver(String format) {
        return switch (format.toLowerCase()) {
            case "zip" -> new ZipArchiver();
            case "rar" -> new RarArchiver();
            case "7z"  -> new SevenZArchiver();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
