package com.langchain4jpractice.service;

/**
 LangChain4j utiliza una técnica llamada Dynamic Proxy (Proxy Dinámico) de Java.
 Cuando tu aplicación arranca (main), LangChain4j escanea tu código buscando
 la anotación @AiService. Cuando la encuentra, crea una clase en la memoria
 RAM que implementa esa interfaz por ti.
* */
// @AiService
public interface Assistant {

    /*@SystemMessage("You are a polite assistant")
    String chat(String userMessage);*/
}
