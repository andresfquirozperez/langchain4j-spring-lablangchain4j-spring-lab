package com.langchain4jpractice.config;

import com.langchain4jpractice.service.Assistant;
import com.langchain4jpractice.tools.CalculadoraTools;
import com.langchain4jpractice.tools.TrelloTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public Assistant assistant(@Value("${langchain4j.google-ai-gemini.chat-model.api-key}") String apiKey,
                               CalculadoraTools calculadoraTools,
                               TrelloTools trelloTools) {
        ChatModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3-pro-preview")
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .build();

        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(20);
        memory.add(dev.langchain4j.data.message.SystemMessage.from(
                "Eres un asistente experto en Java Senior que responde con metáforas de cocina. " +
                        "Sé breve y divertido."
        ));
        // 3. LA MAGIA: AiServices une todo
        return AiServices.builder(Assistant.class)
                .chatModel(gemini)
                .chatMemory(memory)
                .tools(calculadoraTools, trelloTools)
                .build();
    }
}


/*
* * RUTA DE EJECUCIÓN
1-Spring arranca y lee tu clase AiConfig.

2-Ejecuta el método @Bean assistant(...).

3-Tú eliges el Cerebro: Creas manualmente gemini-2.5-flash con tus configuraciones exactas (temperatura 0.7).

4-Tú eliges la Memoria: Creas MessageWindowChatMemory y le insertas manualmente la personalidad ("experto en cocina").

5-El Ensamblaje (AiServices.builder): Aquí tomas el plano (Assistant.class), le atornillas el cerebro (gemini) y le conectas la memoria (memory).

6-Resultado: LangChain4j crea el Proxy Dinámico (el robot real) y Spring lo guarda en su contenedor.

7-Uso: Cuando el Controller pide un Assistant, Spring le entrega este robot específico que tú armaste a mano.



* */

