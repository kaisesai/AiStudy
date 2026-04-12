package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Agent 智能体
 */
@Slf4j
public class learn_04_Agent_02 {

    public static void main(String[] args) {

        // String apiKey = Constants.ALI_QWEN_LLM_API_KEY;
        // String model = Constants.ALI_QWEN_LLM_API_MODEL;
        // String baseUrl = Constants.ALI_QWEN_LLM_BASE_URL;
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

        FoodExpert foodExpert = AgenticServices.agentBuilder(FoodExpert.class)
                .chatModel(chatModel)
                // .async(true)
                .outputKey("meals")
                .build();

        MovieExpert movieExpert = AgenticServices.agentBuilder(MovieExpert.class)
                .chatModel(chatModel)
                // .async(true)
                .outputKey("movies")
                .build();

        // 需求：我们可以请电影和美食专家来制定一些计划，打造一个具有特定氛围的美好夜晚，将电影和与该氛围相匹配的餐食结合起来。
        // 1. 并行模式：多个代理同时被调用，并且它们的输出被合并成一个单一的结果。

        EveningPlannerAgent plannerAgent = AgenticServices.parallelBuilder(EveningPlannerAgent.class)
                // .sequenceBuilder(EveningPlannerAgent.class)
                .subAgents(foodExpert, movieExpert)
                .executor(Executors.newFixedThreadPool(2))
                .outputKey("plans")
                .output(agenticScope -> {
                    List<String> movies = agenticScope.readState("movies", List.of());
                    List<String> meals = agenticScope.readState("meals", List.of());

                    List<EveningPlan> moviesAndMeals = new ArrayList<>();
                    for (int i = 0; i < movies.size(); i++) {
                        if (i >= meals.size()) {
                            break;
                        }
                        moviesAndMeals.add(new EveningPlan(movies.get(i), meals.get(i)));
                    }
                    return moviesAndMeals;
                })
                .build();

        String mood = "浪漫";
        List<EveningPlan> eveningPlans = plannerAgent.plan(mood);
        log.info("氛围：{}", eveningPlans);

        System.exit(-1);
    }

    /**
     * 美食专家
     */
    public interface FoodExpert {

        @UserMessage("""
                你是一位出色的晚间规划师。
                请根据给定的氛围列出三道适合的餐食。
                氛围为 {{mood}} 。
                对于每道餐食，只需给出其名称。
                请提供包含这三道菜品的清单，不要添加其他内容。
            """)
        @Agent(outputKey = "meals")
        List<String> findMeal(@V("mood") String mood);
    }

    /**
     * 电影专家
     */
    public interface MovieExpert {
        @UserMessage("""
                你是一位出色的晚间活动策划者。
                请根据给定的氛围列出三部合适的电影。
                氛围为 {{mood}}。
                请提供包含这三部影片的清单，不要添加其他内容。
            """)
        @Agent(outputKey = "movies")
        List<String> findMovie(@V("mood") String mood);
    }

    /**
     * 晚餐计划
     */
    public interface EveningPlannerAgent {

        @Agent
        List<EveningPlan> plan(@V("mood") String mood);
    }

    public record EveningPlan(String s, String s1) {}
}
