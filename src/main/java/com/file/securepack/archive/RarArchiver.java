package com.file.securepack.archive;

import com.file.securepack.vo.CompressionOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Darren
 * @date 2025-08-22
 */
@Slf4j
public class RarArchiver implements Archiver {
    @Override
    public void compress(File input, File output, CompressionOptions options) throws IOException {
        log.info("傳進來的password: {}", options.getPassword());
        try {
            String rarExeName = "rar.exe";

            // 從資源中讀取可執行檔
            try (InputStream is = getClass().getResourceAsStream("/bin/" + rarExeName)) {
                if (is == null) {
                    throw new IOException("無法找到壓縮工具: " + rarExeName);
                }

                Path tempRar = Files.createTempFile("rar_temp", ".exe");
                Files.copy(is, tempRar, StandardCopyOption.REPLACE_EXISTING);
                tempRar.toFile().setExecutable(true);

                try {
                    List<String> commands = new ArrayList<>();
                    commands.add(tempRar.toString());
                    commands.add("a");

                    if (options.getPassword() != null && !options.getPassword().isEmpty()) {
                        commands.add("-p" + options.getPassword());
                    }

                    commands.add("-ep1"); // 不包含基本目錄名稱
                    commands.add("-m5"); // 最大壓縮率
                    commands.add(output.getAbsolutePath());
                    commands.add(input.getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(commands);

                    log.info("------>準備開始用 RAR 壓縮");

                    // 輸出執行過程
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.info("RAR output: {}", line);
                        }
                    }

                    boolean completed = process.waitFor(5, TimeUnit.MINUTES);

                    if (!completed) {
                        process.destroyForcibly();
                        throw new IOException("壓縮操作超時");
                    }

                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        throw new IOException("壓縮失敗，錯誤碼: " + exitCode);
                    }
                } finally {
                    tempRar.toFile().deleteOnExit();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("壓縮過程被中斷", e);
        } catch (IOException e) {
            log.error("RAR 壓縮錯誤", e);
            throw new IOException("檔案壓縮錯誤: " + e.getMessage(), e);
        }
    }

    @Override
    public void decompress(File input, File output, CompressionOptions options) throws IOException {
        if (!output.exists())
            output.mkdirs();
        log.info("進來的password: {}", options.getPassword());
        try {
            String unrarExeName = "unrar.exe";

            // 從資源中讀取可執行檔
            try (InputStream is = getClass().getResourceAsStream("/bin/" + unrarExeName)) {
                if (is == null) {
                    throw new IOException("無法找到解壓縮工具: " + unrarExeName);
                }

                Path tempUnrar = Files.createTempFile("unrar_temp", ".exe");
                Files.copy(is, tempUnrar, StandardCopyOption.REPLACE_EXISTING);
                tempUnrar.toFile().setExecutable(true);

                try {
                    ProcessBuilder pb;
                    if (options.getPassword() != null && !options.getPassword().isEmpty()) {
                        pb = new ProcessBuilder(tempUnrar.toString(), "x",
                                "-p" + options.getPassword(),
                                "-o+", // 覆蓋已存在的檔案
                                input.getAbsolutePath(),
                                output.getAbsolutePath() + "/"
                        );
                    } else {
                        pb = new ProcessBuilder(tempUnrar.toString(), "x",
                                "-p-",
                                "-o+", // 覆蓋已存在的檔案
                                "-y",
                                input.getAbsolutePath(),
                                output.getAbsolutePath() + "/"
                        );
                    }

                    log.info("------>準備開始用rar解壓縮");

                    // 輸出執行過程
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.info("UnRAR output: {}", line);
                        }
                    }

                    boolean completed = process.waitFor(5, TimeUnit.MINUTES);

                    if (!completed) {
                        process.destroyForcibly();
                        throw new IOException("解壓縮操作超時");
                    }

                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        if (exitCode == 10) {
                            throw new IOException("檔案通行碼錯誤");
                        } else {
                            throw new IOException("解壓縮失敗，錯誤碼: " + exitCode);
                        }
                    }
                } finally {
                    tempUnrar.toFile().deleteOnExit();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("解壓縮過程被中斷", e);
        } catch (IOException e) {
            log.error("rar解壓縮錯誤", e);
            throw new IOException("檔案壓縮錯誤: " + e.getMessage(), e);
        }
    }
}
