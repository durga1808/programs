package com.zaga.handler.query;

import java.util.List;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.repo.query.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;

    public List<OtelLog> getLogProduct(OtelLog logs) {
        return logQueryRepo.listAll();
    }
    
}

