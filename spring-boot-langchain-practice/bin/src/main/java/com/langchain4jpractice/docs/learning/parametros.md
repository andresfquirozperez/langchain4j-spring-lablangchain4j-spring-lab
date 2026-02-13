GUION TÉCNICO: LOS PARÁMETROS DE CONTROL DE LA IA (LANGCHAIN4J)
================================================================

GRUPO 1: CREATIVIDAD Y DETERMINISMO (EL CEREBRO)
------------------------------------------------
1. PARÁMETRO: temperature(Double)
    - Definición: Escalar que divide los logits. Controla el azar. T cercana a 0 es determinista; T cercana a 1 es creativa.
    - Contexto RAG: Úsalo en 0.0 para leyes, contratos y hechos exactos donde no quieres variaciones.
    - Ejemplo de Uso:
      ```java
      .temperature(0.0) // Para RAG estricto (Legal/Financiero)
      .temperature(0.7) // Para Chatbot conversacional
      .temperature(1.2) // Para Lluvia de ideas (Brainstorming)
      ```

2. PARÁMETRO: topP(Double) [Nucleus Sampling]
    - Definición: Considera solo el conjunto más pequeño de palabras cuya probabilidad suma P (ej. 0.9).
    - Contexto RAG: Es el filtro de calidad que elimina opciones absurdas o gramaticalmente incorrectas.
    - Ejemplo de Uso:
      ```java
      .topP(0.9) // El estándar de la industria. Descarta el 10% de "basura" estadística.
      ```

3. PARÁMETRO: topK(Integer)
    - Definición: Limita la elección estrictamente a las K palabras más probables (fuerza bruta), ignorando la suma de probabilidades.
    - Contexto RAG: Evita que el modelo se salga del guion buscando palabras extremadamente raras.
    - Ejemplo de Uso:
      ```java
      .topK(50) // Solo considera las 50 palabras más probables en cada paso.
      ```

GRUPO 2: DISCIPLINA Y REPETICIÓN
--------------------------------
4. PARÁMETRO: frequencyPenalty(Double)
    - Definición: Penaliza un token proporcionalmente a cuántas veces ya apareció.
    - Contexto RAG: Vital para evitar bucles de repetición ("El usuario, el usuario...").
    - Ejemplo de Uso:
      ```java
      .frequencyPenalty(0.5) // Penalización moderada para reducir repeticiones.
      ```

5. PARÁMETRO: presencePenalty(Double)
    - Definición: Penaliza un token si ha aparecido al menos una vez.
    - Contexto RAG: Obliga al modelo a introducir nuevos conceptos y no quedarse estancado en un tema.
    - Ejemplo de Uso:
      ```java
      .presencePenalty(0.6) // Fuerza al modelo a usar vocabulario variado.
      ```

6. PARÁMETRO: maxOutputTokens(Integer)
    - Definición: Límite duro de cantidad de tokens generados.
    - Contexto RAG: Control de costos (presupuesto) y latencia. Evita respuestas eternas.
    - Ejemplo de Uso:
      ```java
      .maxOutputTokens(500) // Aproximadamente 400 palabras. Corta si se pasa.
      ```

7. PARÁMETRO: stopSequences(List<String>)
    - Definición: Palabras clave que detienen la generación inmediatamente.
    - Contexto RAG: Evita que la IA alucine y escriba el rol del usuario en un chat.
    - Ejemplo de Uso:
      ```java
      .stopSequences(List.of("User:", "\n\nHuman:")) // Detiene si la IA intenta simular al humano.
      ```

GRUPO 3: INGENIERÍA Y HERRAMIENTAS
----------------------------------
8. PARÁMETRO: toolSpecifications(List<ToolSpecification>)
    - Definición: Manual de instrucciones de funciones Java para la IA (Schema).
    - Contexto RAG: Habilita el uso de métodos propios del backend (consultar SQL, APIs externas).
    - Ejemplo de Uso:
      ```java
      ToolSpecification tool = ToolSpecification.builder()
         .name("consultar_saldo")
         .description("Devuelve el saldo actual del cliente")
         .build();
      .toolSpecifications(List.of(tool))
      ```

9. PARÁMETRO: toolChoice(ToolChoice)
    - Definición: Obliga o sugiere el uso de una herramienta específica.
    - Contexto RAG: Fuerza la consulta a base de datos si la pregunta es factual, prohibiendo inventar.
    - Ejemplo de Uso:
      ```java
      .toolChoice(ToolChoice.AUTO)     // La IA decide si usa herramienta o texto.
      .toolChoice(ToolChoice.REQUIRED) // La IA ESTÁ OBLIGADA a llamar una función Java.
      ```

10. PARÁMETRO: responseFormat(ResponseFormat)
    - Definición: Obliga a una salida estructurada (JSON).
    - Contexto RAG: Convierte texto no estructurado en objetos Java/POJOs limpios.
    - Ejemplo de Uso:
     ```java
     // Obliga al modelo a responder SOLO JSON válido
     .responseFormat(ResponseFormat.JSON) 
     ```

GRUPO 4: CONFIGURACIÓN Y BUILDER
--------------------------------
11. PARÁMETRO: modelName(String)
    - Definición: Identificador del modelo (inteligencia/costo).
    - Contexto: Define qué "cerebro" procesará la solicitud.
    - Ejemplo de Uso:
     ```java
     .modelName("gpt-4o")        // Modelo más inteligente y caro
     .modelName("gpt-3.5-turbo") // Modelo rápido y barato
     ```

12. PARÁMETRO: overrideWith(ChatRequestParameters)
    - Definición: Patrón Builder para fusionar configuraciones.
    - Contexto: Permite tener una configuración "Default" y sobrescribir solo lo necesario para una request.
    - Ejemplo de Uso:
     ```java
     ChatRequestParameters defaultParams = ...; // Configuración global
     ChatRequestParameters specificParams = ChatRequestParameters.builder()
         .overrideWith(defaultParams) // Copia todo lo anterior
         .temperature(0.9)            // Cambia SOLO la temperatura para esta llamada
         .build();
     ```

13. PARÁMETRO: build()
    - Definición: Método final que crea la instancia del objeto inmutable.
    - Contexto: Valida y sella la configuración antes de enviarla al modelo.
    - Ejemplo de Uso:
     ```java
     ChatRequestParameters params = ChatRequestParameters.builder()
         .temperature(0.7)
         .maxOutputTokens(100)
         .build(); // <--- Aquí se crea el objeto final
     ```