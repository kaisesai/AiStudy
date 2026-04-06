package com.kaige.langchain4j;

import com.kaige.langchain4j.service.Assistant;
import com.kaige.langchain4j.tool.SearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.time.Duration;

public class Learn_03_03_RAG {

    public static void main(String[] args) {

        ChatModel model = buildChatModel();

        // 设置内存 embedding 模型
        EmbeddingStore embeddingStore  =  new InMemoryEmbeddingStore<>();

        // embedding 模型
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(Constants.MINI_MAX_API_KEY)
                .baseUrl(Constants.EMBEDDING_API_BASE_URL)
                // .modelName(Constants.EMBEDDING_API_MODEL)
                .modelName("doubao-embedding-vision-251215")
                .dimensions(1536)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        ContentRetriever contentRetriever = new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                // 将 RAG 作为一种工具
                .tools(new SearchTool(contentRetriever))
                .build();

        String chat = assistant.chat("帮我搜索下 LangChain4j 的配置");
        System.out.println("chat = " + chat);




    }

    private static ChatModel buildChatModel() {
        ChatModel chatModel = OpenAiChatModel
                .builder()
                .apiKey(Constants.MINI_MAX_API_KEY)
                .baseUrl(Constants.MINI_MAX_API_BASE_URL)
                .modelName(Constants.MINI_MAX_API_MODEL)
                .temperature(0.7).
                timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
        return chatModel;
    }

}
