package com.kaige.langchain4j;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class Learn_03_Ai_Service {

    public static void main(String[] args) {

        // 1. 配置模型 (连接你本地的 Ollama)
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Ollama 默认地址
                .modelName("qwen:4b") // 你刚才下载的模型
                .timeout(Duration.of(100, ChronoUnit.SECONDS))
                .build();

        // 2. 定义一个接口 (这就是 AI Service)
        interface Assistant {
            String chat(String message);
        }

        // 3. 让框架自动创建这个接口的实现类
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();


        // 4. 开始对话
        for (int i = 0; i < 100; i++) {
            System.out.println("第 " + i + " 次调用大模型");
            String answer = assistant.chat("你好，我是刚掌握 Ollama 的开发者，帮我把我发出来的数相加， num: " + i);
            System.out.println("大模型回复: " + answer);
        }

    }
}
