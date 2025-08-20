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

@Slf4j
public class RarArchiver implements Archiver {
    @Override
    public void compress(File input, File output, CompressionOptions options) throws IOException {
        log.debug("傳進來的password: {}", options.getPassword());
        try {
            String rarExeName = "rar.exe";

            // 從資源中讀取可執行檔
            try (InputStream is = getClass().getResourceAsStream("/bin/" + rarExeName)) {
                if (is == null) {
                    throw new IOException("無法找到壓縮工具: " + rarExeName);
                }

                // 創建臨時檔案
                Path tempRar = Files.createTempFile("rar_temp", ".exe");
                Files.copy(is, tempRar, StandardCopyOption.REPLACE_EXISTING);
                tempRar.toFile().setExecutable(true);

                try {
                    // 建立命令列參數
                    List<String> commands = new ArrayList<>();
                    commands.add(tempRar.toString());
                    commands.add("a"); // 添加到壓縮檔

                    if (options.getPassword() != null && !options.getPassword().isEmpty()) {
                        commands.add("-p" + options.getPassword()); // 設定密碼
                    }

                    commands.add("-ep1"); // 不包含基本目錄名稱
                    commands.add("-m5"); // 最大壓縮率
                    commands.add(output.getAbsolutePath()); // 目標 RAR 檔案路徑
                    commands.add(input.getAbsolutePath()); // 源文件或目錄路徑

                    ProcessBuilder pb = new ProcessBuilder(commands);

                    log.info("------>準備開始用 RAR 壓縮");

                    // 輸出執行過程的日誌
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    // 讀取並記錄輸出
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.debug("RAR output: {}", line);
                        }
                    }

                    // 等待進程結束，設定最大等待時間
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
        log.debug("進來的password: {}", options.getPassword());
        try {
            String unrarExeName = "unrar.exe";

            // 從資源中讀取可執行檔
            try (InputStream is = getClass().getResourceAsStream("/bin/" + unrarExeName)) {
                if (is == null) {
                    throw new IOException("無法找到解壓縮工具: " + unrarExeName);
                }

                // 創建臨時檔案
                Path tempUnrar = Files.createTempFile("unrar_temp", ".exe");
                Files.copy(is, tempUnrar, StandardCopyOption.REPLACE_EXISTING);
                tempUnrar.toFile().setExecutable(true);

                try {
                    // 建立命令列參數
                    ProcessBuilder pb;
                    if (options.getPassword() != null && !options.getPassword().isEmpty()) {
                        pb = new ProcessBuilder(tempUnrar.toString(), "x", // 提取並保留路徑
                                "-p" + options.getPassword(), // 設定密碼
                                "-o+", // 覆蓋已存在的檔案
                                input.getAbsolutePath(), // RAR 檔案路徑
                                output.getAbsolutePath() + "/" // 目標目錄
                        );
                    } else {
                        pb = new ProcessBuilder(tempUnrar.toString(), "x", // 提取並保留路徑
                                "-p-",
                                "-o+", // 覆蓋已存在的檔案
                                "-y",
                                input.getAbsolutePath(), // RAR 檔案路徑
                                output.getAbsolutePath() + "/" // 目標目錄
                        );
                    }

                    log.info("------>準備開始用rar解壓縮");

                    // 輸出執行過程的日誌
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    // 讀取並記錄輸出
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.debug("UnRAR output: {}", line);
                        }
                    }

                    // 等待進程結束，設定最大等待時間
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
