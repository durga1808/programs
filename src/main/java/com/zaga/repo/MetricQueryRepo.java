package com.zaga.repo;

import com.zaga.entity.queryentity.metric.MetricDTO;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<MetricDTO> {
    
    // public List<MetricDTO> getMetricData(int timeAgoMinutes, String serviceName) {
    //     // Calculate the start time in UTC
    //     LocalDateTime startTimeUtc = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(timeAgoMinutes);
    //     Date startDateUtc = Date.from(startTimeUtc.toInstant(ZoneOffset.UTC));
    
    //     // Query metrics with the given service name and start time
    //     PanacheQuery<MetricDTO> query = find("serviceName = ?1 and date >= ?2", serviceName, startDateUtc);
    //     List<MetricDTO> results = query.list();
    
    //     // Convert UTC dates to IST
    //     results.forEach(metricDTO -> {
    //         metricDTO.setDate(convertUtcToIst(metricDTO.getDate()));
    //     });
    
    //     return results;
    // }
    
    // private Date convertUtcToIst(Date utcDate) {
    //     Instant instant = utcDate.toInstant();
    //     ZoneId istZone = ZoneId.of("Asia/Kolkata");
    //     LocalDateTime istDateTime = LocalDateTime.ofInstant(instant, istZone);
    //     return Date.from(istDateTime.atZone(istZone).toInstant());
    // }


    public List<MetricDTO> getMetricData(LocalDate from, LocalDate to, String serviceName) {
    // Calculate the start time in UTC
    LocalDateTime startTimeUtc = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(ChronoUnit.MINUTES.between(from.atStartOfDay(), to.atStartOfDay()));
    Date startDateUtc = Date.from(startTimeUtc.toInstant(ZoneOffset.UTC));

    // Query metrics with the given service name and start time
    PanacheQuery<MetricDTO> query = find("serviceName = ?1 and date >= ?2", serviceName, startDateUtc);
    List<MetricDTO> results = query.list();

    // Convert UTC dates to IST
    results.forEach(metricDTO -> {
        metricDTO.setDate(convertUtcToIst(metricDTO.getDate()));
    });

    return results;
}

private Date convertUtcToIst(Date utcDate) {
    Instant instant = utcDate.toInstant();
    ZoneId istZone = ZoneId.of("Asia/Kolkata");
    LocalDateTime istDateTime = LocalDateTime.ofInstant(instant, istZone);
    return Date.from(istDateTime.atZone(istZone).toInstant());
}

    
    
}
