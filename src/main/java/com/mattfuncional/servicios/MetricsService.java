package com.mattfuncional.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final ConcurrentHashMap<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> responseTimeCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastAccessTimes = new ConcurrentHashMap<>();
    
    // Métricas de caché
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // Métricas de base de datos
    private final AtomicLong dbQueries = new AtomicLong(0);
    private final AtomicLong dbQueryTime = new AtomicLong(0);
    
    /**
     * Registra una petición HTTP
     */
    public void recordHttpRequest(String endpoint, long responseTimeMs) {
        requestCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        responseTimeCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).addAndGet(responseTimeMs);
        lastAccessTimes.put(endpoint, LocalDateTime.now());
        
        logger.debug("HTTP Request: {} - {}ms", endpoint, responseTimeMs);
    }
    
    /**
     * Registra un hit de caché
     */
    public void recordCacheHit(String cacheName) {
        cacheHits.incrementAndGet();
        logger.debug("Cache HIT: {}", cacheName);
    }
    
    /**
     * Registra un miss de caché
     */
    public void recordCacheMiss(String cacheName) {
        cacheMisses.incrementAndGet();
        logger.debug("Cache MISS: {}", cacheName);
    }
    
    /**
     * Registra una consulta de base de datos
     */
    public void recordDbQuery(String query, long executionTimeMs) {
        dbQueries.incrementAndGet();
        dbQueryTime.addAndGet(executionTimeMs);
        logger.debug("DB Query: {} - {}ms", query, executionTimeMs);
    }
    
    /**
     * Obtiene estadísticas de rendimiento
     */
    public PerformanceStats getPerformanceStats() {
        PerformanceStats stats = new PerformanceStats();
        
        // Estadísticas de peticiones HTTP
        stats.setTotalRequests(requestCounters.values().stream().mapToLong(AtomicLong::get).sum());
        stats.setAverageResponseTime(
            responseTimeCounters.values().stream().mapToLong(AtomicLong::get).sum() / 
            Math.max(1, requestCounters.values().stream().mapToLong(AtomicLong::get).sum())
        );
        
        // Estadísticas de caché
        long totalCacheAccess = cacheHits.get() + cacheMisses.get();
        stats.setCacheHitRate(totalCacheAccess > 0 ? (double) cacheHits.get() / totalCacheAccess : 0.0);
        stats.setCacheHits(cacheHits.get());
        stats.setCacheMisses(cacheMisses.get());
        
        // Estadísticas de base de datos
        stats.setTotalDbQueries(dbQueries.get());
        stats.setAverageDbQueryTime(
            dbQueries.get() > 0 ? (double) dbQueryTime.get() / dbQueries.get() : 0.0
        );
        
        return stats;
    }
    
    /**
     * Clase interna para estadísticas de rendimiento
     */
    public static class PerformanceStats {
        private long totalRequests;
        private double averageResponseTime;
        private double cacheHitRate;
        private long cacheHits;
        private long cacheMisses;
        private long totalDbQueries;
        private double averageDbQueryTime;
        
        // Getters y setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
        
        public long getCacheHits() { return cacheHits; }
        public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }
        
        public long getCacheMisses() { return cacheMisses; }
        public void setCacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; }
        
        public long getTotalDbQueries() { return totalDbQueries; }
        public void setTotalDbQueries(long totalDbQueries) { this.totalDbQueries = totalDbQueries; }
        
        public double getAverageDbQueryTime() { return averageDbQueryTime; }
        public void setAverageDbQueryTime(double averageDbQueryTime) { this.averageDbQueryTime = averageDbQueryTime; }
    }
} 