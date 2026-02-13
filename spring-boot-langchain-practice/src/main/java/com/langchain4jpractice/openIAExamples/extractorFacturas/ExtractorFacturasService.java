package com.langchain4jpractice.openIAExamples.extractorFacturas;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ExtractorFacturasService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;


    public ExtractorFacturasService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }


    public FacturaDTO analizarTexto(String textoSucio) {
        // Construir el prompt para la IA
        String prompt = String.format(
            "Analiza la informacion del texto: '%s' y extrae los datos en formato JSON con esta estructura exacta: " +
            "{\"cliente\": \"nombre del cliente\", \"fecha\": \"fecha de la factura\", \"total\": monto numerico, \"productos\": [\"producto1\", \"producto2\"]}. " +
            "Responde SOLO con el JSON, sin texto adicional.",
            textoSucio
        );

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(prompt))
                .build();

        // Llamar al modelo con el request (no con el texto directamente)
        ChatResponse response = chatModel.chat(request);

        // Extraer el texto de la respuesta
        String jsonRespuesta = response.aiMessage().text();

        // Limpiar posibles caracteres de markdown (```json ... ```)
        jsonRespuesta = jsonRespuesta.replaceAll("```json", "")
                                    .replaceAll("```", "")
                                    .trim();

        try {
            // Parsear el JSON a FacturaDTO
            return objectMapper.readValue(jsonRespuesta, FacturaDTO.class);
        } catch (Exception e) {
            // Si hay error de parsing, loguear y retornar null o lanzar excepcion
            System.err.println("Error parseando respuesta de IA: " + e.getMessage());
            System.err.println("Respuesta recibida: " + jsonRespuesta);
            return null;
        }
    }

}
