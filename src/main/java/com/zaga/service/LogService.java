package com.zaga.service;

import com.zaga.entity.otellog.OtelLog;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface LogService {
    
void createProduct(OtelLog logs);
}