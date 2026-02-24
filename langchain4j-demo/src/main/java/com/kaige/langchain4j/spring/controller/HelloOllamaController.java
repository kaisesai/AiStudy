package com.kaige.langchain4j.spring.controller;

import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/hello")
public class HelloOllamaController {

    private OllamaChatModel ollamaChatModel;

    public HelloOllamaController(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @GetMapping("/chat")
    public String model(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return ollamaChatModel.chat(message);
    }



}
