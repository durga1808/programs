package com.zaga.entity.otelmetric.scopeMetric;

import java.util.List;

import com.zaga.entity.otelmetric.scopeMetric.gauge.GaugeDataPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricGauge {
    private List<GaugeDataPoint> dataPoints;
    private int aggregationTemporality;
}
