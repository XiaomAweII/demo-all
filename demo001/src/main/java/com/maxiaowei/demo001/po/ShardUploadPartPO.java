package com.maxiaowei.demo001.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_shard_upload_part")
public class ShardUploadPartPO {
    private String id;

    /**
     * 分片任务id（t_shard_upload.id
     */
    private String shardUploadId;

    /**
     * 第几个分片，从1开始
     */
    private Integer partOrder;

    /**
     * 当前分片文件完整路径
     */
    private String fileFullPath;
}
