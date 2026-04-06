package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

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

        String apiKey = Constants.ALI_QWEN_LLM_API_KEY;
        String model = Constants.ALI_QWEN_LLM_API_MODEL;
        String baseUrl = Constants.ALI_QWEN_LLM_BASE_URL;

        // 方案 1: 使用 OpenAI 兼容接口 (推荐 ✅)
        ChatModel chatModel = OpenAiChatModel
                .builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)  // MiniMax OpenAI 兼容接口
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

        // 1. 工作流模式
        // 1.1 顺序工作流
        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        AudienceEditor audienceEditor = AgenticServices
                .agentBuilder(AudienceEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();

        UntypedAgent novelCreator = AgenticServices
                .sequenceBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("story")
                .build();

        Map<String, Object> input = Map.of(
                "topic", "龙与巫师",
                "style", "幻想",
                "audience", "年轻人"
        );

        String story = (String) novelCreator.invoke(input);
        log.info("generated story: {}", story);

        /*
        generated story: 雪巅之上，朔风如古刃刮骨。老巫师枯指结印，吟出《岁寒契》的终章。咒音落处，万年玄冰自内里绽开蛛网般的裂痕，一声低沉的龙吟自渊底苏醒，震落漫天霜华。
        他跋涉三世，只为借一缕赤龙吐息，熔断这封冻岁月的寒渊。可当巨兽昂起覆满古符的鳞首，熔金般的竖瞳缓缓睁开时，喷薄而出的并非焚天烈焰，而是簌簌坠落的碎星。
        星辉触地，冻土如琉璃般剥落，幽蓝的灵泉自裂隙涌出，枯木绽出荧荧的春蕊。风褪去凛冽，化作远古歌谣般的低语，拂过新生的苔痕与微光。
        原来世间本无禁忌之术。那不过是两道被时光放逐的孤影，在纪元更迭的尽头，循着同一缕残息，悄然重逢。
         */

        // 1.2 循环工作流

    }

    public interface StyleEditor {

        @UserMessage("""
                You are a professional editor.
                Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
                Return only the story and nothing else.
                The story is "{{story}}".
                """)
        @Agent("Edits a story to better fit a given style")
        String editStory(@V("story") String story, @V("style") String style);
    }

    public interface AudienceEditor {

        @UserMessage("""
                You are a professional editor.
                Analyze and rewrite the following story to better align
                with the target audience of {{audience}}.
                Return only the story and nothing else.
                The story is "{{story}}".
                """)
        @Agent("Edits a story to better fit a given audience")
        String editStory(@V("story") String story, @V("audience") String audience);
    }

    public interface CreativeWriter {

        @UserMessage("""
                You are a creative writer.
                Generate a draft of a story no more than
                3 sentences long around the given topic.
                Return only the story and nothing else.
                The topic is {{topic}}.
                """)
        @Agent(outputKey = "story", description = "Generates a story based on the given topic")
        String generateStory(@V("topic") String topic);
    }

}
