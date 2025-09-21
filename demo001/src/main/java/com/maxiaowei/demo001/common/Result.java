package com.maxiaowei.demo001.common;

import lombok.Builder;
import lombok.Data;

@Data
public class Result<T> {
    /**
     * 编码值
     */
    private String code;

    /**
     * 返回结果
     */
    private T data;

    /**
     * 提示消息
     */
    private String msg;
}
