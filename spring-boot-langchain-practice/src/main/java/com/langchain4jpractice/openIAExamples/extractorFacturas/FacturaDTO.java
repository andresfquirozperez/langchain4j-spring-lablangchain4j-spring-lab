package com.langchain4jpractice.openIAExamples.extractorFacturas;

public record FacturaDTO(
        String cliente,
        String fecha,
        double total,
        String[] productos
) {}
