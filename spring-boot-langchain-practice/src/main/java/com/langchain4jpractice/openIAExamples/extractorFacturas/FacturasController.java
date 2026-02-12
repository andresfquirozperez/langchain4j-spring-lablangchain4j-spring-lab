package com.langchain4jpractice.openIAExamples.extractorFacturas;

import org.springframework.http.ResponseEntity;

public class FacturasController {

    ExtractorFacturas extractorFacturas;

    public FacturasController(ExtractorFacturas extractorFacturas) {
        this.extractorFacturas = extractorFacturas;
    }

    public ResponseEntity<Factura> extractorDeFactura(String factura) {
        Factura facturaLimpia =   extractorFacturas.analizarTexto(factura);
        return ResponseEntity.ok(facturaLimpia);
    }
}
