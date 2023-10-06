package com.zaga.handler;


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

    public List<MetricDTO> getMetricData(int timeAgoMinutes, String serviceName) {
       List<MetricDTO> results = metricQueryRepo.getMetricData(timeAgoMinutes, serviceName);
        return results;
    }


}
