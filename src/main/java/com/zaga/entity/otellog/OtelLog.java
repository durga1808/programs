package com.zaga.entity.otellog;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
@MongoEntity(collection = "Logs", database = "OtelLog")
public class OtelLog extends PanacheMongoEntity{
    private List<ResourceLogs> resourceLogs;
}