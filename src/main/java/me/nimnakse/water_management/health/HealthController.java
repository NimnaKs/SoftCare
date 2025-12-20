package me.nimnakse.water_management.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Service and database health checks")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "Service health check", description = "Returns uptime status for the API service.")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "water-management",
                "time", OffsetDateTime.now().toString()
        );
    }

    @GetMapping("/db")
    @Operation(summary = "Database health check", description = "Returns connectivity status for the database.")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "service", "water-management",
                    "time", OffsetDateTime.now().toString()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN",
                    "service", "water-management",
                    "time", OffsetDateTime.now().toString(),
                    "error", ex.getMessage()
            ));
        }
    }
}
