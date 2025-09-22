package com.maxiaowei;

import com.maxiaowei.utils.LoadRunnerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 功能描述:
 * <p>
 * 作者: maxiaowei
 */
public class LoadRunnerUtilsTest {
    @Test
    public void test1() throws InterruptedException {
        //需要压测的接口地址，这里我们压测test1接口
        //压测参数，总请求数量1000，并发100
        int requests = 1000;
        int concurrency = 100;
        String url = "http://localhost:8080/test1";
        System.out.println(String.format("压测接口:%s", url));
        RestTemplate restTemplate = new RestTemplate();

        //调用压测工具类开始压测
        LoadRunnerUtils.LoadRunnerResult loadRunnerResult = LoadRunnerUtils.run(requests, concurrency, () -> {
            restTemplate.getForObject(url, String.class);
        });

        //输出压测结果
        print(loadRunnerResult);
    }

    @Test
    public void test2() throws InterruptedException {
        //需要压测的接口地址，这里我们压测test2接口
        //压测参数，总请求数量10000，并发100
        int requests = 1000;
        int concurrency = 100;
        String url = "http://localhost:8080/test2";
        System.out.println(String.format("压测接口:%s", url));
        RestTemplate restTemplate = new RestTemplate();

        //调用压测工具类开始压测
        LoadRunnerUtils.LoadRunnerResult loadRunnerResult = LoadRunnerUtils.run(requests, concurrency, () -> {
            restTemplate.getForObject(url, String.class);
        });

        //输出压测结果
        print(loadRunnerResult);
    }

    public void print(LoadRunnerUtils.LoadRunnerResult loadRunnerResult) {
        System.out.println("\n压测结果如下：");
        System.out.println("==============================");
        printFormat("请求总数", loadRunnerResult.getRequests());
        printFormat("并发量", loadRunnerResult.getConcurrency());
        printFormat("成功请求数", loadRunnerResult.getSuccessRequests());
        printFormat("失败请求数", loadRunnerResult.getFailRequests());
        printFormat("请求总耗时(ms)", loadRunnerResult.getTimeTakenForTests());
        printFormat("每秒请求数(吞吐量)", loadRunnerResult.getRequestsPerSecond());
        printFormat("每个请求平均耗时(ms)", loadRunnerResult.getTimePerRequest());
        printFormat("最快的请求耗时(ms)", loadRunnerResult.getFastestCostTime());
        printFormat("最慢的请求耗时(ms)", loadRunnerResult.getSlowestCostTime());
        System.out.println("==============================");
    }

    public void printFormat(Object p1, Object p2) {
        System.out.println(String.format("%s: %s", p1, p2));
    }
}
