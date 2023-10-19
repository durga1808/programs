package com.zaga.repo;

import com.mongodb.client.MongoClient;
import com.zaga.entity.queryentity.metric.MetricDTO;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<MetricDTO> {
    
 

    @Inject
    MongoClient mongoClient;

  
    public List<MetricDTO> findDataByDateRange(LocalDate from, LocalDate to, String serviceName) {
        Instant fromUtc = from.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Instant toUtc = to.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

        Date fromDate = Date.from(fromUtc);
        Date toDate = Date.from(toUtc);

        return list("serviceName = ?1 and date >= ?2 and date <= ?3", serviceName, fromDate, toDate);
    }

   
}
