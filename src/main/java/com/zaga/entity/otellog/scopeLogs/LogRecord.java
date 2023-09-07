package com.zaga.entity.otellog.scopeLogs;

import com.zaga.entity.otellog.scopeLogs.logRecord.Body;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {
    private String observedTimeUnixNano;
    private int severityNumber;
    private String severityText;
    private Body body;
    private int flags;
    private String traceId;
    private String spanId;
    
}
