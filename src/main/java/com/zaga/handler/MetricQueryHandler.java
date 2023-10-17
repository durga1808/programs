package com.zaga.handler;


import java.time.LocalDate;
import java.util.List;


import com.mongodb.client.MongoClient;
import com.zaga.entity.queryentity.metric.MetricDTO;
import com.zaga.repo.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryHandler {

    @Inject
    MetricQueryRepo metricQueryRepo;

    @Inject
    MongoClient mongoClient;

    public List<MetricDTO> getAllMetricData() {
        return metricQueryRepo.listAll();
    }

    public List<MetricDTO> getMetricData(LocalDate from,LocalDate to,String serviceName) {
       List<MetricDTO> results = metricQueryRepo.getMetricData(from,to, serviceName);
        return results;
    }


}
