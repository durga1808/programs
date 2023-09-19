package com.zaga.entity.queryentity.trace;

import java.util.List;
import java.util.Map;

import com.zaga.entity.oteltrace.resource.Attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Spans {
    private List<Attribute> attributes;
    private String endTimeUnixNano;
    private int kind;
    private String name;
    private String parentSpanId;
    private String spanId;
    private String startTimeUnixNano;
    private Map<String, Object> status;
    // private String traceId;
}
