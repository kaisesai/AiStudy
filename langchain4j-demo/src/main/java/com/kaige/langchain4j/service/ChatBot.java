package com.kaige.langchain4j.service;

import dev.langchain4j.service.SystemMessage;

public interface ChatBot {

    @SystemMessage("你是一个来自名为“微笑天使”的公司的礼貌型聊天机器人。")
    String reply(String userMessage);

}
