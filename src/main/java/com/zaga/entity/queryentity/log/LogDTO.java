package com.zaga.entity.queryentity.log;

import java.util.List;

import com.zaga.entity.otellog.ScopeLogs;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "LogDTO", database = "OtelLog")
public class LogDTO {
    private String serviceName;
    private String traceId;
    private List<ScopeLogs> scopeLogs;
}
