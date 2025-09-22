package com.maxiaowei.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class TestController {
    /**
     * 接口中没有任何处理代码，直接返回结果
     *
     * @return
     */
    @GetMapping("/test1")
    public String test1() {
        log.info("test1");
        return "ok";
    }

    /**
     * 接口中休眠100毫秒，用来模拟业务操作
     *
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/test2")
    public String test2() throws InterruptedException {
        //接口中休眠100毫秒，用来模拟业务操作
        TimeUnit.MILLISECONDS.sleep(100);
        return "ok";
    }
}
