package com.langchain4jpractice.openIAExamples.manualAssistant;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion del modelo OpenAI para el asistente manual.
 *
 * Define los parametros POR DEFECTO que serviran como base
 * para todas las llamadas al modelo. Estos parametros pueden
 * ser sobrescritos por el servicio en llamadas especificas.
 *
 * Bean expuesto: ChatModel (inyectado en ManualAssistantService)
 *
 * PARAMETROS POR DEFECTO:
 * - Modelo: gpt-4o-mini (economico)
 * - Temperature: 0.7 (balance entre creatividad y precision)
 * - Max Output Tokens: 100 (limita longitud de respuesta)
 */
@Configuration
public class ManualAssitantOpenIAConfig {

    // API Key de OpenAI - se lee desde application.properties
    // Se usa variable de entorno ${OPENAI_TOKEN} para seguridad
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String TOKEN_API;

    /**
     * Bean principal: ChatModel de OpenAI configurado con parametros por defecto.
     *
     * Este bean es inyectado automaticamente en ManualAssistantService
     * gracias a la anotacion @Autowired implicita de Spring.
     *
     * PARAMETROS POR DEFECTO:
     * - modelName: "gpt-4o-mini" (modelo economico, buen balance calidad/precio)
     * - temperature: 0.7 (balance entre creatividad y coherencia)
     * - maxOutputTokens: 100 (limita longitud de respuestas por defecto)
     *
     * NOTA: Estos parametros pueden ser sobrescritos por el servicio
     * en llamadas especificas usando ChatRequestParameters.override()
     */
    // @Bean
    public ChatModel chatModel() {
        // ============================================
        // CONFIGURACION DE PARAMETROS POR DEFECTO
        // ============================================
        // Estos valores actuan como "fallback" - se usan cuando el servicio
        // no especifica parametros explicitos en la llamada.
        ChatRequestParameters defaultParameters = ChatRequestParameters.builder()
                .modelName("gpt-4o-mini")     // Modelo recomendado para tareas simples
                .temperature(0.7)              // 0.0 = deterministico, 1.0 = muy creativo
                .maxOutputTokens(100)          // Limite de tokens en respuesta
                .build();

        // ============================================
        // CONSTRUCCION DEL MODELO
        // ============================================
        // OpenAiChatModel es la implementacion de ChatModel para OpenAI.
        // - apiKey: autenticacion con OpenAI
        // - defaultRequestParameters: parametros base
        // - logRequests: activa logging para debugging
        return OpenAiChatModel.builder()
                .apiKey(TOKEN_API)
                .defaultRequestParameters(defaultParameters)
                .logRequests(true)             // Log util para debugging en desarrollo
                .build();
    }
}
