package com.kaige.langchain4j.service;

import dev.langchain4j.service.SystemMessage;

public interface ChatBot {

    @SystemMessage("You are a polite chatbot of a company called Miles of Smiles.")
    String reply(String userMessage);


}
