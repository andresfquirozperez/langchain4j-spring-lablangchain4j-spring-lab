# Practice RAG - Retrieval-Augmented Generation

Implementación completa de **RAG (Generación Aumentada por Recuperación)** - IA que consulta documentos antes de responder.

## ¿Qué es RAG?

RAG permite que la IA responda basándose en información específica de documentos, no solo en su conocimiento general pre-entrenado.

**Problema que resuelve:**
- IA general no sabe sobre documentos privados de tu empresa
- RAG enriquece el prompt con contexto relevante de tus documentos
- Resultado: Respuestas precisas basadas en tus datos

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    FASE 1: INGESTA                          │
│         (Se ejecuta automáticamente al iniciar)             │
└─────────────────────────────────────────────────────────────┘
                      │
    datos_empresa.txt ↓
                      │
            ┌─────────────────┐
            │ Document Loader │ ← Carga archivo desde resources
            └────────┬────────┘
                     │
            ┌────────▼────────┐
            │  Splitter       │ ← Divide en chunks de 300 chars
            │  (300, 0)       │   (tamaño, overlap)
            └────────┬────────┘
                     │
            ┌────────▼────────┐
            │ Embedding Model │ ← all-MiniLM-L6-v2 (local, gratis)
            │ (Texto→Vector)  │   Convierte texto a vector numérico
            └────────┬────────┘
                     │
            ┌────────▼────────┐
            │ EmbeddingStore  │ ← InMemory (dev) / Pinecone (prod)
            └─────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   FASE 2: CONSULTA                          │
│           (Cada vez que el usuario pregunta)                │
└─────────────────────────────────────────────────────────────┘
                      │
    Pregunta usuario  ↓
                      │
            ┌─────────────────┐
            │ Embedding Model │ ← Mismo modelo de ingestión
            └────────┬────────┘
                     │
            ┌────────▼────────────────┐
            │   ContentRetriever      │ ← Busca en Vector DB
            │   (maxResults: 2)       │   Top 2 más similares
            │   (minScore: 0.6)       │   Mínimo 60% similitud
            └────────┬────────────────┘
                     │
            ┌────────▼────────────────┐
            │  Contexto Relevante     │ ← Chunks que coinciden
            └────────┬────────────────┘
                     │
            ┌────────▼────────────────┐
            │  Enriquecimiento        │
            │  de Prompt              │ ← "Basado en: [contexto]...
            └────────┬────────────────┘    Responde: [pregunta]"
                     │
            ┌────────▼────────────────┐
            │      OpenAI GPT         │ ← Responde con contexto
            └─────────────────────────┘
```

## Componentes

### 1. RagConfig.java
Configuración completa del pipeline RAG con 4 beans principales:

#### Bean 1: EmbeddingModel
```java
@Bean
EmbeddingModel embeddingModel() {
    return new AllMiniLmL6V2EmbeddingModel();
}
```

**Función:** Convierte texto a vectores numéricos (embeddings)
- **Modelo:** all-MiniLM-L6-v2
- **Ventajas:** Gratis, corre localmente en tu máquina, no requiere API key
- **Dimensiones:** 384 valores por texto
- **Uso:** Mismo modelo para ingestión Y consulta

#### Bean 2: EmbeddingStore
```java
@Bean
EmbeddingStore<TextSegment> embeddingStore() {
    return new InMemoryEmbeddingStore<>();
}
```

**Función:** Base de datos vectorial que almacena los embeddings
- **Implementación:** InMemoryEmbeddingStore (para desarrollo)
- **Producción:** Reemplazar por Pinecone, Weaviate, MongoDB Atlas
- **Almacena:** Tuplas de (vector, texto original, metadata)

#### Bean 3: Ingestor (ApplicationRunner)
```java
@Bean
ApplicationRunner ingestor(EmbeddingModel em, EmbeddingStore<TextSegment> es) {
    return args -> {
        // 1. Carga archivo datos_empresa.txt
        // 2. Divide en chunks de 300 caracteres
        // 3. Genera embeddings
        // 4. Almacena en EmbeddingStore
        // 5. Imprime diagnóstico con ejemplo de vector
    };
}
```

**Función:** Pipeline ETL que corre automáticamente al iniciar Spring Boot
- **Trigger:** ApplicationRunner ejecuta al arrancar la app
- **Proceso:** Carga → Fragmenta → Embedding → Almacena
- **Output:** Vector DB lista para consultas

#### Bean 4: RagAssistantService
```java
@Bean
RagAssistantService ragAssistant(ChatModel chatModel, 
                                  EmbeddingStore<TextSegment> store, 
                                  EmbeddingModel model) {
    ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
        .embeddingStore(store)
        .embeddingModel(model)
        .maxResults(2)    // Solo top 2 resultados
        .minScore(0.6)    // Mínimo 60% de similitud
        .build();

    return AiServices.builder(RagAssistantService.class)
        .chatModel(chatModel)
        .contentRetriever(retriever)  // <-- ACTIVA RAG
        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
        .build();
}
```

**Función:** Servicio AI con RAG integrado
- **ContentRetriever:** El "bibliotecario" que busca info relevante
- **ChatMemory:** Mantiene contexto de conversación (últimos 10 mensajes)
- **AiServices:** Genera implementación automática de la interfaz

### 2. RagAssistantService.java
Interfaz declarativa que expone el método de chat:

```java
public interface RagAssistantService {
    // LangChain4j automáticamente:
    // 1. Toma tu pregunta
    // 2. Busca contexto relevante en el EmbeddingStore
    // 3. Enriquece el prompt con ese contexto
    // 4. Envía a OpenAI
    // 5. Retorna la respuesta
    String chatear(String userMessage);
}
```

### 3. RagController.java
Expone el endpoint REST:

```java
@RestController
@RequestMapping("/api/rag")
public class RagController {
    
