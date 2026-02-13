package com.langchain4jpractice.openIAExamples.practiceRAG;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagAssistantService assistant;

    // Inyectamos la interfaz que definimos en el Config
    public RagController(RagAssistantService assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ask")
    public String preguntar(@RequestParam String query) {
        return assistant.chatear(query);
    }
}