package com.langchain4jpractice.openIAExamples.manualAssistant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone el endpoint del asistente manual.
 *
 * Endpoint: GET /api/v1/manual/explain?topic={tema}
 *
 * Actua como punto de entrada HTTP, delegando toda la logica
 * al ManualAssistantService. Mantiene separacion de responsabilidades:
 * - Controller: Maneja HTTP (entrada/salida)
 * - Service: Contiene logica de negocio y llamadas a IA
 *
 * Ejemplo de uso:
 * GET http://localhost:8080/api/v1/manual/explain?topic=Java
 */
@RestController
@RequestMapping("/api/v1/manual")
public class ManualAssistantController {

    // Servicio inyectado por constructor (Spring maneja la dependencia automaticamente)
    private final ManualAssistantService manualAssistantService;

    // Constructor con inyeccion de dependencias
    public ManualAssistantController(ManualAssistantService manualAssistantService) {
        this.manualAssistantService = manualAssistantService;
    }

    /**
     * Genera una explicacion humoristica sobre el tema proporcionado.
     *
     * @param topic El tema sobre el cual se quiere la explicacion
     * @return ResponseEntity con la explicacion humoristica generada por IA
     */
    @GetMapping("/explain")
    public ResponseEntity<String> getExplainFunny(@RequestParam String topic) {
        // Delega al servicio que contiene la logica de IA
        String explain = manualAssistantService.getExplainFunny(topic);
        return ResponseEntity.ok(explain);
    }
}

