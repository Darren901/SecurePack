package com.file.securepack.common;

/**
 * @author Darren
 * @date 2025-08-22
 */
public class Constant {

    public static final String WORK_DIR = System.getProperty("user.home") + "/tmp/securepack";
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
