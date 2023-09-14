package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;


import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.handler.command.MetricCommandHandler;

import jakarta.inject.Inject;

public class MetricConsumerService {
    
    @Inject
    MetricCommandHandler metricCommandHandler;
    
    // @Incoming("metric-in")
    public void consumeMetricDetails(OtelMetric metrics) {
        System.out.println("consumer++++++++++++++"+metrics);
       metricCommandHandler.createMetricProduct(metrics);
    }
}
