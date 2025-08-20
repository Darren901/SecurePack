package com.file.securepack.common;

public class Constant {

    // public static final String WORK_DIR = System.getProperty("user.home");
    public static final String WORK_DIR = "/tmp/tap";

    public enum ACTION{
        COMPRESS("compress"), DECOMPRESS("decompress");

        private final String value;

        ACTION(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
