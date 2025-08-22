package com.file.securepack.controller;

import com.file.securepack.common.Constant;
import com.file.securepack.service.ArchiverWebService;
import com.file.securepack.vo.CompressionOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ArchiverWebController {

    private final ArchiverWebService archiverWebService;

    @GetMapping("/")
    public String home(){
        return "securePack";
    }

    @PostMapping("/compress")
    @ResponseBody
    public ResponseEntity<?> compress(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "zip") String format) {

        CompressionOptions co = new CompressionOptions();
        co.setPassword(password);
        co.setFormat(format);

        try{
            File tempExtractFile = archiverWebService.process(file, co, Constant.ACTION.COMPRESS);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(tempExtractFile));
            HttpHeaders headers = new HttpHeaders();

            // 處理中文檔案名稱編碼
            String encodedFilename = encodeFilename(tempExtractFile.getName());
            log.info("tempExtractFile.getName(): {},  encodedFilename: {}", tempExtractFile.getName(), encodedFilename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + encodedFilename);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(tempExtractFile.length()));

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (IOException e){
            log.error("處理檔案時發生IO錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("處理檔案時發生IO錯誤: " + e.getMessage());
        }catch (Exception e) {
            log.error("處理檔案時發生未預期錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("處理檔案時發生未預期錯誤: " + e.getMessage());
        }
    }

    @PostMapping("/decompress")
    @ResponseBody
    public ResponseEntity<?> decompress(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String format) {

        CompressionOptions co = new CompressionOptions();
        co.setPassword(password);

        // 從檔名推斷格式
        String fileName = file.getOriginalFilename();
        if (format == null && fileName != null) {
            if (fileName.toLowerCase().endsWith(".rar")) {
                co.setFormat("rar");
            } else if (fileName.toLowerCase().endsWith(".7z")) {
                co.setFormat("7z");
            } else {
                co.setFormat("zip");
            }
        } else {
            co.setFormat(format != null ? format : "zip");
        }

        try{
            File tempExtractFile = archiverWebService.process(file, co, Constant.ACTION.DECOMPRESS);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(tempExtractFile));
            HttpHeaders headers = new HttpHeaders();

            // 處理中文檔案名稱編碼
            String encodedFilename = encodeFilename(tempExtractFile.getName());
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + encodedFilename);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(tempExtractFile.length()));

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (IOException e){
            log.error("處理檔案時發生IO錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("處理檔案時發生IO錯誤: " + e.getMessage());
        }catch (Exception e) {
            log.error("處理檔案時發生未預期錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("處理檔案時發生未預期錯誤: " + e.getMessage());
        }
    }

    /**
     * 處理中文檔案名稱編碼，支援不同瀏覽器
     */
    private String encodeFilename(String filename) {
        // RFC 5987 標準編碼方式，支援 UTF-8 中文檔名
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20"); // 空格處理

        // 同時提供兩種格式以支援不同瀏覽器
        return String.format("filename=\"%s\"; filename*=UTF-8''%s",
                toAsciiFilename(filename), encodedFilename);
    }

    /**
     * 將中文檔案名稱轉換為 ASCII 相容格式
     */
    private String toAsciiFilename(String filename) {
        // 移除或替換非 ASCII 字符
        return filename.replaceAll("[^\\x00-\\x7F]", "_");
    }
}