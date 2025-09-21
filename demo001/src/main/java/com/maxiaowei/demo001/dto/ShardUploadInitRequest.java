package com.maxiaowei.demo001.dto;

import lombok.Data;


@Data
public class ShardUploadInitRequest {
    /**
     * 文件名称
     */

    private String fileName;

    /**
     * 分片数量
     */
    private Integer partNum;

    /**
     * 文件md5字
     */
    private String md5;

}
