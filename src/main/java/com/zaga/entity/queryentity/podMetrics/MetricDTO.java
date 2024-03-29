package com.zaga.entity.queryentity.podMetrics;


import java.util.Date;

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
    private Date date;
    private Double cpuUsage;
    private Long memoryUsage;

    // private String serviceName;
    
}

