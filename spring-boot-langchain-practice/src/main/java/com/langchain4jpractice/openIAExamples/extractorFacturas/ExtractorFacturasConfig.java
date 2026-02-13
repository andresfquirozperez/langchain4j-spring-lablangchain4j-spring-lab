package com.langchain4jpractice.openIAExamples.extractorFacturas;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExtractorFacturasConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String TOKEN_API;

    @Bean
    public ChatModel chatModel() {

        ChatRequestParameters defaultParameters = ChatRequestParameters.builder()
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .maxOutputTokens(100)
                .build();

        return OpenAiChatModel.builder()
                .apiKey(TOKEN_API)
                .defaultRequestParameters(defaultParameters)
                .maxTokens(500)
                .temperature(1.2)
                .logRequests(true)
                .build();
    }
}

