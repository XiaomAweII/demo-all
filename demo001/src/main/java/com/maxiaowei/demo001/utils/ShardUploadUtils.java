package com.maxiaowei.demo001.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ShardUploadUtils {
    /**
     * 当文件不存在的时候创建文件
     *
     * @param file
     * @throws IOException
     */
    public static File createFileNotExists(File file) throws IOException {
        if (!file.exists()) {
            FileUtils.forceMkdirParent(file);
            file.createNewFile();
        }
        return file;
    }

    /**
     * 获取分片数量
     *
     * @param fileSize 文件大小（byte）
     * @param partSize 分片大小（byte）
     * @return
     */
    public static int shardNum(long fileSize, long partSize) {
        if (fileSize % partSize == 0) {
            return (int) (fileSize / partSize);
        } else {
            return (int) (fileSize / partSize) + 1;
        }
    }
}
