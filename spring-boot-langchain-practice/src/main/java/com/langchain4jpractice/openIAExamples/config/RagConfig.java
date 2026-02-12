package com.langchain4jpractice.openIAExamples.config;

import com.langchain4jpractice.openIAExamples.service.RagAssistantService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
public class RagConfig {

    // 1. EL TRADUCTOR (Texto -> Números)
    // Usamos 'all-minilm-l6-v2'. Es un modelo pequeño, rápido y GRATIS que corre en tu RAM.
    // Transforma "Hola" en un vector de [0.1, -0.5, 0.8, ...]
    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    // 2. LA MEMORIA (Base de Datos Vectorial)
    // Aquí guardamos los vectores. En producción usarías Pinecone o Mongo.
    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    // 3. EL INGESTADOR (ETL)
    // Este método se ejecuta AUTOMÁTICAMENTE al iniciar Spring Boot.
    @Bean
    ApplicationRunner ingestor(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        return args -> {
            // A. Cargar el archivo
            URL url = getClass().getClassLoader().getResource("datos_empresa.txt");
            if (url == null) {
                System.out.println("❌ ERROR: No encontré datos_empresa.txt en resources");
                return;
            }
            Path path = Paths.get(url.toURI());
            Document document = FileSystemDocumentLoader.loadDocument(path, new TextDocumentParser());

            // B. Configurar el Ingestor
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(300, 0)) // Corta en trozos de 300 caracteres
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            // C. Procesar
            ingestor.ingest(document);
            System.out.println("✅ DATOS INGESTADOS: El sistema ya conoce la política de la empresa.");

            // --- 🔬 ZONA DE LABORATORIO: VER LA MATRIX ---
            System.out.println("\n--- 🕵️‍♂️ INSPECCIONANDO LA MATRIZ (VECTORES) ---");

            String frasePrueba = "El CEO es Pandebono";

            // 1. Convertimos la frase a números usando el modelo
            Embedding vector = embeddingModel.embed(frasePrueba).content();

            // 2. Imprimimos los datos técnicos
            System.out.println("Texto analizado: \"" + frasePrueba + "\"");
            System.out.println("Dimensiones del vector: " + vector.dimension()); // Debería ser 384

            // 3. Imprimimos solo los primeros 10 números para no llenar la pantalla
            float[] vectorArray = vector.vector();
            System.out.println("Primeros 10 valores: " + Arrays.toString(Arrays.copyOfRange(vectorArray, 0, 10)) + "...");

            // Si quieres ver TODO el vector (384 números), descomenta la siguiente línea:
            System.out.println("Vector completo: " + Arrays.toString(vectorArray));

            System.out.println("----------------------------------------------\n");
        };
    }

    // 4. EL SERVICIO AI (El Ensamblador)
    // Aquí conectamos el ChatModel (OpenAI) con nuestra Memoria (EmbeddingStore)
    @Bean
    RagAssistantService ragAssistant(ChatModel chatModel, EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        // El 'ContentRetriever' es el bibliotecario que busca la info relevante
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)   // Solo trae los 2 trozos más parecidos
                .minScore(0.6)   // Que se parezcan al menos un 60%
                .build();

        /*
         Java utiliza una técnica avanzada llamada Reflexión y Proxies Dinámicos.
         LangChain4j lee tu interface RagAssistantService.class en tiempo de ejecución y genera el código sucio
         (el de arriba) automáticamente en la memoria RAM.
         */
        return AiServices.builder(RagAssistantService.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever) // <--- AQUÍ ACTIVAMOS RAG
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}