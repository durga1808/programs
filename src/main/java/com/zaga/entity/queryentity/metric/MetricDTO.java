package com.zaga.entity.queryentity.metric;

import java.time.LocalDateTime;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
@MongoEntity(collection = "MetricDTO", database = "OtelMetric")
public class MetricDTO {
    private LocalDateTime date;
    private Long cpuUsage;
    private Long memoryUsage;
    private String serviceName;
    
}
