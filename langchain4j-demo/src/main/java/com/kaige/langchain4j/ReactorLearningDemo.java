package com.kaige.langchain4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class ReactorLearningDemo {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Project Reactor 学习演示 ===\n");

        // 1. 基础Flux创建和操作
        demoBasicFlux();

        // 2. 流式AI响应模拟
        demoAiStreaming();

        // 3. 错误处理和背压
        demoErrorHandling();

        // 4. 并发处理
        demoConcurrency();

        System.out.println("\n=== 演示完成 ===");
    }

    /**
     * 基础Flux操作演示
     */
    private static void demoBasicFlux() {
        System.out.println("1. 基础Flux操作:");

        // 创建Flux并应用操作符
        Flux.just("Hello", "Reactive", "World")
                .map(String::toUpperCase)           // 转换操作
                .filter(s -> s.length() > 5)        // 过滤操作
                .doOnNext(item -> System.out.println("  处理项: " + item)) // 副作用
                .subscribe(
                        item -> System.out.println("  ✓ 接收: " + item),
                        error -> System.err.println("  ✗ 错误: " + error),
                        () -> System.out.println("  ✓ 完成\n")
                );
    }

    /**
     * 模拟AI流式响应
     */
    private static void demoAiStreaming() {
        System.out.println("2. AI流式响应模拟:");

        // 模拟AI逐步生成响应
        Flux.interval(Duration.ofMillis(200))   // 每200ms发射一次
                .map(i -> getAiToken(i.intValue())) // 获取AI token
                .takeWhile(token -> !"[END]".equals(token)) // 遇到结束标记停止
                .doOnNext(token -> System.out.print(token))
                .doOnComplete(() -> System.out.println("\n  ✓ AI响应完成\n"))
                .subscribe();

        // 等待流完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 错误处理演示
     */
    private static void demoErrorHandling() {
        System.out.println("3. 错误处理:");

        Flux.range(1, 5)
                .map(i -> {
                    if (i == 3) throw new RuntimeException("模拟网络错误");
                    return "Item " + i;
                })
                .onErrorResume(error -> {
                    System.out.println("  捕获错误: " + error.getMessage());
                    return Flux.just("Error Recovery Item");
                })
                .subscribe(
                        item -> System.out.println("  ✓ " + item),
                        error -> System.err.println("  ✗ 最终错误: " + error),
                        () -> System.out.println("  ✓ 错误处理完成\n")
                );
    }

    /**
     * 并发处理演示
     */
    private static void demoConcurrency() {
        System.out.println("4. 并发处理:");

        Flux.range(1, 3)
                .publishOn(Schedulers.boundedElastic()) // 切换到弹性线程池
                .flatMap(i ->
                        Mono.fromCallable(() -> simulateSlowOperation(i))
                                .subscribeOn(Schedulers.parallel()) // 并行执行
                )
                .doOnNext(result -> System.out.println("  处理结果: " + result))
                .subscribe(
                        result -> {},
                        error -> System.err.println("  ✗ 并发错误: " + error),
                        () -> System.out.println("  ✓ 并发处理完成\n")
                );
    }

    // 辅助方法

    private static String getAiToken(int index) {
        String[] tokens = {"你好", "！ ", "我", "是", "AI", "助", "手", "[END]"};
        return index < tokens.length ? tokens[index] : "[END]";
    }

    private static String simulateSlowOperation(int id) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
            return "操作" + id + "完成";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
