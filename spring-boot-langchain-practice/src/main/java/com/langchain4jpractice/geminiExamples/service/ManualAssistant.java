package com.langchain4jpractice.geminiExamples.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ManualAssistant {
    private final ChatModel gemini;
    private final ChatMemory memory;


    public ManualAssistant(@Value("${langchain4j.google-ai-gemini.chat-model.api-key}") String apiKey) {

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("¡ERROR! No encontré la variable de entorno GEMINI_API_KEY");
        }

        this.gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .logRequestsAndResponses(true)
                .build();
        this.memory = MessageWindowChatMemory.withMaxMessages(20);
        this.memory.add(dev.langchain4j.data.message.SystemMessage.from(
                "Eres un asistente experto en Java Senior que responde con metáforas de cocina. " +
                        "Sé breve y divertido."
        ));
    }

    public String chat(String userText) {
        this.memory.add(UserMessage.from(userText));

        ChatRequest request = ChatRequest.builder()
                .messages(this.memory.messages())
                .build();

        ChatResponse chatResponse = this.gemini.chat(request);

        AiMessage aiMessage = chatResponse.aiMessage();
        this.memory.add(aiMessage);

        return aiMessage.text();
    }
}
