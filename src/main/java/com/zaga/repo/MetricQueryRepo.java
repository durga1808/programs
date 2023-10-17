package com.zaga.repo;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.zaga.entity.queryentity.metric.MetricDTO;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

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

    @Inject
    MongoClient mongoClient;

    // public List<MetricDTO> getMetricData(LocalDate from, LocalDate to, String serviceName) {
    //     // Calculate the start time in UTC and end time in UTC
    //     Instant fromUtc = from.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    //     Instant toUtc = to.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

    //     // Create the aggregation pipeline stages
    //     Document addFieldsStage = Document.parse("{$addFields: { justDate: { $dateToString: { format: '%m-%d-%Y', date: '$date' } } }}");

    //     Bson matchStage = Filters.and(
    //         Filters.eq("serviceName", serviceName),
    //         Filters.gte("justDate", from.toString()), // Convert LocalDate to string
    //         Filters.lte("justDate", to.toString())     // Convert LocalDate to string
    //     );

    //     // Build the aggregation pipeline
    //     List<Bson> pipeline = List.of(addFieldsStage, matchStage);

    //     // Get the MongoDB collection for your entity
    //     MongoCollection<MetricDTO> collection = mongoClient
    //         .getDatabase("YourDatabaseName")
    //         .getCollection("YourCollectionName", MetricDTO.class);

    //     // Execute the aggregation pipeline
    //     AggregateIterable<MetricDTO> aggregationResult = collection.aggregate(pipeline);

    //     List<MetricDTO> results = new ArrayList<>();

    //     try (MongoCursor<MetricDTO> cursor = aggregationResult.iterator()) {
    //         while (cursor.hasNext()) {
    //             results.add(cursor.next());
    //         }
    //     }

    //     // Convert UTC dates to IST
    //     results.forEach(metricDTO -> metricDTO.setDate(convertUtcToIst(metricDTO.getDate())));

    //     return results;
    // }
    public List<MetricDTO> findDataByDateRange(LocalDate from, LocalDate to, String serviceName) {
        // Calculate the start time in UTC and end time in UTC
        Instant fromUtc = from.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Instant toUtc = to.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

        // Convert to Date
        Date fromDate = Date.from(fromUtc);
        Date toDate = Date.from(toUtc);

        // Perform the date range query
        return list("serviceName = ?1 and date >= ?2 and date <= ?3", serviceName, fromDate, toDate);
    }

    // private Date convertUtcToIst(Date utcDate) {
    //     Instant instant = utcDate.toInstant();
    //     ZoneId istZone = ZoneId.of("Asia/Kolkata");
    //     LocalDateTime istDateTime = LocalDateTime.ofInstant(instant, istZone);
    //     return Date.from(istDateTime.atZone(istZone).toInstant());
    // }
}
