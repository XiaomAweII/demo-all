package com.maxiaowei.demo001.dto;

import lombok.Data;

import java.util.List;


@Data
public class ShardUploadDetailResponse {
    /**
     * 分片任务id
     */
    private String shardUploadId;

    /**
     * 分片数量
     */
    private Integer partNum;

    /**
     * 分片任务是否已上传完成
     */
    private Boolean success;

    /**
     * 已完成的分片任务编号列表
     */
    private List<Integer> partOrderList;
}
