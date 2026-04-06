package com.kaige.langchain4j;

public class Constants {

    // API密钥（需要替换为您的实际API Key）
    public static String MINI_MAX_API_KEY = System.getenv("MINI_MAX_API_KEY");

    public static String ALI_QWEN_LLM_API_KEY = System.getenv("ALI_QWEN_LLM_API_KEY");

    // API地址 (MiniMax OpenAI 兼容接口)
    // 使用 OpenAiChatModel 时: https://api.minimaxi.com/v1
    // 使用 AnthropicChatModel 时: https://api.minimaxi.com/anthropic
    public static String MINI_MAX_API_BASE_URL = System.getenv("MINI_MAX_BASE_URL");

    public static String ALI_QWEN_LLM_BASE_URL = System.getenv("ALI_QWEN_LLM_BASE_URL");

    // API模型
    public static String MINI_MAX_API_MODEL = System.getenv("MINI_MAX_MODEL");

    public static String ALI_QWEN_LLM_API_MODEL = System.getenv("ALI_QWEN_LLM_MODEL");

    // embedding API地址
    public static String EMBEDDING_API_BASE_URL = System.getenv("HUO_SHAN_EMBEDDING_BASE_URL");

    // embedding API模型
    public static String EMBEDDING_API_MODEL = System.getenv("HUO_SHAN_EMBEDDING_MODEL");

    // web 搜索 API 密钥
    public static final String TAVILY_API_KEY = System.getenv("TAVILY_API_KEY");

}
