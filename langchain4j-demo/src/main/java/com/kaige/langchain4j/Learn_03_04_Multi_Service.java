package com.kaige.langchain4j;

import com.kaige.langchain4j.service.ChatBot;
import com.kaige.langchain4j.service.GreetingExpert;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class Learn_03_04_Multi_Service {

    static {
        // 明确指定使用的HTTP客户端，解决冲突问题
        System.setProperty("langchain4j.http.clientBuilderFactory", "dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory");

    }

    public static void main(String[] args) {

        // 低价格模型
        ChatModel lowPriceChatModel = buildChatModel(Constants.ALI_QWEN_LLM_API_KEY, Constants.ALI_QWEN_LLM_API_MODEL, Constants.ALI_QWEN_LLM_BASE_URL);
        // 礼貌服务（低价模型）
        GreetingExpert greetingExpert = AiServices.create(GreetingExpert.class, lowPriceChatModel);

        // 高价格模型
        String miniMaxApiKey = Constants.MINI_MAX_API_KEY;
        String miniMaxApiModel = Constants.MINI_MAX_API_MODEL;
        String miniMaxApiBaseUrl = Constants.MINI_MAX_API_BASE_URL;
        ChatModel highPriceChatModel = buildChatModel(miniMaxApiKey, miniMaxApiModel, miniMaxApiBaseUrl);
        // 创建 Tavily 搜索引擎
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder().apiKey(Constants.TAVILY_API_KEY)  // 需要从 https://tavily.com/ 获取 API Key
                .build();
        // web 内容检索器
        WebSearchContentRetriever contentRetriever = WebSearchContentRetriever.builder().webSearchEngine(webSearchEngine).maxResults(3).build();
        // 聊天机器人（高价模型）（拥有网络数据检索的能力）
        ChatBot chatBot = AiServices.builder(ChatBot.class).chatModel(highPriceChatModel)
                .contentRetriever(contentRetriever) // 内容检索
                .build();

        // 微笑服务
        MilesOfSmiles milesOfSmiles = new MilesOfSmiles(greetingExpert, chatBot);

        String greeting = milesOfSmiles.handle("你好");
        System.out.println("大模型返回：" + greeting); // Greetings from Miles of Smiles! How can I make your day better?

        String answer = milesOfSmiles.handle("2026年有哪些性价比高的大模型?");
        System.out.println("大模型返回：" + answer); // At Miles of Smiles, we provide a wide range of services ...

        System.exit(-1);
    }

    @AllArgsConstructor
    static class MilesOfSmiles {

        private final GreetingExpert greetingExpert;
        private final ChatBot chatBot;

        public String handle(String userMessage) {
            String result = greetingExpert.isGreeting(userMessage);
            log.info("MilesOfSmiles大模型返回：{}", result);

            if ("true".equalsIgnoreCase(result)) {
                return "来自微笑天使的服务，我如何让你变得更好呢?";
            } else {
                return chatBot.reply(userMessage);
            }
        }

        /**
         * 从模型返回的文本中解析布尔值
         * 处理可能包含的 <think>> 标签和额外文本
         */
        private boolean parseBooleanResult(String result) {
            if (result == null || result.isEmpty()) {
                return false;
            }

            // 移除 <think>> 标签及其内容
            String cleaned = result.replaceAll("<think>.*?</think>", "").trim();

            // 提取最后的 true 或 false
            cleaned = cleaned.toLowerCase().trim();

            // 查找最后一个 true 或 false
            int lastTrueIndex = cleaned.lastIndexOf("true");
            int lastFalseIndex = cleaned.lastIndexOf("false");

            if (lastTrueIndex > lastFalseIndex) {
                return true;
            } else if (lastFalseIndex > lastTrueIndex) {
                return false;
            }

            // 如果都没找到，尝试直接匹配
            return cleaned.contains("true");
        }
    }


    public static ChatModel buildChatModel(String apiKey, String model, String baseUrl) {

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
                // 通过 customParameters 传递 MiniMax 特有的参数
                // 注意: customParameters 会直接将参数添加到请求体顶层
                // reasoning_split: false - 思考内容包含在 content 中(默认行为)
                // reasoning_split: true - 思考内容分离到 reasoning_details 字段
                .customParameters(Map.of("reasoning_split", true))
                // .returnThinking(false)
                .build();

        /*
            minimax 模型扩展参数 reasoning_split为 false 时的输出：
            "message": {
                "content": "<think>\nThe user says: \"下面的文本是否礼貌？只需要回复 true 或者 false。Text: hello\". They ask: \"Is the following text polite? Just reply true or false. Text: hello\". The user is asking for a boolean answer regarding whether the text \"hello\" is polite. The question: Is the text \"hello\" polite? Typically \"hello\" is a neutral greeting; it could be considered polite. In many contexts, saying \"hello\" is a polite greeting. However, \"politeness\" can be subjective. Usually, it's considered polite, though maybe less polite than \"hello, how are you?\" But the typical standard for politeness is that \"hello\" is a neutral, polite greeting. The user wants a true/false answer. So answer: true.\n\nWe should obey the request and just output \"true\". There's no disallowed content. So output \"true\".\n</think>\n\ntrue",
            }

            minimax 模型扩展参数 reasoning_split为 true 时的输出：
            "message": {
                "content": "true",
                "reasoning_content": "The user asks: \"下面的文本是否礼貌？只需要回复 true 或者 false。Text: hello\". The user asks to check if the text \"hello\" is polite, answer only true or false.\n\nWe need to decide if \"hello\" is considered polite or not. The phrase \"hello\" is generally a polite greeting, considered a polite expression. So answer true. However we should consider if there are any special instructions: The user says \"只需要回复 true 或者 false\". So we must respond only \"true\" or \"false\". Should we include any additional text? No. Only \"true\" or \"false\". So answer \"true\".\n\nBut also check if there's any policy violation: This is a simple request, no disallowed content. The answer is safe. So we can comply.\n\nThus output: \"true\".",
                "reasoning_details": [
                    {
                        "type": "reasoning.text",
                        "id": "reasoning-text-1",
                        "format": "MiniMax-response-v1",
                        "index": 0,
                        "text": "The user asks: \"下面的文本是否礼貌？只需要回复 true 或者 false。Text: hello\". The user asks to check if the text \"hello\" is polite, answer only true or false.\n\nWe need to decide if \"hello\" is considered polite or not. The phrase \"hello\" is generally a polite greeting, considered a polite expression. So answer true. However we should consider if there are any special instructions: The user says \"只需要回复 true 或者 false\". So we must respond only \"true\" or \"false\". Should we include any additional text? No. Only \"true\" or \"false\". So answer \"true\".\n\nBut also check if there's any policy violation: This is a simple request, no disallowed content. The answer is safe. So we can comply.\n\nThus output: \"true\"."
                    }
                ]
            }
         */

        return chatModel;
    }
}
