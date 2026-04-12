package com.kaige.langchain4j;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Agent 条件化的智能体
 */
@Slf4j
public class learn_04_Agent_05_tokenStream {

    public static void main(String[] args) {

        StreamingCreativeWriter story = AgenticServices.agentBuilder(StreamingCreativeWriter.class)
                .streamingChatModel(streamingBaseModel())
                .outputKey("story")
                .build();

        TokenStream tokenStream = story.generateStory("地下城与勇士");
        log.info("response: {}", tokenStream);

        // StreamingCreativeWriter creativeWriter = AgenticServices.agentBuilder(StreamingCreativeWriter.class)
        //         .streamingChatModel(streamingBaseModel())
        //         .outputKey("story")
        //         .build();
        //
        // StreamingAudienceEditor audienceEditor = AgenticServices.agentBuilder(StreamingAudienceEditor.class)
        //         .streamingChatModel(streamingBaseModel())
        //         .outputKey("story")
        //         .build();
        //
        // StreamingStyleEditor styleEditor = AgenticServices.agentBuilder(StreamingStyleEditor.class)
        //         .streamingChatModel(streamingBaseModel())
        //         .outputKey("story")
        //         .build();
        //
        // StreamingReviewedWriter novelCreator = AgenticServices.sequenceBuilder(StreamingReviewedWriter.class)
        //         .subAgents(creativeWriter, audienceEditor, styleEditor)
        //         .outputKey("story")
        //         .build();

        // tokenStream = novelCreator.writeStory("dragons and wizards", "young adults", "fantasy");

        // log.info("response: {}", tokenStream);

        System.exit(-1);
    }

    private static StreamingChatModel streamingBaseModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(Constants.MINI_MAX_API_KEY)
                .baseUrl(Constants.MINI_MAX_API_BASE_URL)
                .modelName(Constants.MINI_MAX_API_MODEL)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    public interface StreamingReviewedWriter {
        @Agent
        TokenStream writeStory(@V("topic") String topic, @V("audience") String audience, @V("style") String style);
    }

    public interface StreamingCreativeWriter {

        @UserMessage("""
            你是一位富有创造力的作家。
            围绕给定的主题生成一段不超过 3 句话的故事草稿。
            只返回故事内容，不要其他任何信息。
            主题为 {{topic}} 。
            """)
        @Agent("根据给定的主题生成一个故事")
        TokenStream generateStory(@V("topic") String topic);
    }

    private static class StreamingAudienceEditor {}

    private static class StreamingStyleEditor {}
}
