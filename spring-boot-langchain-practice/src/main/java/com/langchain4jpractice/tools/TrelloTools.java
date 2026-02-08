package com.langchain4jpractice.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrelloTools {

    @Value("${trello.api-key}")
    private String apiKey;

    @Value("${trello.token}")
    private String token;

    @Value("${trello.board-id}")
    private String boardID;

    // Cliente HTTP de Spring
    private final RestTemplate restTemplate = new RestTemplate();

    @Tool("Crea una nueva tarjeta (card) en Trello en una lista específica")
    public String crearTarjeta(String idLista, String titulo, String descripcion) {
        System.out.println("🤖 IA: Intentando crear tarjeta en Trello...");
        System.out.println("   📝 Título: " + titulo);
        System.out.println("   📍 ID Lista: " + idLista);

        // 1. Construimos la URL segura con parámetros
        String url = UriComponentsBuilder.fromHttpUrl("https://api.trello.com/1/cards")
                .queryParam("idList", idLista)
                .queryParam("key", apiKey)
                .queryParam("token", token)
                .queryParam("name", titulo)
                .queryParam("desc", descripcion)
                .toUriString();

        // 2. Creamos el "Cuerpo" del mensaje (El JSON)
        // Usamos un Map para no crear una clase extra
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", titulo);
        requestBody.put("desc", descripcion);
        requestBody.put("pos", "top"); // Opcional: Para que aparezca arriba de la lista


        try {
            // 2. Hacemos la petición POST
            // Trello devuelve un JSON con la tarjeta creada, lo convertimos a String
            String respuesta = restTemplate.postForObject(url, requestBody, String.class);

            return "¡Éxito! Tarjeta creada correctamente en Trello.";

        } catch (Exception e) {
            // Si falla (por ejemplo, ID de lista incorrecto), la IA se enterará
            return "Error al crear la tarjeta: " + e.getMessage();
        }
    }

    @Tool("Obtiene todas las listas disponibles en el tablero actual con sus IDs")
    public String obtenerListasDelTablero() {
        System.out.println("🤖 IA: Buscando listas en el tablero...");

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.trello.com/1/boards/" + boardID + "/lists")
                .queryParam("key", apiKey)
                .queryParam("token", token)
                .toUriString();

        try {
            // Esto devuelve un JSON grande con todas las listas:
            // [{"id":"123", "name":"Pendientes"}, {"id":"456", "name":"Hecho"}]
            String jsonRespuesta = restTemplate.getForObject(url, String.class);

            return "Aquí están las listas y sus IDs: " + jsonRespuesta;

        } catch (Exception e) {
            return "Error al leer las listas: " + e.getMessage();
        }
    }
}