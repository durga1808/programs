package com.zaga.repo;

import com.zaga.entity.queryentity.metric.MetricDTO;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<MetricDTO> {
    
public List<MetricDTO> getMetricData(int timeAgoMinutes, String serviceName) {
    LocalDateTime timeAgo = LocalDateTime.now().minusMinutes(timeAgoMinutes);

    Date timeAgoDate = Date.from(timeAgo.atZone(ZoneId.systemDefault()).toInstant());


    PanacheQuery<MetricDTO> query = find("serviceName = ?1 and date >= ?2", serviceName, timeAgoDate);
    List<MetricDTO> result = query.list();
    return result;
}
    
}
