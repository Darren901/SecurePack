package com.file.securepack.service;

import com.file.securepack.archive.Archiver;
import com.file.securepack.archive.ArchiverFactory;
import com.file.securepack.common.Constant;
import com.file.securepack.vo.CompressionOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ArchiverWebService {

    public File process(MultipartFile file, CompressionOptions co, Constant.ACTION action) throws IOException {
        File uploadFile = null;
        try{
            Archiver archiver = ArchiverFactory.getArchiver(co.getFormat());

            File workDirFile = new File(Constant.WORK_DIR);
            if(!workDirFile.exists()){workDirFile.mkdirs();}

            String uploadPath = Constant.WORK_DIR + "/" + file.getOriginalFilename();
            uploadFile = new File(uploadPath);
            if(!uploadFile.exists()) uploadFile.getParentFile().mkdirs();

            file.transferTo(uploadFile);

            String tempExtractPath = Constant.WORK_DIR + "/temp_" + System.currentTimeMillis();
            File tempExtractFile;
            if(action == Constant.ACTION.COMPRESS) {
                String baseName = file.getOriginalFilename();
                if (baseName.contains(".")) {
                    baseName = baseName.substring(0, baseName.lastIndexOf("."));
                }
                if(co.getFormat().equals("zip")) {
                    tempExtractPath = tempExtractPath + "/" + baseName + ".zip";
                } else if(co.getFormat().equals("rar")) {
                    tempExtractPath = tempExtractPath + "/" + baseName + ".rar";
                } else {
                    tempExtractPath = tempExtractPath + "/" + baseName + ".7z";
                }
                tempExtractFile = new File(tempExtractPath);
                if(!tempExtractFile.exists()) tempExtractFile.getParentFile().mkdirs();
                archiver.compress(uploadFile, tempExtractFile, co);
                return tempExtractFile;
            }else{
                tempExtractFile = new File(tempExtractPath);
                if(!tempExtractFile.exists()) tempExtractFile.mkdirs();
                archiver.decompress(uploadFile, tempExtractFile, co);

                // 將解壓縮後的檔案包成一個zip回傳給使用者
                CompressionOptions co1 = new CompressionOptions();
                co1.setFormat("zip");
                File zipFile = new File(tempExtractFile + "/extractFile.zip");
                archiver.compress(tempExtractFile, zipFile, co1);
                return zipFile;
            }
        } finally {
            if (uploadFile != null && uploadFile.exists()) {
                uploadFile.delete();
            }
        }
    }
}
