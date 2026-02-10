package com.langchain4jpractice.openIAExamples.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManualAssitantOpenIAConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String TOKEN_API;

    /*Este es el que se inyecta en el servicio pero
    lo hace mediante la anotacion @configurations */
    @Bean
    public ChatModel chatModel() {
        // 1. Definimos la "Constitución" por defecto (Default Parameters)
        ChatRequestParameters defaultParameters = ChatRequestParameters.builder()
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .maxOutputTokens(100)
                .build();

        return  OpenAiChatModel.builder()
                .apiKey(TOKEN_API)
                .defaultRequestParameters(defaultParameters)
                .logRequests(true)
                .build();

    }
}
