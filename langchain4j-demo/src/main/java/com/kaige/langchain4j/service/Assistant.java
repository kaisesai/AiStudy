package com.kaige.langchain4j.service;

import dev.langchain4j.service.UserMessage;

/**
 * 这是一个AI 服务接口
 */
public interface Assistant {

    String chat(@UserMessage String message);

}
