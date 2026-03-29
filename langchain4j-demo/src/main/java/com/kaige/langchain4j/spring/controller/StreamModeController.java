package com.kaige.langchain4j.spring.controller;

import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LangChain4j Stream模式Spring Boot控制器示例
 * <p>
 * 演示如何在Spring Boot应用中实现流式AI响应。
 * 提供两种流式处理方式的REST API端点。
 *
 * <p><strong>API端点：</strong></p>
 * <ul>
 *   <li>GET /stream/callback - Callback方式流式响应</li>
 *   <li>GET /stream/reactive - Reactive方式流式响应</li>
 *   <li>POST /stream/chat - POST方式的流式聊天</li>
 * </ul>
 *
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>实时聊天应用</li>
 *   <li>长文本生成展示</li>
 *   <li>需要即时反馈的AI应用</li>
 * </ul>
 *
 * @author kaige
 * @version 1.0
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/stream")
@Slf4j
public class StreamModeController {

    @Autowired
    private OpenAiStreamingChatModel streamingChatModel;

    @Autowired
    private StreamingAssistant streamingAssistant;

    @GetMapping(value = "/name-normal")
    public List<String> normal() {
        return List.of("Kaige-normal", "Kaige1-normal", "Kaige2-normal");
    }

    @GetMapping(value = "/name")
    public Mono<List<String>> streamWithReactive(@RequestParam(value = "message") String message) {

        log.info("来自用户的请求 message = {}", message);


        // 使用OpenAiStreamingChatModel实现真正的异步AI调用
        return Mono.fromCallable(() -> {
            try {
                // 收集流式响应的结果
                List<String> responses = new ArrayList<>();

                // 使用CountDownLatch等待流式处理完成
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Exception> errorRef = new AtomicReference<>();

                // 执行流式AI调用
                streamingChatModel.chat(message, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        if (partialResponse != null && !partialResponse.trim().isEmpty()) {
                            responses.add(partialResponse);
                        }
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable error) {
                        errorRef.set(new RuntimeException("AI调用失败: " + error.getMessage(), error));
                        latch.countDown();
                    }
                });

                // 等待流式处理完成（设置超时避免无限等待）
                if (!latch.await(300, TimeUnit.SECONDS)) {
                    throw new RuntimeException("AI调用超时");
                }

                // 检查是否有错误
                if (errorRef.get() != null) {
                    throw errorRef.get();
                }

                return responses;

            } catch (Exception e) {
                throw new RuntimeException("处理AI响应时出错: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic()); // 使用专用线程池执行耗时操作
    }


    /**
     * Callback方式的流式响应
     * 使用Server-Sent Events (SSE) 格式返回流式数据
     */
    @GetMapping(value = "/callback", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamWithCallback(@RequestParam(defaultValue = "你好") String message) {

        log.info("来自用户的请求 message = {}", message);

        return Flux.create(sink -> {
            streamingChatModel.chat(message, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
                    // 带上下文的部分响应处理
                    if (partialResponse != null && partialResponse.text() != null) {
                        // String eventData = "data: " + partialResponse.text().replace("\n", "\\n") + "\n\n";
                        // sink.next(eventData);

                        // try {
                            // 模拟延迟
                            // Thread.sleep(10L);
                        // } catch (InterruptedException e) {
                        //     throw new RuntimeException(e);
                        // }

                        sink.next(partialResponse.text());

                        // 可以根据上下文做特殊处理
                        if (context != null && shouldStopStreaming(context)) {
                            context.streamingHandle().cancel();
                            sink.next("data: [STOPPED]\n\n");
                            sink.complete();
                        }
                    }
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
                    // 带上下文的思考处理
                    if (partialThinking != null) {
                        String thinkingData = "data: [THINKING_CONTEXT] " + partialThinking.text() + "\n\n";
                        sink.next(thinkingData);
                    }
                }


                @Override
                public void onPartialToolCall(PartialToolCall partialToolCall, PartialToolCallContext context) {
                    // 带上下文的工具调用处理
                    // if (partialToolCall != null) {
                    //     String toolData = "data: [TOOL_CALL_DETAIL] " +
                    //             partialToolCall.name() + ": " + partialToolCall.arguments() + "\n\n";
                    //     sink.next(toolData);
                    // }
                    System.out.println("partialToolCall = " + partialToolCall);
                }

                @Override
                public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                    // 完整工具调用完成
                    // if (completeToolCall != null) {
                    //     String completeToolData = "data: [TOOL_COMPLETED] " +
                    //             completeToolCall.name() + " -> " + completeToolCall.result() + "\n\n";
                    //     sink.next(completeToolData);
                    // }
                    System.out.println("completeToolCall = " + completeToolCall);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    // 发送完成信号
                    sink.next("data: [DONE]\n\n");
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    // 错误处理
                    String errorData = "data: [ERROR] " + error.getMessage() + "\n\n";
                    sink.next(errorData);
                    sink.error(error);
                }
            });
        });
    }


    @GetMapping(value = "/streaming-ai", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamingAssistant(@RequestParam(value = "message", defaultValue = "当前时间是?") String message) {
        return streamingAssistant.chat(message).map(response -> {
            log.info("接收到的数据: {}", response);
            return response;
        });
    }


    // 辅助方法

    private boolean shouldStopStreaming(PartialResponseContext context) {
        // 实现停止流式传输的逻辑
        // 例如：达到特定关键词时停止
        return false;
    }

    private String processMessageWithContext(String message, String context) {
        // 根据上下文处理消息
        if (context != null && !context.isEmpty()) {
            return context + "\n\n" + message;
        }
        return message;
    }

    private void logChatCompletion(ChatRequest request) {
        // 记录聊天完成日志
        System.out.println("聊天完成 - 消息: " + request.getMessage());
    }


    /**
     * 聊天请求数据传输对象
     */
    @Data
    public static class ChatRequest {
        private String message;
        private String context;
    }
}