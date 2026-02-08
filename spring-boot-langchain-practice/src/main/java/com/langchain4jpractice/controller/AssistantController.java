package com.langchain4jpractice.controller;

import com.langchain4jpractice.service.Assistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AssistantController {

    /*
    Spring inyecta aquí no una clase tuya, sino el Proxy Dinámico que LangChain4j generó en el paso 1.
    git El controlador es solo un recepcionista; no sabe de IAs, solo sabe pasar mensajes.
    * */
    @Autowired
    Assistant assistant;

    @GetMapping("/chat")
    public String chat(String message) {
        return assistant.chat(message);
    }
}