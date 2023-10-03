package com.zaga.entity.queryentity.log;

import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
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
    private Date createdTime;
    private List<ScopeLogs> scopeLogs;
  }
