package com.zaga.entity.queryentity.kepler;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties("id")
@MongoEntity(collection = "KeplerMetricDTO", database = "KeplerMetric")
public class KeplerMetricDTO {
    private Date date;
    private Double powerConsumption;
    private String serviceName;
}
