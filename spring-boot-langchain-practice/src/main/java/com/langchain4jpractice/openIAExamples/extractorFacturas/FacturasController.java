package com.langchain4jpractice.openIAExamples.extractorFacturas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturasController {

    ExtractorFacturasService extractorFacturasService;

    public FacturasController(ExtractorFacturasService extractorFacturasService) {
        this.extractorFacturasService = extractorFacturasService;
    }

    @GetMapping("/extraer")
    public ResponseEntity<FacturaDTO> extractorDeFactura(String factura) {
        FacturaDTO facturaDTOLimpia =   extractorFacturasService.analizarTexto(factura);
        return ResponseEntity.ok(facturaDTOLimpia);
    }
}
