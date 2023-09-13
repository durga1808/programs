package com.zaga.serviceImpl;

import com.zaga.entity.otelmetric.OtelMetric;
import com.zaga.repo.command.MetricCommandRepo;
import com.zaga.service.MetricService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricServiceImpl implements MetricService{

    @Inject
    MetricCommandRepo metricRepo;


    @Override
    public void createProduct(OtelMetric metric) {
        metricRepo.persist(metric);
    }
    
}
