package com.zaga.serviceImpl;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.LogRepo;
import com.zaga.service.LogService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogServiceImpl implements LogService{
    @Inject
    LogRepo logRepo;

    @Override
    public void createProduct(OtelLog logs) {
        logRepo.persist(logs);
    }
    
    
}
