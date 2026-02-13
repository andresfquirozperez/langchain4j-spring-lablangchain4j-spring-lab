package com.langchain4jpractice.openIAExamples.extractorFacturas;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturasController {

    private final ExtractorFacturasService extractorFacturasService;

    public FacturasController(ExtractorFacturasService extractorFacturasService) {
        this.extractorFacturasService = extractorFacturasService;
    }

    /**
     * Endpoint básico: Extrae factura y retorna el objeto completo
     * Uso: GET /api/v1/facturas/extraer?factura=Factura%20a%20Juan%20Perez...
     */
    @GetMapping("/extraer")
    public ResponseEntity<?> extractorDeFactura(@RequestParam String factura) {
        // Validar que el texto no esté vacío
        if (factura == null || factura.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Error: El texto de la factura no puede estar vacío");
        }

        // Llamar al servicio que usa ObjectMapper internamente
        FacturaDTO facturaDTO = extractorFacturasService.analizarTexto(factura);

        // Manejar caso de error (cuando el parsing falla)
        if (facturaDTO == null) {
            return ResponseEntity.badRequest()
                    .body("Error: No se pudo extraer la información de la factura");
        }

        return ResponseEntity.ok(facturaDTO);
    }

    /**
     * Endpoint avanzado: Muestra cómo acceder a cada campo del FacturaDTO
     * Uso: GET /api/v1/facturas/detalles?factura=...
     */
    @GetMapping("/detalles")
    public ResponseEntity<?> extraerConDetalles(@RequestParam String factura) {
        if (factura == null || factura.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Error: El texto de la factura no puede estar vacío");
        }

        FacturaDTO dto = extractorFacturasService.analizarTexto(factura);

        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body("Error: No se pudo extraer la información");
        }

        // ============================================
        // EJEMPLOS DE ACCESO A DATOS DEL OBJETO
        // ============================================

        // 1. Acceder a campos individuales (métodos del Record)
        String nombreCliente = dto.cliente();
        String fechaFactura = dto.fecha();
        double montoTotal = dto.total();
        String[] listaProductos = dto.productos();

        // 2. Procesar los productos (array)
        int cantidadProductos = listaProductos.length;
        String productosConcatenados = String.join(", ", listaProductos);

        // 3. Calcular precio promedio por producto
        double promedioPorProducto = cantidadProductos > 0 ? montoTotal / cantidadProductos : 0;

        // 4. Crear un resumen personalizado
        String resumen = String.format(
            "Factura de %s del %s. Total: $%.2f (%d productos)",
            nombreCliente, fechaFactura, montoTotal, cantidadProductos
        );

        // 5. Construir respuesta con todos los datos procesados
        Map<String, Object> respuesta = new HashMap<>();

        // Datos originales extraídos
        respuesta.put("datosExtraidos", Map.of(
            "cliente", nombreCliente,
            "fecha", fechaFactura,
            "total", montoTotal,
            "productos", Arrays.asList(listaProductos)  // Convertir array a lista
        ));

        // Datos procesados/calculados
        respuesta.put("analisis", Map.of(
            "cantidadProductos", cantidadProductos,
            "productosLista", productosConcatenados,
            "promedioPorProducto", String.format("$%.2f", promedioPorProducto),
            "resumen", resumen
        ));

        // Información del objeto
        respuesta.put("infoObjeto", Map.of(
            "tipo", dto.getClass().getSimpleName(),
            "stringRepresentation", dto.toString()
        ));

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint de ejemplo: Retorna solo campos específicos
     * Uso: GET /api/v1/facturas/resumen?factura=...
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(@RequestParam String factura) {
        if (factura == null || factura.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Error: El texto de la factura no puede estar vacío");
        }

        FacturaDTO dto = extractorFacturasService.analizarTexto(factura);

        if (dto == null) {
            return ResponseEntity.badRequest()
                    .body("Error: No se pudo extraer la información");
        }

        // Ejemplo: Retornar solo información financiera
        Map<String, Object> resumenFinanciero = new HashMap<>();
        resumenFinanciero.put("cliente", dto.cliente());
        resumenFinanciero.put("montoTotal", dto.total());
        resumenFinanciero.put("cantidadItems", dto.productos().length);
        resumenFinanciero.put("tieneProductos", dto.productos().length > 0);

        return ResponseEntity.ok(resumenFinanciero);
    }
}
