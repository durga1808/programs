package com.zaga.entity.otelmetric.scopeMetric;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metric {
    private String name;
    private String description;
    private String unit;
    private MetricSum sum;
    private MetricGauge gauge;
    private MetricHistogram histogram;
    
}
