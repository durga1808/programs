package com.zaga.kafka.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;


import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.service.MetricService;

import jakarta.inject.Inject;

public class MetricConsumerService {
    
    @Inject
    MetricService metricService;
    
    @Incoming("metric-in")
    public void consumeProductDetails(OtelMetric metrics) {
        System.out.println("consumer++++++++++++++"+metrics);
       metricService.createProduct(metrics);
    }
}
