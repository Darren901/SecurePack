package com.file.securepack.archive;

import com.file.securepack.utils.SevenZOutItemCallback;
import com.file.securepack.vo.CompressionOptions;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class SevenZArchiver implements Archiver {
    static {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            System.out.println("7-Zip-JBinding library was initialized");
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void compress(File input, File output, CompressionOptions options) throws IOException {
        IOutCreateArchive7z outArchive = null;
        RandomAccessFileOutStream outStream = null;
        try {
            outArchive = SevenZip.openOutArchive7z();
            outArchive.setHeaderEncryption(true);
            log.info("設置表頭加密");

            outStream = new RandomAccessFileOutStream(new RandomAccessFile(output, "rw"));
            log.info("輸出流創建完成 rw");

            log.info("準備壓縮檔案 加密密碼: {} , 輸入流創建完成 r", options.getPassword());
            outArchive.createArchive(outStream, 1, new SevenZOutItemCallback(input, options.getPassword()));
            log.info("SevenZ File 壓縮成功...");
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ignore) {
                }
            }
            IOUtils.closeQuietly(outArchive);
        }
    }

    @Override
    public void decompress(File input, File output, CompressionOptions options) throws IOException {
        RandomAccessFileInStream inStream = null;
        IInArchive inArchive = null;
        try{
            RandomAccessFile randomAccessFile = new RandomAccessFile(input, "r");
            inStream = new RandomAccessFileInStream(randomAccessFile);
            boolean usePassword = options.getPassword() != null && !options.getPassword().isEmpty();
            if(usePassword) {
                inArchive = SevenZip.openInArchive(null, inStream, options.getPassword());
            }else{
                inArchive = SevenZip.openInArchive(null, inStream);
            }

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
            ISimpleInArchiveItem[] archiveItems = simpleInArchive.getArchiveItems();
            for (ISimpleInArchiveItem item : archiveItems) {
                RandomAccessFileOutStream rafo = null;
                try {
                    File file;
                    if (item.isFolder()) {
                        new File(output , item.getPath()).mkdirs();
                        continue;
                    } else {
                        file = new File(output , item.getPath());
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    }

                    rafo = new RandomAccessFileOutStream(new RandomAccessFile(file , "rw"));
                    if (usePassword) {
                        item.extractSlow(rafo , options.getPassword());
                    } else {
                        item.extractSlow(rafo);
                    }
                } finally {
                    if (rafo != null) {
                        rafo.close();
                    }
                }
            }
        }catch (Exception e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(inArchive);
        }
    }
}

