package com.kaige.langchain4j;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * LangChain4j Stream模式学习示例（AiServices版本）
 *
 * @author kaige
 * @version 1.0
 * @since 2026-02-14
 */
public class Learn_02_Stream_Mode_Demo {

    public static void main(String[] args) {
        // 明确指定使用的HTTP客户端，解决冲突问题
        System.setProperty("langchain4j.http.clientBuilderFactory",
                          "dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory");
        
        demoStreamMode();
    }

    /**
     * Stream模式演示主方法
     */
    private static void demoStreamMode() {

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);

        // 创建流式模型，显式指定HTTP客户端工厂
        OllamaStreamingChatModel streamingModel = OllamaStreamingChatModel.builder()
                .modelName("qwen:4b")
                .baseUrl("http://localhost:11434")
                // .httpClientBuilder(jdkHttpClientBuilder)
                .build();

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== LangChain4j Stream模式演示 ===");
        System.out.println("请输入问题，AI将逐步回复（输入'quit'退出）：");

        while (true) {
            System.out.print("\n👤 你的问题: ");
            String question = scanner.nextLine().trim();

            if ("quit".equalsIgnoreCase(question)) {
                System.out.println("👋 再见！");
                break;
            }

            if (question.isEmpty()) {
                System.out.println("⚠️  请输入有效问题");
                continue;
            }

            // 执行流式处理
            processStreamResponse(streamingModel, question);
        }

        scanner.close();
    }

    /**
     * 处理流式响应的核心方法
     *
     */
    private static void processStreamResponse(OllamaStreamingChatModel model, String question) {
        System.out.println("\n🤖 AI正在思考并逐步回复...");
        System.out.println("========================================");

        CountDownLatch latch = new CountDownLatch(1);

        try {
            CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

            model.chat(question, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    // 关键：逐个token接收并立即显示
                    System.out.print(partialResponse);
                    System.out.flush(); // 立即刷新输出缓冲区
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    System.out.println("\n========================================");
                    System.out.println("✅ 回复完成！");
                    System.out.println("\n\nDone streaming");
                    futureChatResponse.complete(completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    futureChatResponse.completeExceptionally(error);
                    System.err.println("\n❌ 错误: " + error.getMessage());
                }
            });

            // 等待流式处理完成
            futureChatResponse.join();


        } catch (Exception e) {
            System.err.println("❌ 处理异常: " + e.getMessage());
        }
    }
}