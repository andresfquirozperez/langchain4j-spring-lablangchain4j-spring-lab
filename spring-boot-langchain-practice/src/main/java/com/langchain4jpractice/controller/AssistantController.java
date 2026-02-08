package com.langchain4jpractice.controller;

import com.langchain4jpractice.service.Assistant;
import com.langchain4jpractice.service.ManualAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AssistantController {

    /*
    Spring inyecta aquí no una clase tuya, sino el Proxy Dinámico que LangChain4j generó en el paso 1.
    git El controlador es solo un recepcionista; no sabe de IAs, solo sabe pasar mensajes.
    * */
    // @Autowired
    Assistant assistant;

    private final ManualAssistant manualAssistant;

    // Spring ve que el controlador necesita el asistente.
    // Como YA LO CREÓ en el paso anterior (con la key inyectada),
    // simplemente se lo pasa aquí.
    public AssistantController(ManualAssistant manualAssistant) {
        this.manualAssistant = manualAssistant;
    }

    @GetMapping("/chat")
    public String chat(String message) {
        String response = manualAssistant.chat(message);
        return response;
    }
}