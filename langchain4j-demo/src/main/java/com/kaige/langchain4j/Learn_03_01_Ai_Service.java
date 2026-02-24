package com.kaige.langchain4j;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.*;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Learn_03_01_Ai_Service {

    public static void main(String[] args) {

        // 1. 配置模型 (连接你本地的 Ollama)
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Ollama 默认地址
                .modelName("qwen:4b") // 你刚才下载的模型
                .timeout(Duration.of(100, ChronoUnit.SECONDS))
                .responseFormat(ResponseFormat.JSON) // 响应格式设置为 json
                .build();

        // 3. 让框架自动创建这个接口的实现类
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);

        // Assistant assistant = AiServices.builder(Assistant.class)
        //         .chatModel(model)
        //         .chatMemory(chatMemory)
        //         .systemMessageProvider(chatMemoryId -> "你是我很好的朋友。用俚语回答。")
        //         .build();

        // Assistant assistant = AiServices.builder(Assistant.class)
        //         .chatModel(model)
        //         .chatMemory(chatMemory)
        //         .build();

        // Friend friend = AiServices.create(Friend.class, model);


        // 使用 Programmatic ChatRequest rewriting
        // Assistant assistant = AiServices.builder(Assistant.class)
        //         .chatModel(model)
        //         .chatMemory(chatMemory)
        //         .chatRequestTransformer((chatRequest, chatMemoryId) -> {
        //             // 从内存 ID 中获取其他信息
        //             System.out.println("chatRequest = " + chatRequest);
        //             System.out.println("chatMemoryId = " + chatMemoryId);
        //
        //             return chatRequest;
        //         })
        //         .build();

        // 带有自定义请求参数的 AI 服务
        // AssistantWithChatParams assistant = AiServices.builder(AssistantWithChatParams.class)
        //         .chatModel(model)  // or whichever model
        //         .build();

        // ChatRequestParameters chatRequestParameters = ChatRequestParameters.builder().temperature(0.85).build();

        // String answer = assistant.chat("hi 在这呢", chatRequestParameters);
        // System.out.println("大模型回复: " + answer);

        // 4. 开始对话
        // for (int i = 0; i < 1; i++) {
        //     String answer = assistant.chat("你好啊");
        //     // String answer = friend.chat("你好啊");
        //     System.out.println("大模型回复: " + answer);
        // }


        // 使用result 对象获取结果
        // Assistant assistant = AiServices.builder(Assistant.class)
        //         .chatModel(model)
        //         .chatMemory(chatMemory)
        //         .build();
        //
        // Result<List<String>> result = assistant.generateOutlineFor("Java");
        //
        // List<String> outline = result.content();
        // System.out.println("outline = " + outline);
        //
        // TokenUsage tokenUsage = result.tokenUsage();
        // System.out.println("tokenUsage = " + tokenUsage);
        //
        // List<Content> sources = result.sources();
        // System.out.println("sources = " + sources);
        //
        // List<ToolExecution> toolExecutions = result.toolExecutions();
        // System.out.println("toolExecutions = " + toolExecutions);
        //
        // FinishReason finishReason = result.finishReason();
        // System.out.println("finishReason = " + finishReason);
        //
        // List<ChatResponse> chatResponses = result.intermediateResponses();
        // System.out.println("chatResponses = " + chatResponses);


        // boolean 类型返回值
        // SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);
        // boolean positive = sentimentAnalyzer.isPositive("It's wonderful!");
        // true
        // System.out.println("positive = " + positive);

        // 枚举类型返回值
        // enum Priority {
        //     CRITICAL, HIGH, LOW
        // }
        //
        // interface PriorityAnalyzer {
        //     @UserMessage("Analyze the priority of the following issue: {{it}}")
        //     Priority analyzePriority(String issueDescription);
        // }
        // PriorityAnalyzer priorityAnalyzer = AiServices.create(PriorityAnalyzer.class, model);
        // Priority priority = priorityAnalyzer.analyzePriority("The main payment gateway is down, and customers cannot process transactions.");
        // // CRITICAL
        // System.out.println("priority = " + priority);


        // POJO 作为返回类型
        interface PersonExtractor {
            @UserMessage("Extract information about a person from {{it}}")
            Person extractPersonFrom(String text);
        }
        PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, model);
        String text = """
                In 1968, amidst the fading echoes of Independence Day,
                a child named John arrived under the calm evening sky.
                This newborn, bearing the surname Doe, marked the start of a new journey.
                He was welcomed into the world at 345 Whispering Pines Avenue
                a quaint street nestled in the heart of Springfield
                an abode that echoed with the gentle hum of suburban dreams and aspirations.
                """;
        Person person = personExtractor.extractPersonFrom(text);
        System.out.println(person); // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04, address = Address { ... } }

    }


    @Data
    @Description("an address") // you can add an optional description to help an LLM have a better understanding
    static class Address {
        String street;
        Integer streetNumber;
        String city;
    }

    // 使用 pojo 返回值接口
    @Data
    static class Person {
        @Description("first name of a person") // you can add an optional description to help an LLM have a better understanding
        String firstName;
        String lastName;
        LocalDate birthDate;
        Address address;
    }


    // 定义一个 boolean 类型返回值接口
    interface SentimentAnalyzer {
        @UserMessage("Does {{it}} has a positive sentiment?")
        boolean isPositive(String text);

    }


    // 定义有返回值的接口
    interface Assistant {

        @UserMessage("为以下主题的文章生成一个大纲: {{it}}")
        Result<List<String>> generateOutlineFor(String topic);
    }

    // 定义一个有 chatrequestParameters 的接口 (这就是 AI Service)
    interface AssistantWithChatParams {
        String chat(@UserMessage String message, ChatRequestParameters chatRequestParameters);
    }


    // 定义一个接口 (这就是 AI Service)
    interface Assistant1 {
        @SystemMessage("你是我很好的朋友。用俚语回答。")
        String chat(String message);
    }

    // 定义一个接口 (这就是 AI Service)
    interface Friend1 {
        @UserMessage("你是我很好的朋友。用俚语回答。{{message}}")
        String chat(@V("message") String message);
    }

    // 定义一个接口 (这就是 AI Service)
    interface Friend {
        @UserMessage("你是我很好的朋友。用俚语回答。{{it}}")
        String chat(String message);
    }

}
