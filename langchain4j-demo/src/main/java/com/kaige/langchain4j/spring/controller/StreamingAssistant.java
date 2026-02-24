package com.kaige.langchain4j.spring.controller;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, streamingChatModel = "openAiStreamingChatModel")
public interface StreamingAssistant {

    @SystemMessage("你是一位有礼貌的助手。")
    Flux<String> chat(String userMessage);
}