    @GetMapping("/ask")
    public String preguntar(@RequestParam String query) {
        return assistant.chatear(query);
    }
}
```

**Endpoint:** `GET /api/rag/ask?query={tu_pregunta}`

## ¿Qué son los Embeddings?

Los embeddings son representaciones numéricas de texto donde:
- Cada texto se convierte en un vector de N dimensiones
- Textos con significado similar tienen vectores cercanos
- Permiten búsqueda semántica (por concepto, no por palabra exacta)

### Ejemplo Visual

```
Texto: "El CEO es Pandebono"
         ↓
Embedding Model (all-MiniLM-L6-v2)
         ↓
Vector: [0.12, -0.45, 0.89, -0.23, 0.67, ...] (384 dimensiones)
```

**Búsqueda semántica:**
```
Pregunta: "¿Quién dirige la empresa?"
Vector pregunta: [0.10, -0.42, 0.85, -0.20, 0.65, ...]
                              ↑
                    Similaridad ~95% con "El CEO es Pandebono"
```

## Flujo Detallado

### Fase 1: Ingesta (Al Iniciar App)

1. **Carga de Documento:**
   ```
   datos_empresa.txt → FileSystemDocumentLoader
   ```

2. **Fragmentación (Splitting):**
   ```
   Documento completo → Chunks de 300 caracteres
   "La empresa..."     → "La empresa fue fundada..."
   "fue fundada..."    → "fue fundada en 2020..."
   ```

3. **Embedding (Vectorización):**
   ```
   Cada chunk → EmbeddingModel → Vector de 384 dimensiones
   ```

4. **Almacenamiento:**
   ```
   Vector + Texto original → EmbeddingStore
   ```

5. **Diagnóstico:**
   ```
   Imprime ejemplo de vector para verificación
   "El CEO es Pandebono" → [0.12, -0.45, 0.89, ...]
   ```

### Fase 2: Consulta (Runtime)

1. **Recibe Pregunta:**
   ```
   GET /api/rag/ask?query=¿Quién es el CEO?
   ```

2. **Embedding de Pregunta:**
   ```
   "¿Quién es el CEO?" → EmbeddingModel → Vector
   ```

3. **Búsqueda Semántica:**
   ```
   ContentRetriever busca en EmbeddingStore
   - maxResults: 2 (solo top 2)
   - minScore: 0.6 (mínimo 60% similitud)
   ```

4. **Recuperación de Contexto:**
   ```
   Si encuentra chunks relevantes:
     → "El CEO es Pandebono..."
     → "La empresa fue fundada..."
   ```

5. **Enriquecimiento de Prompt:**
   ```
   Prompt final = """
   Basado en la siguiente información:
   ---
   [Chunk 1: El CEO es Pandebono...]
   [Chunk 2: La empresa fue fundada...]
   ---
   Responde la pregunta: ¿Quién es el CEO?
   """
   ```

6. **Respuesta IA:**
   ```
   OpenAI GPT procesa el prompt enriquecido
   ↓
   Retorna respuesta basada SOLO en el contexto proporcionado
   ```

## Parámetros Clave

### Configuración de Splitting
```java
.recursive(300, 0)
// (tamaño_chunk, overlap)
// 300 = caracteres por chunk
// 0 = caracteres de superposición entre chunks
```

**Consideraciones:**
- **Tamaño pequeño (100-300):** Más granularidad, mejor precisión, más vectores
- **Tamaño grande (500-1000):** Menos vectores, más contexto por chunk, menos preciso
- **Overlap:** Evita perder información en los límites de chunks

### Configuración de Búsqueda
```java
.maxResults(2)    // Cuántos chunks traer
.minScore(0.6)    // Umbral mínimo de similitud (0.0 - 1.0)
```

**maxResults:**
- **1:** Solo el más relevante (muy enfocado)
- **2-3:** Balance (recomendado)
- **5+:** Más contexto pero puede confundir al modelo

**minScore:**
- **0.8+:** Muy estricto, solo respuestas muy relevantes
- **0.6:** Balance (usado aquí)
- **0.4:** Permite respuestas menos relevantes

## Comparación: Con vs Sin RAG

| Aspecto | Sin RAG | Con RAG (Este) |
|---------|---------|----------------|
| Conocimiento | General del modelo | Específico de documentos |
| Actualización | Reentrenar modelo | Cambiar archivo txt |
| Precisión | Puede "alucinar" | Basada en fuentes verificables |
| Citas | No puede citar fuentes | Puede referenciar documentos |
| Uso ideal | Preguntas generales | FAQs sobre documentos específicos |

## Ejemplo de Uso

### Usando curl
```bash
curl "http://localhost:8080/api/rag/ask?query=¿Quién es el CEO?"
```

### Usando navegador
```
http://localhost:8080/api/rag/ask?query=¿Cuándo se fundó la empresa?
```

### Respuesta esperada
```
Según la política de la empresa documentada, el CEO es Pandebono 
y la empresa fue fundada en 2020...
```

## Archivos Requeridos

- `src/main/resources/datos_empresa.txt` - Documento base para RAG
  - Debe existir antes de iniciar la aplicación
  - Formato: Texto plano
  - Contenido: Información de la empresa, políticas, FAQs, etc.

## Producción vs Desarrollo

### Desarrollo (Configuración actual)
| Componente | Implementación | Características |
|------------|----------------|-----------------|
| EmbeddingStore | InMemoryEmbeddingStore | Rápido, volátil, gratis |
| EmbeddingModel | all-MiniLM-L6-v2 | Local, gratis, 384 dims |
| Persistencia | No | Se pierde al reiniciar |

### Producción (Recomendaciones)
| Componente | Implementación | Características |
|------------|----------------|-----------------|
| EmbeddingStore | Pinecone / Weaviate / MongoDB Atlas | Persistente, escalable |
| EmbeddingModel | OpenAI text-embedding-3-small | Mejor calidad, pago por uso |
| Persistencia | Sí | Datos sobreviven reinicios |
| Escalabilidad | Alta | Millones de vectores |

## Ventajas de este Enfoque

1. **Respuestas contextualizadas:** La IA usa TU información
2. **Actualización fácil:** Solo cambia el archivo txt
3. **Transparencia:** Sabes de dónde viene la información
4. **Control:** Puedes limitar qué información accede la IA
5. **Costo:** Embeddings locales son gratis

## Limitaciones y Consideraciones

1. **Tamaño de documento:** Documentos muy grandes requieren más memoria
2. **Calidad de splitting:** Cortes malos pueden perder contexto
3. **Relevancia:** Dependes de la calidad del embedding model
4. **Latencia:** Búsqueda vectorial + llamada a OpenAI = más tiempo
5. **Memoria:** InMemoryEmbeddingStore no persiste entre reinicios

## Debugging y Troubleshooting

### Problema: "No encuentra información relevante"
- **Causa:** minScore muy alto o maxResults muy bajo
- **Solución:** Reducir minScore a 0.5 o aumentar maxResults a 3

### Problema: "Respuestas demasiado largas/cortas"
- **Causa:** Tamaño de chunks inadecuado
- **Solución:** Ajustar .recursive(300, 0) - probar 200 o 500

### Problema: "Información incompleta"
- **Causa:** Overlap = 0, información se corta entre chunks
- **Solución:** Aumentar overlap a 50 o 100 caracteres

## Archivos del Paquete

```
practiceRAG/
├── RagConfig.java              # Configuración completa del pipeline
├── RagAssistantService.java    # Interfaz declarativa del asistente
├── RagController.java          # API REST endpoint
└── README.md                   # Este archivo
```

## Próximos Pasos Sugeridos

1. **Persistencia:** Migrar a Pinecone o Weaviate para producción
2. **Múltiples documentos:** Soportar varios archivos txt
3. **Actualización en caliente:** Endpoint para recargar documentos
4. **Metadata:** Agregar tags/categorías a los chunks
5. **Re-ranking:** Segunda pasada para mejorar orden de resultados
6. **Streaming:** Respuestas en streaming para mejor UX
7. **Citas:** Mostrar qué chunks se usaron para la respuesta

## Recursos Adicionales

- **LangChain4j RAG Docs:** https://docs.langchain4j.dev/tutorials/rag
- **Embeddings explicado:** https://huggingface.co/blog/getting-started-with-embeddings
- **Vector Databases:** https://www.pinecone.io/learn/vector-database/

## Notas de Implementación

- El `ApplicationRunner` se ejecuta solo al iniciar la aplicación
- Si `datos_empresa.txt` no existe, el sistema imprime error pero continúa
- Los embeddings se calculan una sola vez (durante ingestión)
- La búsqueda vectorial es muy rápida (milisegundos)
- ChatMemory mantiene contexto de conversación para preguntas de seguimiento
