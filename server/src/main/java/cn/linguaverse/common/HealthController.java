package cn.linguaverse.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 白名单探活：同时验证数据库连通性。 */
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return R.ok(Map.of(
                "status", one != null && one == 1 ? "UP" : "DOWN",
                "service", "linguaverse-server",
                "time", System.currentTimeMillis()));
    }
}
