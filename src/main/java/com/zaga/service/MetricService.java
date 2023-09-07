package com.zaga.service;

import com.zaga.entity.otelmetric.OtelMetric;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface MetricService {
    void createProduct(OtelMetric metric);
    
}
