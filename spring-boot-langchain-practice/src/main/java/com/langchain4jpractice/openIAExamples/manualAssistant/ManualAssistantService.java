package com.langchain4jpractice.openIAExamples.manualAssistant;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de asistente IA con control manual del flujo LangChain4j.
 *
 * IMPLEMENTACION:
 * - Usa el patron de inyeccion por constructor (buena practica Spring)
 * - Demuestra uso MANUAL de LangChain4j vs el enfoque declarativo @AiService
 * - Implementa override de parametros: Configuracion global + Override por metodo
 * - Incluye telemetria completa: tracking de tokens y calculo de costos OpenAI
 *
 * FLUJO:
 * 1. Recibe topic del usuario
 * 2. Construye ChatRequestParameters (override de temperatura, modelo, tokens)
 * 3. Crea ChatRequest con UserMessage
 * 4. Ejecuta chatModel.chat() y obtiene ChatResponse
 * 5. Extrae TokenUsage y calcula costo estimado en USD
 * 6. Retorna texto de la respuesta AI
 *
 * NOTA: Este enfoque manual da control total pero requiere mas codigo
 * que usar @AiService con interfaces declarativas.
 */
@Service
@Slf4j
public class ManualAssistantService {


    private final ChatModel chatModel;

    public ManualAssistantService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String getExplainFunny(String topic) {
        // Log de inicio para debugging
        log.info("-----> Preparando explicacion sobre: {}", topic);

        // ============================================
        // PASO 1: OVERRIDE DE PARAMETROS
        // ============================================
        // Definimos parametros ESPECIFICOS para esta llamada.
        // Estos sobrescribiran los parametros por defecto de la configuracion.
        // Temperature 1.0 = maxima creatividad (ideal para humor)
        // maxOutputTokens 50 = respuestas cortas y concisas
        ChatRequestParameters overrideParams = ChatRequestParameters.builder()
                .modelName("gpt-4o-mini")  // Modelo economico de OpenAI
                .temperature(1.0)          // Alta creatividad para explicaciones divertidas
                .maxOutputTokens(50)       // Limitamos longitud para respuestas punchy
                .build();

        // ============================================
        // PASO 2: CONSTRUCCION DEL REQUEST
        // ============================================
        // Creamos el mensaje del usuario con el topic.
        // LangChain4j automaticamente hara MERGE entre:
        // - Parametros DEFAULT (de ManualAssitantOpenIAConfig)
        // - Parametros OVERRIDE (los de arriba)
        // El resultado sera: temp=1.0, maxTokens=50
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("Tell me a funny explain about " + topic))
                .parameters(overrideParams)  // Inyectamos override aqui
                .build();

        // ============================================
        // PASO 3: EJECUCION DE LA LLAMADA IA
        // ============================================
        // chatModel.chat() internamente:
        // 1. Fusiona default + override
        // 2. Envia request a OpenAI API
        // 3. Retorna ChatResponse con texto + metadata
        ChatResponse response = chatModel.chat(request);

        // ============================================
        // PASO 4: TELEMETRIA Y CALCULO DE COSTOS
        // ============================================
        // Extraemos TokenUsage para tracking de consumo.
        // Esto es CRITICO para aplicaciones en produccion
        // donde los costos de API pueden escalar rapido.
        TokenUsage usage = response.tokenUsage();
        if (usage != null) {
            // Log detallado del consumo
            log.info("REPORTE DE CONSUMO:");
            log.info(" Entrada (Prompt): {} tokens", usage.inputTokenCount());
            log.info(" Salida (Respuesta): {} tokens", usage.outputTokenCount());
            log.info(" Total: {} tokens", usage.totalTokenCount());

            // Calculo de costo basado en tarifas OpenAI (feb 2026):
            // GPT-4o-mini: $0.15/1M input tokens + $0.60/1M output tokens
            // Convertimos a costo por token individual
            double costoInput = usage.inputTokenCount() * 0.00000015;   // $0.15 / 1,000,000
            double costoOutput = usage.outputTokenCount() * 0.00000060; // $0.60 / 1,000,000
            double costoEstimado = costoInput + costoOutput;

            log.info("   💵 Costo aprox de esta llamada: ${}", String.format("%.8f", costoEstimado));
        }

        // ============================================
        // PASO 5: RETORNO DE LA RESPUESTA
        // ============================================
        // Extraemos el texto de la respuesta AI.
        // response.aiMessage() contiene el mensaje generado por el modelo.
        return response.aiMessage().text();
    }
}
