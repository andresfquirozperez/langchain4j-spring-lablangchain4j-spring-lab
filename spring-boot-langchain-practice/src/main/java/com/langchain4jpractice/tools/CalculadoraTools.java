package com.langchain4jpractice.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraTools {

    @Tool("Calcula la suma de dos números")
    public double sumar(double a, double b) {
        System.out.println("🤖 IA: Estoy usando la herramienta SUMAR para " + a + " + " + b);
        return a + b;
    }

    @Tool("Calcula la multiplicación de dos números")
    public double multiplicar(double a, double b) {
        System.out.println("🤖 IA: Estoy usando la herramienta MULTIPLICAR para " + a + " * " + b);
        return a * b;
    }

    @Tool("Obtiene la longitud de una palabra")
    public int contarLetras(String palabra) {
        return palabra.length();
    }
}
