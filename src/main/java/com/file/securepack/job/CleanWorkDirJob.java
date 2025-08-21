package com.file.securepack.job;

import com.file.securepack.common.Constant;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CleanWorkDirJob {

    @Scheduled(fixedRate = 3600000) // 每小時掃一次
    public void cleanWorkDir() {
        File targetDir = new File(Constant.WORK_DIR);
        if (targetDir.exists()) {
            File[] files = targetDir.listFiles();
            if (files != null) {
                long now = System.currentTimeMillis();
                for (File f : files) {
                    // 只刪掉 5 分鐘前的檔案
                    if (now - f.lastModified() > 5 * 60 * 1000) {
                        try {
                            if (f.isDirectory()) {
                                FileUtils.deleteDirectory(f);
                            } else {
                                f.delete();
                            }
                        } catch (Exception e) {
                            // log 但不要拋出
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
