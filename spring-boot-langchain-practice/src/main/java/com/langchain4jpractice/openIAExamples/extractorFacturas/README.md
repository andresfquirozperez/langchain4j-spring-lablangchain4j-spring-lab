# Extractor de Facturas - Extracción Estructurada con IA

Este paquete demuestra el patrón **@AiService declarativo** de LangChain4j para extraer datos estructurados de texto no estructurado.

## ¿Qué hace?

Convierte texto libre de una factura en un objeto `Factura` tipado con campos:
- cliente (String)
- fecha (String)
- total (double)
- productos (String[])

## Diagrama de Flujo

```
Texto de Factura (String)
    ↓
ExtractorFacturas.analizarTexto()
    ↓
LangChain4j + OpenAI analiza y extrae
    ↓
Retorna objeto Factura tipado
```

## Componentes

### 1. Factura.java
Record Java (POJO inmutable) que define la estructura de datos.

**Ejemplo:**
```java
public record Factura(
    String cliente,      // "Juan Pérez"
    String fecha,        // "2024-01-15"
    double total,        // 150.00
    String[] productos   // ["Laptop", "Mouse"]
) {}
```

**Ventajas de usar Record:**
- Inmutable por defecto
- Genera automáticamente: constructor, getters, equals, hashCode, toString
- Menos código boilerplate
- Perfecto para DTOs (Data Transfer Objects)

### 2. ExtractorFacturas.java
Interfaz declarativa LangChain4j. Solo define el contrato, LangChain4j genera la implementación automáticamente.

```java
public interface ExtractorFacturas {
    @UserMessage("Extrae la informacion de la factura")
    Factura analizarTexto(String textoSucio);
}
```

**¿Cómo funciona?**
1. El usuario pasa texto libre de una factura
2. LangChain4j genera un prompt optimizado para extracción
3. OpenAI analiza y devuelve JSON estructurado
4. LangChain4j mapea automáticamente al record Factura

### 3. FacturasController.java
Controller REST que expone el endpoint para extracción de facturas.

### 4. ExtractorFacturasConfig.java
Placeholder para configuración adicional. Actualmente vacío porque usa configuración por defecto.

## Flujo Detallado

```
Usuario (HTTP POST/GET con texto de factura)
    ↓
FacturasController
    ↓
ExtractorFacturas.analizarTexto(textoSucio)
    ↓
LangChain4j @AiService intercepta la llamada
    ↓
Construye prompt automático:
    "Extrae la informacion de la factura: [TEXTO]"
    ↓
OpenAI GPT-4o-mini procesa
    ↓
Retorna JSON estructurado
    ↓
LangChain4j deserializa a Factura record
    ↓
Retorna objeto Factura al Controller
    ↓
ResponseEntity<Factura> al usuario
```

## Comparación: @AiService vs Manual

| Aspecto | @AiService (Este) | Manual (manualAssistant) |
|---------|-------------------|--------------------------|
| Código | Mínimo (interfaz) | Verboso |
| Control | Limitado (LangChain4j maneja todo) | Total |
| Flexibilidad | Baja | Alta (puedes modificar cada paso) |
| Telemetría | Automática | Manual (tú implementas tracking) |
| Ideal para | Extracción estructurada simple | Lógica compleja con overrides |

## Ejemplo de Uso

### Input (Texto libre):
```
Factura #123 emitida a Juan Pérez el 15 de enero de 2024.
Productos adquiridos: Laptop Dell XPS $1200, Mouse Logitech $50.
Total a pagar: $1250.00
```

### Output (Objeto Factura):
```json
{
  "cliente": "Juan Pérez",
  "fecha": "2024-01-15",
  "total": 1250.00,
  "productos": ["Laptop Dell XPS", "Mouse Logitech"]
}
```

## Configuración del Modelo

Este paquete usa configuración por defecto de LangChain4j:
- **Modelo**: Hereda de configuración global (gpt-4o-mini)
- **Temperatura**: Default (0.7)
- **Máximo tokens**: Default

Para personalizar, agregar en `ExtractorFacturasConfig.java`:
```java
@Bean
ExtractorFacturas extractorFacturasService(ChatModel chatModel) {
    return AiServices.builder(ExtractorFacturas.class)
        .chatModel(chatModel)
        .build();
}
```

## Casos de Uso

✅ **Ideal para:**
- Procesamiento de facturas, recibos, comprobantes
- Extracción de datos de formularios escaneados
- Parsing de correos electrónicos estructurados
- Conversión de texto libre a objetos tipados

❌ **No ideal para:**
- Lógica de negocio compleja
- Cuando necesitas control total del prompt
- Procesamiento masivo con telemetría detallada

## Ventajas del Enfoque Declarativo

1. **Rápido desarrollo**: Solo defines la interfaz
2. **Mantenible**: Cambios en la estructura solo requieren modificar el record
3. **Type-safe**: El compilador garantiza la estructura de datos
4. **Testing fácil**: Puedes mockear la interfaz directamente

## Limitaciones

1. **Menos control**: No puedes ver/modificar el prompt generado
2. **Telemetría limitada**: No tienes acceso directo a tokens usados
3. **Debugging difícil**: Si la extracción falla, es más difícil diagnosticar
4. **Dependencia**: Fuertemente acoplado a LangChain4j @AiService

## Archivos del Paquete

```
extractorFacturasService/
├── Factura.java                    # Record de datos estructurados
├── ExtractorFacturas.java          # Interfaz declarativa @AiService
├── ExtractorFacturasConfig.java    # Configuración (placeholder)
├── FacturasController.java         # API REST
└── README.md                       # Este archivo
```

## Proximos Pasos Sugeridos

1. **Validación**: Agregar validación de campos (ej: total > 0)
2. **Formatos**: Soportar múltiples formatos de factura
3. **Batch**: Procesar múltiples facturas en una llamada
4. **Testing**: Tests unitarios con facturas de ejemplo
5. **Logging**: Agregar logging de extracciones exitosas/fallidas

## Notas de Implementación

- Usa Java Record para inmutabilidad y menos código
- La interfaz no necesita anotaciones adicionales
- LangChain4j maneja automáticamente la serialización/deserialización
- El record debe tener campos compatibles con JSON
