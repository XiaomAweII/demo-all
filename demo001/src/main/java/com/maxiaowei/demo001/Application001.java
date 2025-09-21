package com.maxiaowei.demo001;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分片上传
 */
@SpringBootApplication
@MapperScan(basePackages = "com.maxiaowei.demo001.mapper")
public class Application001 {
    public static void main(String[] args) {
        SpringApplication.run(Application001.class, args);
    }
}
