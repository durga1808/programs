package com.zaga.entity.kepler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zaga.entity.otelmetric.ResourceMetric;
import com.zaga.entity.otelmetric.ScopeMetric;
import com.zaga.entity.otelmetric.scopeMetric.Metric;
import com.zaga.entity.otelmetric.scopeMetric.sum.SumDataPoint;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties("id")
@MongoEntity(collection = "Metrics", database = "KeplerMetric")
public class KeplerMetric extends PanacheMongoEntity {
  private List<ResourceMetric> resourceMetrics;
 

  public Long getStartTimeUnixNano() {
    if (resourceMetrics != null && !resourceMetrics.isEmpty()) {
        ResourceMetric firstResourceMetric = resourceMetrics.get(0);
        if (firstResourceMetric != null && firstResourceMetric.getScopeMetrics() != null && !firstResourceMetric.getScopeMetrics().isEmpty()) {
            ScopeMetric firstScopeMetric = firstResourceMetric.getScopeMetrics().get(0);
            if (firstScopeMetric != null && firstScopeMetric.getMetrics() != null && !firstScopeMetric.getMetrics().isEmpty()) {
                Metric firstMetric = firstScopeMetric.getMetrics().get(0);
                if (firstMetric != null && firstMetric.getSum() != null && firstMetric.getSum().getDataPoints() != null && !firstMetric.getSum().getDataPoints().isEmpty()) {
                    SumDataPoint firstDataPoint = firstMetric.getSum().getDataPoints().get(0);
                    if (firstDataPoint != null) {
                        String startTimeUnixNanoAsString = firstDataPoint.getStartTimeUnixNano();
                        // Assuming you have a method to convert String to Long, replace convertStringToLong with your actual conversion method
                        return convertStringToLong(startTimeUnixNanoAsString);
                    }
                }
            }
        }
    }
    return null;
}

// Example conversion method, replace with your actual conversion logic
private Long convertStringToLong(String stringValue) {
    try {
        return Long.parseLong(stringValue);
    } catch (NumberFormatException e) {
        // Handle the case where conversion fails
        return null;
    }
}

   
}
