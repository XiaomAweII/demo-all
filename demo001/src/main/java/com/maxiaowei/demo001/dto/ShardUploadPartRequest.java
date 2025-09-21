package com.maxiaowei.demo001.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ShardUploadPartRequest {
    /**
     * 分片上传任务id（由初始化分片接口返回的）
     */
    private String shardUploadId;

    /**
     * 第几个分片
     */
    private Integer partOrder;


    /**
     * 分片文件
     */
    private MultipartFile file;
}
