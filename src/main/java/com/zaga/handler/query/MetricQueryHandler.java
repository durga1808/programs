package com.zaga.handler.query;

import java.util.List;

import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.repo.query.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryHandler {

    @Inject
    MetricQueryRepo metricQueryRepo;

    public List<OtelMetric> getMetricProduct(OtelMetric metric) {
        return metricQueryRepo.listAll();
    }
    
}
