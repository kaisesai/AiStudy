package com.kaige.langchain4j.service;

import dev.langchain4j.service.UserMessage;

public interface GreetingExpert {

    @UserMessage("下面的文本是否是礼貌用语？只需要回复 true 或者 false。Text: {{it}}")
    String isGreeting(String text);

}
