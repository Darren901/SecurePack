package com.file.securepack.utils;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * @author Darren
 * @date 2025-08-22
 */
public class SevenZOutItemCallback implements IOutCreateCallback<IOutItem7z>, ICryptoGetTextPassword {
    private final File fileToCompress;
    private final String password;

    public SevenZOutItemCallback(File fileToCompress, String password) {
        this.fileToCompress = fileToCompress;
        this.password = password;
    }

    @Override
    public void setOperationResult(boolean b) throws SevenZipException {

    }

    @Override
    public IOutItem7z getItemInformation(int i, OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException {
        IOutItem7z outItem = outItemFactory.createOutItem();
        // 設定檔案名稱
        outItem.setPropertyPath(fileToCompress.getName());
        outItem.setPropertyLastModificationTime(new Date());
        outItem.setDataSize(fileToCompress.length());
        return outItem;
    }

    @Override
    public ISequentialInStream getStream(int i) throws SevenZipException {
        try {
            // 提供檔案的輸入流
            return new RandomAccessFileInStream(new RandomAccessFile(fileToCompress, "r"));
        } catch (FileNotFoundException e) {
            throw new SevenZipException("File not found: " + fileToCompress.getAbsolutePath(), e);
        }
    }

    @Override
    public void setTotal(long l) throws SevenZipException {

    }

    @Override
    public void setCompleted(long l) throws SevenZipException {

    }

    @Override
    public String cryptoGetTextPassword() throws SevenZipException {
        return password;
    }
}
