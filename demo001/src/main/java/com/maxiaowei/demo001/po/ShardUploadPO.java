package com.maxiaowei.demo001.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_shard_upload")
public class ShardUploadPO {
    private String id;

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

    /**
     * 文件最终存储完整路径
     */
    private String fileFullPath;
}
