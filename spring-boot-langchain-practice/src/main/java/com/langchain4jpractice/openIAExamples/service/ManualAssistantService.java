package com.langchain4jpractice.openIAExamples.service;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ManualAssistantService {


    private final ChatModel chatModel;

    public ManualAssistantService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String getExplainFunny(String topic) {

        log.info("-----> Preparando explicacion sobre: {}", topic);
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

        // --- EXTRACCIÓN DE TOKENS ---
        TokenUsage usage = response.tokenUsage();
        if (usage != null) {
            log.info("REPORTE DE CONSUMO:");
            log.info(" Entrada (Prompt): {} tokens", usage.inputTokenCount());
            log.info(" Salida (Respuesta): {} tokens", usage.outputTokenCount());
            log.info(" Total: {} tokens", usage.totalTokenCount());

            // Opcional: Calcular precio estimado (Precios feb 2026 aprox para gpt-4o-mini)
            // Input: $0.15 / 1M tokens | Output: $0.60 / 1M tokens
            double costoEstimado = (usage.inputTokenCount() * 0.00000015) + (usage.outputTokenCount() * 0.00000060);
            log.info("   💵 Costo aprox de esta llamada: ${}", String.format("%.8f", costoEstimado));
        }

        return response.aiMessage().text();
    }
}
