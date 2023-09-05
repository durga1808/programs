package com.zaga.service;

import com.zaga.entity.metrics.MetricMain;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface MetricService {
    void createProduct(MetricMain metric);
    
}
