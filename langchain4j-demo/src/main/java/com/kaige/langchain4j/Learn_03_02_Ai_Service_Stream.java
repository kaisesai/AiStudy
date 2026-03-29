package com.kaige.langchain4j;

import com.kaige.langchain4j.service.Assistant;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;

import java.time.Duration;

/**
 * langchain4j 学习：流式模式（Streaming）
 */
public class Learn_03_02_Ai_Service_Stream {

    public static void main(String[] args) {

        ChatModel chatModel = OpenAiChatModel
                .builder()
                .apiKey(Constants.API_KEY)
                .baseUrl(Constants.API_BASE_URL)
                .modelName(Constants.API_MODEL)
                .temperature(0.7).
                timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();


        // 创建千问API模型实例
        StreamingChatModel streamingChatModel = OpenAiStreamingChatModel
                .builder()
                .apiKey(Constants.API_KEY)
                .baseUrl(Constants.API_BASE_URL)
                .modelName(Constants.API_MODEL)
                .temperature(0.7).
                timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();



        // 配置模型
        // 本地模型 ollama
        // StreamingChatModel model = OllamaStreamingChatModel.builder()
        // .baseUrl("http://localhost:11434")
        // .modelName("qwen:4b")
        // .build();

        // 1. 流式服务 tokenStream
        // // 定义一个接口，返回值是token 流
        // interface Assistant {
        //     TokenStream chat(String message);
        // }

        // // 创建 AI 服务
        // Assistant assistant = AiServices.create(Assistant.class, model);
        //
        // // 执行请求，返回流
        // TokenStream tokenStream = assistant.chat("帮我讲个笑话");
        //
        // // 设置一个 future 等待流完成响应数据
        // CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();
        //
        // tokenStream
        //         // .onPartialResponseWithContext((PartialResponse partialResponse, PartialResponseContext context) -> {
        //         //     System.out.println("有部分响应结果: " + partialResponse);
        //         //     // 当出现关键字 你的表情 时停止
        //         //     if (partialResponse.text().contains("笑着说")) {
        //         //         // 取消流
        //         //         context.streamingHandle().cancel();
        //         //     }
        //         // })
        //         // 当有部分响应结果时输出
        //         .onPartialResponse((String partialResponse) -> System.out.println("有部分响应结果: " + partialResponse))
        //         // 当有部分思考时输出
        //         .onPartialThinking((PartialThinking partialThinking) -> System.out.println("有部分思考 : " + partialThinking))
        //         // 当有部分内容被检索时输出
        //         .onRetrieved((List<Content> contents) -> System.out.println("有部分内容被检索: " + contents))
        //         // 当有部分中间响应时输出
        //         .onIntermediateResponse((ChatResponse intermediateResponse) -> System.out.println("有部分中间响应: " + intermediateResponse))
        //         // This will be invoked every time a new partial tool call (usually containing a single token of the tool's arguments) is available.
        //         // 当有部分工具调用时输出
        //         .onPartialToolCall((PartialToolCall partialToolCall) -> System.out.println("有部分工具调用: " + partialToolCall))
        //         // This will be invoked right before a tool is executed. BeforeToolExecution contains ToolExecutionRequest (e.g. tool name, tool arguments, etc.)
        //         // 当工具被执行前输出
        //         .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> System.out.println("工具被执行前: " + beforeToolExecution))
        //         // This will be invoked right after a tool is executed. ToolExecution contains ToolExecutionRequest and tool execution result.
        //         // 工具被执行后输出
        //         .onToolExecuted((ToolExecution toolExecution) -> System.out.println("工具被执行后: " + toolExecution))
        //         // 当响应完成时，添加到 futureResponse
        //         .onCompleteResponse((ChatResponse response) -> {
        //             System.out.println("响应完成: " + response);
        //             futureResponse.complete(response);
        //         })
        //         // 当有错误时，添加到 futureResponse
        //         .onError((Throwable error) -> {
        //             System.out.println("有错误: " + error);
        //             futureResponse.completeExceptionally(error);
        //         })
        //         // 启动 流
        //         .start();
        //
        // //  等待流完成
        // futureResponse.join(); // Blocks the main thread until the streaming process (running in another thread) is complete
        //
        // System.exit(0);


        // 2. 聊天记忆

        // interface Assistant extends ChatMemoryAccess {
        //     String chat(@MemoryId int memoryId, @UserMessage String message);
        // }
        //
        // Assistant assistant = AiServices.builder(Assistant.class)
        //         .chatModel(chatModel)
        //         .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
        //         .build();
        //
        // String answerToKlaus = assistant.chat(1, "你好，我的名字叫做 Klaus");
        // System.out.println("answerToKlaus1 = " + answerToKlaus);
        //
        // String answerToFrancine = assistant.chat(2, "你好，我的名字叫做 Francine");
        // System.out.println("answerToFrancine1 = " + answerToFrancine);
        //
        //
        // answerToKlaus = assistant.chat(1, "我叫什么名字？");
        // System.out.println("answerToKlaus2 = " + answerToKlaus);
        //
        // answerToFrancine = assistant.chat(2, "我叫什么名字");
        // System.out.println("answerToFrancine2 = " + answerToFrancine);


    // 3. 工具使用

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(new Tools())
                .build();

        String answer = assistant.chat(" 1+2 等于多少？3*4 等于多少？");
        System.out.println("answer = " + answer);
    }


    static class Tools {

        @Tool
        int add(int a, int b) {
            return a + b;
        }

        @Tool
        int multiply(int a, int b) {
            return a * b;
        }
    }

}
