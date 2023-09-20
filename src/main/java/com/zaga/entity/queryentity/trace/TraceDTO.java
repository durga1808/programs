package com.zaga.entity.queryentity.trace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zaga.entity.oteltrace.scopeSpans.Spans;

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
@MongoEntity(collection="TraceDto",database="OtelTrace")
public class TraceDTO extends PanacheMongoEntity{
    private String traceId;
    private String serviceName;
    private String methodName;
    private String duration;
    private String statusCode;
    private String spanCount;
    private String createdTime;
    private List<Spans> spans;

}






   







    

