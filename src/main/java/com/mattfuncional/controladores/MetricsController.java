package com.mattfuncional.controladores;

import com.mattfuncional.servicios.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        MetricsService.PerformanceStats stats = metricsService.getPerformanceStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalRequests", stats.getTotalRequests());
        response.put("averageResponseTime", String.format("%.2f ms", stats.getAverageResponseTime()));
        response.put("cacheHitRate", String.format("%.2f%%", stats.getCacheHitRate() * 100));
        response.put("cacheHits", stats.getCacheHits());
        response.put("cacheMisses", stats.getCacheMisses());
        response.put("totalDbQueries", stats.getTotalDbQueries());
        response.put("averageDbQueryTime", String.format("%.2f ms", stats.getAverageDbQueryTime()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthMetrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
} 