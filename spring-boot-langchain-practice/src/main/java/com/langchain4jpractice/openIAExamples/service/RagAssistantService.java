package com.langchain4jpractice.openIAExamples.service;

// Ojo: Esto es una INTERFAZ, no una clase
public interface RagAssistantService {

    // LangChain4j hará lo siguiente automáticamente:
    // 1. Tomará tu pregunta.
    // 2. Buscará en el EmbeddingStore info relevante.
    // 3. Pegará esa info en el prompt del sistema.
    // 4. Enviará todo a OpenAI.
    String chatear(String userMessage);
}