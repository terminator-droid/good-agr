package org.example.http.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.IntegratedParsingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.example.service.IntegratedParsingService.*;

@RestController
@RequestMapping("/api/parsing")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ParsingController {

    private final IntegratedParsingService parsingService;

    /**
     * Получение статистики парсинга
     */
    @GetMapping("/stats")
    public ResponseEntity<ParsingStats> getStats() {
        ParsingStats stats = parsingService.getParsingStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Запуск парсинга всех магазинов
     */
    @PostMapping("/run-all")
    public ResponseEntity<Map<String, String>> runFullParsing() {
        try {
            log.info("Manual parsing triggered from API");
            parsingService.parseAllShops();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Full parsing completed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in manual parsing", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Parsing failed: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Запуск парсинга только Лавки
     */
    @PostMapping("/run-lavka")
    public ResponseEntity<Map<String, String>> runLavkaParsing() {
        try {
            log.info("Lavka parsing triggered from API");
            parsingService.parseLavkaAsync();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Lavka parsing started (async)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting Lavka parsing", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start Lavka parsing: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Запуск парсинга только Самоката
     */
    @PostMapping("/run-samokat")
    public ResponseEntity<Map<String, String>> runSamokatParsing() {
        try {
            log.info("Samokat parsing triggered from API");
            parsingService.parseSamokatAsync();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Samokat parsing started (async)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting Samokat parsing", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start Samokat parsing: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Проверка здоровья парсеров (healthcheck)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            ParsingStats stats = parsingService.getParsingStats();

            health.put("status", "healthy");
            health.put("total_products", stats.getTotalProducts());
            health.put("lavka_products", stats.getLavkaProducts());
            health.put("samokat_products", stats.getSamokatProducts());
            health.put("last_update", stats.getLastUpdate());

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());

            return ResponseEntity.status(500).body(health);
        }
    }
}