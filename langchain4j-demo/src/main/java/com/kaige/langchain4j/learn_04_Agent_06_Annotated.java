package com.kaige.langchain4j;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.declarative.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Agent 条件化的智能体
 */
@Slf4j
public class learn_04_Agent_06_Annotated {

    public static void main(String[] args) {
        ChatModel chatModel = loadBaseModel();

        EveningPlannerAgent eveningPlannerAgent =
                AgenticServices.createAgenticSystem(EveningPlannerAgent.class, chatModel);
        // List<learn_04_Agent_02.EveningPlan> plans = eveningPlannerAgent.plan("浪漫");

        // log.info("晚餐计划 plans: {}", plans);

        // 声明式智能体
        learn_04_Agent_04.ExpertRouterAgent expertRouterAgent = AgenticServices.sequenceBuilder(
                        learn_04_Agent_04.ExpertRouterAgent.class)
                .subAgents(learn_04_Agent_04.CategoryRouter.class, ExpertsAgent.class)
                .outputKey("response")
                .build();

        String askExpert = expertRouterAgent.ask("我的摩托车刹车有问题，该怎么办？");
        log.info("专家回答: {}", askExpert);
    }

    private static ChatModel loadBaseModel() {
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

        return chatModel;
    }

    // 注解声明式智能体
    public interface EveningPlannerAgent {

        @ParallelExecutor
        static Executor executor() {
            return Executors.newFixedThreadPool(2);
        }

        @Output
        static List<learn_04_Agent_02.EveningPlan> output(
                @V("movies") List<String> movies, @V("meals") List<String> meals) {
            List<learn_04_Agent_02.EveningPlan> moviesAndMeals = new ArrayList<>();
            for (int i = 0; i < movies.size(); i++) {
                if (i >= meals.size()) {
                    break;
                }
                moviesAndMeals.add(new learn_04_Agent_02.EveningPlan(movies.get(i), meals.get(i)));
            }
            return moviesAndMeals;
        }

        @ParallelAgent(
                outputKey = "plans",
                subAgents = {learn_04_Agent_02.FoodExpert.class, learn_04_Agent_02.MovieExpert.class})
        List<learn_04_Agent_02.EveningPlan> plan(@V("mood") String mood);
    }

    /**
     * 注解式专家智能体
     */
    public interface ExpertsAgent {

        @ActivationCondition(learn_04_Agent_04.MedicalExpert.class)
        static boolean activateMedical(@V("category") learn_04_Agent_04.RequestCategory category) {
            return category == learn_04_Agent_04.RequestCategory.MEDICAL;
        }

        @ActivationCondition(learn_04_Agent_04.TechnicalExpert.class)
        static boolean activateTechnical(@V("category") learn_04_Agent_04.RequestCategory category) {
            return category == learn_04_Agent_04.RequestCategory.TECHNICAL;
        }

        @ActivationCondition(learn_04_Agent_04.LegalExpert.class)
        static boolean activateLegal(@V("category") learn_04_Agent_04.RequestCategory category) {
            return category == learn_04_Agent_04.RequestCategory.LEGAL;
        }

        @ChatModelSupplier
        static ChatModel chatModel() {
            return loadBaseModel();
        }

        @ConditionalAgent(
                outputKey = "response",
                subAgents = {
                    learn_04_Agent_04.MedicalExpert.class,
                    learn_04_Agent_04.TechnicalExpert.class,
                    learn_04_Agent_04.LegalExpert.class
                })
        String askExpert(@V("request") String request);
    }
}
