package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.observability.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Agent 智能体
 */
@Slf4j
public class learn_04_Agent {

    public static void main(String[] args) {

        /*
        从本质上讲，代理是普通的 AI 服务，提供相同的功能，但能够与其他代理结合，创建更复杂的工作流程和代理系统。

        与人工智能服务的另一个主要区别在于，它包含一个outputKey参数，用于指定共享变量的名称，该变量用于存储代理调用的结果，
        以便同一智能体系统中的其他代理可以访问该结果。此外，输出名称也可以直接在@Agent注解中声明，而不是像本例中那样通过编程方式声明，
        这样就可以在代码中省略该名称，而将其添加到此处。
         */

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

        // 创建一个作家 agent
        // CreativeWriter creativeWriter = AgenticServices.agentBuilder(CreativeWriter.class)
        //         .chatModel(chatModel)
        //         .outputKey("story")
        //         .build();
        // String story = creativeWriter.generateStory("天网与终结者");
        // log.info("story: {}", story);

        AgentMonitor monitor = new AgentMonitor();

        // 1. 工作流模式
        // 1.1 顺序工作流
        //
        CreativeWriter creativeWriter = AgenticServices.agentBuilder(CreativeWriter.class)
                .listener(new AgentListener() {
                    @Override
                    public void beforeAgentInvocation(AgentRequest request) {
                        System.out.println("Invoking CreativeWriter with topic: "
                                + request.inputs().get("topic"));
                    }
                })
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        AudienceEditor audienceEditor = AgenticServices.agentBuilder(AudienceEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices.agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        // 顺序工作流
        UntypedAgent novelCreator = AgenticServices.sequenceBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        Map<String, Object> input = Map.of("topic", "龙与巫师", "style", "幻想", "audience", "年轻人");

        String story = (String) novelCreator.invoke(input);
        log.info("generated story: {}", story);

        /*
        generated story: 雪巅之上，朔风如古刃刮骨。老巫师枯指结印，吟出《岁寒契》的终章。咒音落处，万年玄冰自内里绽开蛛网般的裂痕，一声低沉的龙吟自渊底苏醒，震落漫天霜华。
        他跋涉三世，只为借一缕赤龙吐息，熔断这封冻岁月的寒渊。可当巨兽昂起覆满古符的鳞首，熔金般的竖瞳缓缓睁开时，喷薄而出的并非焚天烈焰，而是簌簌坠落的碎星。
        星辉触地，冻土如琉璃般剥落，幽蓝的灵泉自裂隙涌出，枯木绽出荧荧的春蕊。风褪去凛冽，化作远古歌谣般的低语，拂过新生的苔痕与微光。
        原来世间本无禁忌之术。那不过是两道被时光放逐的孤影，在纪元更迭的尽头，循着同一缕残息，悄然重逢。
         */

        // 1.2 循环工作流
        // 通过反复调用能够编辑或改进的文本的代理，来迭代地完善文本。
        StyleScorer styleScorer = AgenticServices.agentBuilder(StyleScorer.class)
                .chatModel(chatModel)
                .outputKey("score")
                .build();

        // 故事循环检查
        UntypedAgent styleReviewLoop = AgenticServices.loopBuilder()
                .subAgents(styleScorer, styleEditor)
                .maxIterations(5)
                .exitCondition((agenticScope, loopCounter) -> {
                    double score = agenticScope.readState("score", 0.0);
                    return loopCounter <= 3 ? score >= 0.8 : score >= 0.6;
                })
                .build();

        // 循环工作流, 故事创作和风格审核结合在一起
        StyledWriter styledWriter = AgenticServices.sequenceBuilder(StyledWriter.class)
                .subAgents(creativeWriter, styleReviewLoop)
                .listener(monitor)
                .listener(new AgentListener() {
                    @Override
                    public void afterAgentInvocation(AgentResponse response) {
                        if ("styleScorer".equals(response.agentName())) {
                            System.out.println("Current score: " + response.output());
                        }
                    }
                })
                .outputKey("story")
                .build();

        String story1 = styledWriter.writeStoryWithStyle("龙与巫师", "幻想");
        log.info("使用循环+顺序 agent 生成的故事: {}", story1);

        MonitoredExecution execution = monitor.successfulExecutions().get(0);
        System.out.println(execution);

        HtmlReportGenerator.generateReport(monitor, Path.of("review-loop.html"));
    }

    public interface StyledWriter {

        @Agent
        String writeStoryWithStyle(@V("topic") String topic, @V("style") String style);
    }

    /**
     * 风格打分者
     */
    public interface StyleScorer {

        @UserMessage("""
            您是一名专业的评论者。
            请根据故事与“{{style}}”的契合程度，为其给出一个介于 0.0 到 1.0 之间的评分。
            仅返回评分，不要包含其他内容。
            故事内容如下：“{{story}}”
            """)
        @Agent("根据一个故事与特定风格的契合程度来给其打分")
        double scoreStyle(@V("story") String story, @V("style") String style);
    }

    /**
     * 风格编辑
     */
    public interface StyleEditor {

        @UserMessage("""
            你是一名专业的编辑。
            请对以下故事进行分析并重新编写，使其更符合并更具连贯性地符合 {{style}} 风格。
            只返回故事内容，不包含其他任何内容。
            故事内容为：“{{story}}”。
            """)
        @Agent("对故事进行修改，使其更符合特定的风格。")
        String editStory(@V("story") String story, @V("style") String style);
    }

    /**
     * 听众编辑
     */
    public interface AudienceEditor {

        @UserMessage("""
            你是一名专业的编辑。
            请对以下故事进行分析并重新编写，使其更符合 {{audience}} 的目标读者群体。
            仅返回故事内容，不要其他任何内容。
            故事是 "{{story}}".
            """)
        @Agent("对故事进行修改，使其更符合特定的受众群体。")
        String editStory(@V("story") String story, @V("audience") String audience);
    }

    /**
     * 创作力作家
     */
    public interface CreativeWriter {

        @UserMessage("""
            你是一位富有创造力的作家。围绕给定的主题生成一段不超过 3 句话的故事草稿。只返回故事内容，不要其他任何信息。主题为 "{{topic}}"。
            """)
        @Agent(outputKey = "story", description = "根据给定的主题生成一个故事")
        String generateStory(@V("topic") String topic);
    }
}
