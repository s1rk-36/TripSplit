package learn.tripsplit.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unauthenticated liveness endpoint. Two jobs, both about surviving free hosting:
 *
 *  - Render spins a free web service down after ~15 minutes with no inbound
 *    traffic. An external scheduler hitting this route keeps the container up.
 *  - Aiven powers off a free database that sees no connections. The trivial
 *    query below makes every ping database traffic too, so one schedule keeps
 *    both ends awake.
 *
 * This always answers 200 while the application itself is running, even when the
 * database is unreachable. Render restarts a service that fails its health check,
 * so returning 503 on a database blip would take the whole API down instead of
 * leaving it up to serve the error. The body carries the real database state.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "up");

        try {
            jdbcTemplate.queryForObject("select 1", Integer.class);
            body.put("database", "up");
        } catch (Exception ex) {
            body.put("database", "down");
        }

        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
