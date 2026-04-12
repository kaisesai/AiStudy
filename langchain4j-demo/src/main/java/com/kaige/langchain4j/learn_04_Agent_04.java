package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

/**
 * Agent 条件化的智能体
 */
@Slf4j
public class learn_04_Agent_04 {

    public static void main(String[] args) {

        ChatModel chatModel = laodChatModel();

        CategoryRouter routerAgent = AgenticServices.agentBuilder(CategoryRouter.class)
                .chatModel(chatModel)
                .outputKey("category")
                .build();

        MedicalExpert medicalExpert = AgenticServices.agentBuilder(MedicalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
                .build();
        LegalExpert legalExpert = AgenticServices.agentBuilder(LegalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
                .build();
        TechnicalExpert technicalExpert = AgenticServices.agentBuilder(TechnicalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
                .build();

        UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
                .subAgents(
                        agenticScope ->
                                agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.MEDICAL,
                        medicalExpert)
                .subAgents(
                        agenticScope ->
                                agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL,
                        legalExpert)
                .subAgents(
                        agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN)
                                == RequestCategory.TECHNICAL,
                        technicalExpert)
                .build();

        ExpertRouterAgent expertRouterAgent = AgenticServices.sequenceBuilder(ExpertRouterAgent.class)
                .subAgents(routerAgent, expertsAgent)
                .outputKey("response")
                .build();

        String response = expertRouterAgent.ask("我的腿骨折了，我该怎么办？");

        log.info("response: {}", response);

        System.exit(-1);
    }

    private static ChatModel laodChatModel() {
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

    public enum RequestCategory {
        LEGAL,
        MEDICAL,
        TECHNICAL,
        UNKNOWN
    }

    /**
     * 分类路由
     */
    public interface CategoryRouter {

        @ChatModelSupplier
        static ChatModel chatModel() {
            return laodChatModel();
        }

        @UserMessage("""
            分析以下用户请求，并将其归类为“LEGAL”、“MEDICAL”或“TECHNICAL”类别。
            如果该请求不属于上述任何类别，则将其归类为“UNKNOWN”。
            仅回复其中一个词，不要添加其他内容。
            用户请求为：“{{request}}”
            """)
        @Agent(value = "Categorizes a user request", outputKey = "category")
        RequestCategory classify(@V("request") String request);
    }

    /**
     * 医疗专家
     */
    public interface MedicalExpert {

        @UserMessage("""
            您是医学专家。
            请从医学角度对以下用户请求进行分析，并给出最合适的回答。
            用户请求为 {{request}} 。
            """)
        @Agent(value = "A medical expert", outputKey = "response")
        String medical(@V("request") String request);
    }

    /**
     * 法律专家
     */
    public interface LegalExpert {

        @UserMessage("""
            您是法律专家。
            请从法律角度对该用户请求进行分析，并给出最合适的答复。
            用户请求为 {{request}} 。
            """)
        @Agent(value = "A legal expert", outputKey = "response")
        String legal(@V("request") String request);
    }

    /**
     * 教育专家
     */
    public interface TechnicalExpert {

        @UserMessage("""
            您是一位技术专家。
            从技术角度对该用户请求进行分析，并给出最合适的回答。
            用户请求为 {{request}} 。
            """)
        @Agent(value = "A technical expert", outputKey = "response")
        String technical(@V("request") String request);
    }

    /**
     * 专业路由
     */
    public interface ExpertRouterAgent {

        @Agent(value = "Routes the request to the appropriate expert", outputKey = "response")
        String ask(@V("request") String request);
    }
}
