package com.zaga.entity.otelmetric;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaugeDataPoint {
    private List<GaugeDataPointAttribute> attributes;
    private String startTimeUnixNano;
    private String timeUnixNano;
    private String asInt;
}
