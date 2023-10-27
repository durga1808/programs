package com.zaga.entity.queryentity.trace;

import java.util.List;

import com.zaga.entity.otellog.scopeLogs.logRecord.Body;
import com.zaga.entity.otellog.scopeLogs.logRecord.LogAttribute;
import com.zaga.entity.oteltrace.scopeSpans.Spans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpanDTO {
    private Spans spans;
    private boolean errorStatus;
    private Body errorMessage;
    private String LogTraceId;
    private String LogSpanId;
    private List<LogAttribute> logAttributes; 
}
