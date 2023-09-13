package com.zaga.serviceImpl;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.command.LogCommandRepo;
import com.zaga.service.LogService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogServiceImpl implements LogService{
    @Inject
    LogCommandRepo logRepo;

    @Override
    public void createProduct(OtelLog logs) {
        logRepo.persist(logs);
    }
    
    
    
}
