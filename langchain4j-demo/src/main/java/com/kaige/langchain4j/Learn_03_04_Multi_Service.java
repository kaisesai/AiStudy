package com.kaige.langchain4j;

import com.kaige.langchain4j.service.ChatBot;
import com.kaige.langchain4j.service.GreetingExpert;
import lombok.AllArgsConstructor;

public class Learn_03_04_Multi_Service {

    public static void main(String[] args) {

    }

    @AllArgsConstructor
    static class MilesOfSmiles {

        private final GreetingExpert greetingExpert;
        private final ChatBot chatBot;

        public String handle(String userMessage) {
            if (greetingExpert.isGreeting(userMessage)) {
                return "Greetings from Miles of Smiles! How can I make your day better?";
            } else {
                return chatBot.reply(userMessage);
            }
        }
    }
}
