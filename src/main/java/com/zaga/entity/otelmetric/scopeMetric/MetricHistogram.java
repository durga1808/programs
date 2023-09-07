package com.zaga.entity.otelmetric.scopeMetric;

import java.util.List;

import com.zaga.entity.otelmetric.scopeMetric.histogram.HistogramDataPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricHistogram {
    private List<HistogramDataPoint> dataPoints;
    private int aggregationTemporality;
}
