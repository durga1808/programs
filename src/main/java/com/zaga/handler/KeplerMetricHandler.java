package com.zaga.handler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.trace.KafkaMetrics;
import com.zaga.repo.KeplerMetricRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class KeplerMetricHandler {

    @Inject 
    KeplerMetricRepo keplerMetricRepo;

     @Inject
    MongoClient mongoClient;

public List<KeplerMetric> getAllKeplerData() {
        List<KeplerMetric> allKepler = keplerMetricRepo.listAll();
        return allKepler;
    }

//  public List<KeplerMetric> getKeplerData(LocalDate from, LocalDate to, int minutesAgo) {
//         Bson timeFilter;
    
//         // Rearrange 'from' and 'to' if 'to' is earlier than 'from'
//         if (from != null && to != null && to.isBefore(from)) {
//             LocalDate temp = from;
//             from = to;
//             to = temp;
//         }
    
//         if (from != null && to != null) {
//             timeFilter = createCustomDateFilter(from, to);
//         } else if (minutesAgo > 0) {
//             LocalDateTime currentDateTime = LocalDateTime.now();
//             LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        
//             // Calculate 'fromDateTime' based on 'minutesAgo' but ensure it doesn't go beyond the start of today
//             LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
//             if (fromDateTime.isBefore(startOfToday)) {
//                 fromDateTime = startOfToday;
//             }
        
//             // Set 'toDateTime' to the current time
//             LocalDateTime toDateTime = currentDateTime;
        
//             timeFilter = Filters.and(
//                 Filters.gte("date", fromDateTime),
//                 Filters.lt("date", toDateTime)
//             );
//         }
//          else {
//             // Handle the case when neither date range nor minutesAgo is provided
//             throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//         }
    
//         Bson finalFilter = Filters.and(timeFilter);
    
//         MongoCollection<Document> collection = mongoClient
//             .getDatabase("KeplerMetric")
//             .getCollection("Metrics");
    
//         List<KeplerMetric> filteredResults = new ArrayList<>();
    
//         try (MongoCursor<Document> cursor = collection.find(finalFilter).iterator()) {
//             while (cursor.hasNext()) {
//                 Document document = cursor.next();
//                 KeplerMetric keplerMetric = convertDocumentToMetricDTO(document);
//                 filteredResults.add(keplerMetric);
//             }
//         }
    
//         return filteredResults;
//     }
    


// private KeplerMetric convertDocumentToMetricDTO(Document document) {
//     return null;
// }

// private Bson createCustomDateFilter(LocalDate from, LocalDate to) {
//     return Filters.and(
//             Filters.gte("date", from.atStartOfDay()),
//             Filters.lt("date", to.plusDays(1).atStartOfDay())
//     );
//   }

 public List<KeplerMetric> getKeplerData(LocalDate from, LocalDate to, int minutesAgo) {
        Bson timeFilter;

        if (from != null && to != null && to.isBefore(from)) {
            LocalDate temp = from;
            from = to;
            to = temp;
        }

        if (from != null && to != null) {
            timeFilter = createCustomDateFilter(from, to);
        } else if (minutesAgo > 0) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

            LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
            if (fromDateTime.isBefore(startOfToday)) {
                fromDateTime = startOfToday;
            }

            LocalDateTime toDateTime = currentDateTime;

            timeFilter = Filters.and(
                Filters.gte("date", fromDateTime),
                Filters.lt("date", toDateTime)
            );
        } else {
            throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
        }

        Bson finalFilter = Filters.and(timeFilter);

        MongoCollection<Document> collection = mongoClient
            .getDatabase("KeplerMetric")
            .getCollection("Metrics");

        List<KeplerMetric> filteredResults = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find(finalFilter).iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                KeplerMetric keplerMetric = convertDocumentToKeplerMetric(document);
                filteredResults.add(keplerMetric);
            }
        }

        return filteredResults;
    }

    private KeplerMetric convertDocumentToKeplerMetric(Document document) {
        KeplerMetric keplerMetric = new KeplerMetric();
        keplerMetric.setStartTimeUnixNano(document.getDate("startTimeUnixNano").toInstant().toEpochMilli() * 1_000_000);
        return keplerMetric;
    }
    
    

    private Bson createCustomDateFilter(LocalDate from, LocalDate to) {
        return Filters.and(
            Filters.gte("date", from.atStartOfDay()),
            Filters.lt("date", to.plusDays(1).atStartOfDay())
        );
    }
}
