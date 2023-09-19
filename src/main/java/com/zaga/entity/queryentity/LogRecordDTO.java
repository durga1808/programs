package com.zaga.entity.queryentity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
public class LogRecordDTO {
    private String body;
    private String observedTimeUnixNano;
    private String severityText;
    private String spanId;
    private String timeUnixNano;
    private String traceId;
}
