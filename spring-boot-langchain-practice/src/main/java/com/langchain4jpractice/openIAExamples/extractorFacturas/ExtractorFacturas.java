package com.langchain4jpractice.openIAExamples.extractorFacturas;

import dev.langchain4j.service.UserMessage;

public interface ExtractorFacturas {

    @UserMessage("Extrae la informacion de la factura ")
    Factura analizarTexto(String textoSucio);

}
