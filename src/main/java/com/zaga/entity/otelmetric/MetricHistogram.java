package com.zaga.entity.otelmetric;

import java.util.List;
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
