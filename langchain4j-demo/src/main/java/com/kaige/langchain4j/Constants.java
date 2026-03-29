package com.kaige.langchain4j;

public class Constants {

    // API密钥（需要替换为您的实际API Key）
    public static String API_KEY = System.getenv("HUO_SHAN_API_KEY");

    // API地址
    public static String API_BASE_URL = System.getenv("HUO_SHAN_OPEN_AI_BASE_URL");

    // API模型
    public static String API_MODEL = System.getenv("HUO_SHAN_MODEL");

    // embedding API地址
    public static String EMBEDDING_API_BASE_URL = System.getenv("HUO_SHAN_EMBEDDING_BASE_URL");

    // embedding API模型
    public static String EMBEDDING_API_MODEL = System.getenv("HUO_SHAN_EMBEDDING_MODEL");

}
