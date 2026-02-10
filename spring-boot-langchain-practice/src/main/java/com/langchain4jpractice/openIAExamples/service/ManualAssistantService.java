package com.langchain4jpractice.openIAExamples.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ManualAssistantService {


    private final ChatModel chatModel;

    public ManualAssistantService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String getExplainFunny(String topic) {
        // 1. Definimos el Override (Parámetros específicos para ESTA llamada)
        // Queremos que las historias sean con el modelo barato (mini) y muy creativas.
        ChatRequestParameters overrideParams = ChatRequestParameters.builder()
                .modelName("gpt-4o-mini") // Sobrescribimos el modelo global
                .temperature(1.0)         // Sobrescribimos la temperatura
                .maxOutputTokens(50)
                .build();

        // 2. Construimos el Request (El "Sobre")
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("Tell me a funny explain about " + topic))
                .parameters(overrideParams) // ¡Aquí inyectamos la excepción!
                .build();

        // 3. Ejecución (El modelo hace el Merge automático: Default + Override)
        ChatResponse response = chatModel.chat(request);

        return response.aiMessage().text();
    }
}
