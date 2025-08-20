package com.file.securepack.archive;

import com.file.securepack.vo.CompressionOptions;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class ZipArchiver implements Archiver {
    @Override
    public void compress(File input, File output, CompressionOptions options) throws IOException {
        ZipParameters zipParameters = new ZipParameters();
        try(ZipFile zipFile = new ZipFile(output.getAbsolutePath())) {
            log.info("傳進來的選項: {}", options.toString());

            if(options.getPassword() != null && !options.getPassword().isEmpty()) {
                log.info("設置加密參數");
                zipParameters.setEncryptFiles(true);
                zipParameters.setEncryptionMethod(EncryptionMethod.AES);
                zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
                zipFile.setPassword(options.getPassword().toCharArray());
            }else{
                log.info("檔案不加密");
                zipParameters.setEncryptFiles(false);
            }

            log.info("開始添加檔案到壓縮檔: {}", input.getAbsolutePath());
            if (input.isDirectory()) {
                log.info("壓縮資料夾: {}", input.getAbsolutePath());
                zipFile.addFolder(input, zipParameters);
            } else {
                log.info("壓縮檔案: {}", input.getAbsolutePath());
                zipFile.addFile(input, zipParameters);
            }
            log.info("zipFile壓縮成功: {}", output.getAbsolutePath());
        }catch (ZipException e) {
            log.error("Zip壓縮過程發生錯誤", e);
            throw new IOException("壓縮檔案失敗: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("壓縮過程發生未預期錯誤", e);
            throw new IOException("壓縮過程發生錯誤: " + e.getMessage(), e);
        }
    }

    @Override
    public void decompress(File input, File output, CompressionOptions options) throws IOException {
        ZipFile zipFile = null;
        try{
            if(options.getPassword() != null && !options.getPassword().isEmpty()) {
                zipFile = new ZipFile(input.getAbsolutePath(),  options.getPassword().toCharArray());
            }else{
                zipFile = new ZipFile(input.getAbsolutePath());
            }
            log.info("------>準備開始用zip解壓縮");
            zipFile.extractAll(output.getAbsolutePath());
        }catch (ZipException e) {
            if (e.getMessage().contains("password")) {
                throw new IOException("密碼錯誤", e);
            } else {
                log.error("壓縮過程發生未預期錯誤", e);
                throw new IOException("壓縮過程發生錯誤: " + e.getMessage(), e);
            }
        } finally {
            assert zipFile != null;
            zipFile.close();
        }
    }
}
