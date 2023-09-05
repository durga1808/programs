package com.zaga.entity.otelmetric.histogram;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistogramDataPoint {
    private List<HistogramDataPoint> attributes;
    private String startTimeUnixNano;
    private String timeUnixNano;
    private String count;
    private double sum;
    private List<String> bucketCounts;
    private double min;
    private double max;
}
