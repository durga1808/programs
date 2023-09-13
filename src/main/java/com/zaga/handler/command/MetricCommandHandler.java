package com.zaga.handler.command;

import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.repo.command.MetricCommandRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricCommandHandler {

    @Inject
    MetricCommandRepo metricCommandRepo;

    public void createMetricProduct(OtelMetric metric) {
        metricCommandRepo.persist(metric);
    }
    
}
