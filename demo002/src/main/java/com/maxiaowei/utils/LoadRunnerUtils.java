package com.maxiaowei.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 压测工具类
 */
@Slf4j
public class LoadRunnerUtils {
    @Data
    public static class LoadRunnerResult {
        // 请求总数
        private int requests;
        // 并发量
        private int concurrency;
        // 成功请求数
        private int successRequests;
        // 失败请求数
        private int failRequests;
        // 请求总耗时(ms)
        private int timeTakenForTests;
        // 每秒请求数（吞吐量）
        private float requestsPerSecond;
        // 每个请求平均耗时(ms)
        private float timePerRequest;
        // 最快的请求耗时(ms)
        private float fastestCostTime;
        // 最慢的请求耗时(ms)
        private float slowestCostTime;
    }

    /**
     * 对 command 执行压测
     *
     * @param requests    总请求数
     * @param concurrency 并发数量
     * @param command     需要执行的压测代码
     * @param <T>
     * @return 压测结果 {@link LoadRunnerResult}
     * @throws InterruptedException
     */
    public static <T> LoadRunnerResult run(int requests, int concurrency, Runnable command) throws InterruptedException {
        log.info("压测开始......");
        //创建线程池，并将所有核心线程池都准备好
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(concurrency, concurrency,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        poolExecutor.prestartAllCoreThreads();

        // 创建一个 CountDownLatch，用于阻塞当前线程池待所有请求处理完毕后，让当前线程继续向下走
        CountDownLatch countDownLatch = new CountDownLatch(requests);

        //成功请求数、最快耗时、最慢耗时 （这几个值涉及到并发操作，所以采用 AtomicInteger 避免并发修改导致数据错误）
        AtomicInteger successRequests = new AtomicInteger(0);
        AtomicInteger fastestCostTime = new AtomicInteger(Integer.MAX_VALUE);
        AtomicInteger slowestCostTime = new AtomicInteger(Integer.MIN_VALUE);

        long startTime = System.currentTimeMillis();
        //循环中使用线程池处理被压测的方法
        for (int i = 0; i < requests; i++) {
            poolExecutor.execute(() -> {
                try {
                    long requestStartTime = System.currentTimeMillis();
                    //执行被压测的方法
                    command.run();

                    //command执行耗时
                    int costTime = (int) (System.currentTimeMillis() - requestStartTime);

                    //请求最快耗时
                    setFastestCostTime(fastestCostTime, costTime);

                    //请求最慢耗时
                    setSlowestCostTimeCostTime(slowestCostTime, costTime);

                    //成功请求数+1
                    successRequests.incrementAndGet();
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        //阻塞当前线程，等到压测结束后，该方法会被唤醒，线程继续向下走
        countDownLatch.await();
        //关闭线程池
        poolExecutor.shutdown();

        long endTime = System.currentTimeMillis();
        log.info("压测结束，总耗时(ms):{}", (endTime - startTime));


        //组装最后的结果返回
        LoadRunnerResult result = new LoadRunnerResult();
        result.setRequests(requests);
        result.setConcurrency(concurrency);
        result.setSuccessRequests(successRequests.get());
        result.setFailRequests(requests - result.getSuccessRequests());
        result.setTimeTakenForTests((int) (endTime - startTime));
        result.setRequestsPerSecond((float) requests * 1000f / (float) (result.getTimeTakenForTests()));
        result.setTimePerRequest((float) result.getTimeTakenForTests() / (float) requests);
        result.setFastestCostTime(fastestCostTime.get());
        result.setSlowestCostTime(slowestCostTime.get());
        return result;
    }

    private static void setFastestCostTime(AtomicInteger fastestCostTime, int costTime) {
        while (true) {
            int fsCostTime = fastestCostTime.get();
            if (fsCostTime < costTime) {
                break;
            }
            if (fastestCostTime.compareAndSet(fsCostTime, costTime)) {
                break;
            }
        }
    }

    private static void setSlowestCostTimeCostTime(AtomicInteger slowestCostTime, int costTime) {
        while (true) {
            int slCostTime = slowestCostTime.get();
            if (slCostTime > costTime) {
                break;
            }
            if (slowestCostTime.compareAndSet(slCostTime, costTime)) {
                break;
            }
        }
    }
}
