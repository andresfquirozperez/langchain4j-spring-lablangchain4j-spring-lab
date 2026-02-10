package com.langchain4jpractice.openIAExamples.controller;

import com.langchain4jpractice.openIAExamples.service.ManualAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manual")
public class ManualAssistantController {

    ManualAssistantService manualAssistantService;

    public ManualAssistantController(ManualAssistantService manualAssistantService) {
        this.manualAssistantService = manualAssistantService;
    }

    @GetMapping("/explain")
    public ResponseEntity<String> getExplainFunny(@RequestParam String topic) {
        String explain = manualAssistantService.getExplainFunny(topic);
        return ResponseEntity.ok(explain);
    }
}
