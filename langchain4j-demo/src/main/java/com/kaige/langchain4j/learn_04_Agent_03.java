package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Agent 智能体
 */
@Slf4j
public class learn_04_Agent_03 {

    public static void main(String[] args) {

        String apiKey = Constants.MINI_MAX_API_KEY;
        String model = Constants.MINI_MAX_API_MODEL;
        String baseUrl = Constants.MINI_MAX_API_BASE_URL;

        // 方案 1: 使用 OpenAI 兼容接口 (推荐 ✅)
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl) // MiniMax OpenAI 兼容接口
                .modelName(model)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                // 注意: customParameters 会直接将参数添加到请求体顶层
                // reasoning_split: false - 思考内容包含在 content 中(默认行为)
                // reasoning_split: true - 思考内容分离到 reasoning_details 字段
                .customParameters(Map.of("reasoning_split", true))
                // .returnThinking(false)
                .build();

        // 占星师
        PersonAstrologyAgent personAstrologyAgent = AgenticServices.agentBuilder(PersonAstrologyAgent.class)
                .chatModel(chatModel)
                .outputKey("horoscope")
                .build();

        // 并行映射模版
        BatchHoroscopeAgent agent = AgenticServices.parallelMapperBuilder(BatchHoroscopeAgent.class)
                .subAgents(personAstrologyAgent)
                .itemsProvider("persons")
                .executor(Executors.newFixedThreadPool(3))
                .build();

        List<Person> persons =
                List.of(new Person("Mario", "白羊座"), new Person("Luigi", "双鱼座"), new Person("Peach", "狮子座"));

        List<String> horoscopes = agent.generateHoroscopes(persons);
        log.info("horoscopes: {}", horoscopes);

        System.exit(-1);
    }

    /**
     * 占星师
     */
    public interface PersonAstrologyAgent {

        @SystemMessage("""
            你是一名占星师，能够根据用户的姓名和星座来生成星座运势。
            """)
        @UserMessage("""
            为 {{person}} 生成星座运势。
            此人有姓名和星座信息。请结合这两项内容来为其生成个性化的星座运势。
            """)
        @Agent(description = "一位为某人生成星座运势的占星师", outputKey = "horoscope")
        String horoscope(@V("person") Person person);
    }

    private interface BatchHoroscopeAgent {

        @Agent
        List<String> generateHoroscopes(@V("persons") List<Person> persons);
    }

    public record Person(String name, String constellation) {}
}
