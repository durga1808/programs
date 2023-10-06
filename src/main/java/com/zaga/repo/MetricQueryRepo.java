package com.zaga.repo;

import java.time.LocalDateTime;
import java.util.List;

import com.zaga.entity.queryentity.metric.MetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<MetricDTO> {
    
      
    public List<MetricDTO> getMetricData(int timeAgoMinutes, String serviceName) {
        LocalDateTime timeAgo = LocalDateTime.now().minusMinutes(timeAgoMinutes);
        PanacheQuery<MetricDTO> query = find("serviceName = ?1 and date >= ?2", serviceName, timeAgo);
        return query.list();
    }
}
