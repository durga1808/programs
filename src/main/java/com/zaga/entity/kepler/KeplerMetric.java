package com.zaga.entity.kepler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zaga.entity.otelmetric.ResourceMetric;

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
  private long startTimeUnixNano;

  
  public void setStartTimeUnixNano(long startTimeEpochMillis) {
    this.startTimeUnixNano = startTimeEpochMillis * 1_000_000;
}
 
}
