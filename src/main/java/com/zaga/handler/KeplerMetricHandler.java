package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.entity.queryentity.kepler.KeplerMetricQuery;
import com.zaga.repo.KeplerMetricRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class KeplerMetricHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  MongoClient mongoClient;

  // public List<KeplerMetric> getAllKeplerData() {
  //   List<KeplerMetric> allKepler = keplerMetricRepo.listAll();
  //   return allKepler;
  // }

  public List<KeplerMetricQuery> getKeplerData(
    // LocalDate from,
    // LocalDate to,
    // int minutesAgo
  ) {
    MongoCollection<Document> collection = mongoClient
      .getDatabase("KeplerMetric")
      .getCollection("Metrics");
    // Bson matchStage;

    List<Bson> pipeline = Arrays.asList(
      new Document(
        "$project",
        new Document("resourceMetrics.scopeMetrics.metrics", 1L)
      ),
      new Document("$unwind", new Document("path", "$resourceMetrics")),
      new Document(
        "$unwind",
        new Document("path", "$resourceMetrics.scopeMetrics")
      ),
      new Document(
        "$unwind",
        new Document("path", "$resourceMetrics.scopeMetrics.metrics")
      ),
      new Document(
        "$match",
        new Document(
          "resourceMetrics.scopeMetrics.metrics.name",
          "kepler_container_cpu_cycles_total"
        )
      ),
      new Document(
        "$unwind",
        new Document(
          "path",
          "$resourceMetrics.scopeMetrics.metrics.sum"
        )
      )
    //   new Document(
    //     "$unwind",
    //     new Document(
    //       "path",
    //       "$resourceMetrics.scopeMetrics.metrics.sum.dataPoints.attributes"
    //     )
    //   )
    );
    AggregateIterable<Document> aggregationResult = collection.aggregate(
      pipeline
    );

    List<KeplerMetricQuery> keplerMetrics = new ArrayList<>();
    for(Document document : aggregationResult){
        System.out.println("Aggregation result:===================================== " + document.get("dataPoints").toString());
    }
    

    // Return the list of KeplerMetricQuery objects
    return keplerMetrics;
  }




  //get all kepler metric dto 
//   public List<KeplerMetricDTO> getAllKeplerByMinutes(LocalDate from, LocalDate to, int minutesAgo) {
//    return keplerMetricRepo.listAll();
// }



// public List<KeplerMetricDTO> getAllKeplerByDateAndTime(LocalDate from, LocalDate to, int minutesAgo) {
//     Document timeFilter;
//     if (from != null && to != null && to.isBefore(from)) {
//         LocalDate temp = from;
//         from = to;
//         to = temp;
//     }

//     if (from != null && to != null) {
//         timeFilter = createCustomDateFilter(from, to);
//     } else if (minutesAgo > 0) {
//         LocalDateTime currentDateTime = LocalDateTime.now();
//         LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

//         LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
//         if (fromDateTime.isBefore(startOfToday)) {
//             fromDateTime = startOfToday;
//         }
//         LocalDateTime toDateTime = currentDateTime;

//         Bson bsonFilter = Filters.and(
//                 Filters.gte("date", fromDateTime),
//                 Filters.lt("date", toDateTime)
//         );

//      timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());
//     } else {
//         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//     }

//     PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);
//     return query.list();
// }

// private Document createCustomDateFilter(LocalDate from, LocalDate to) {
//     Bson bsonFilter = Filters.and(
//             Filters.gte("date", from.atStartOfDay()),
//             Filters.lt("date", to.plusDays(1).atStartOfDay())
//     );

//    return Document.parse(bsonFilter.toBsonDocument().toJson());
// }


// public List<KeplerMetricDTO> getAllKeplerByDateAndTime(LocalDate from, LocalDate to, int minutesAgo) {
//  Document timeFilter;
//     if (from != null && to != null && to.isBefore(from)) {
//         LocalDate temp = from;
//         from = to;
//         to = temp;
//     }

//     if (from != null && to != null) {
//         timeFilter = createCustomDateFilter(from, to);
//     } else if (minutesAgo > 0) {
//         LocalDateTime currentDateTime = LocalDateTime.now();
//         LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

//         LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
//         if (fromDateTime.isBefore(startOfToday)) {
//             fromDateTime = startOfToday;
//         }
//         LocalDateTime toDateTime = currentDateTime;

//         Bson bsonFilter = Filters.and(
//                 Filters.gte("date", Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())),
//                 Filters.lt("date", Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant()))
//         );

//         timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());
//     } else {
//         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//     }

//     PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);
//     return query.list();
// }

// private Document createCustomDateFilter(LocalDate from, LocalDate to) {
//     Bson bsonFilter = Filters.and(
//             Filters.gte("date", from.atStartOfDay()),
//             Filters.lt("date", to.plusDays(1).atStartOfDay())
//     );

//     return Document.parse(bsonFilter.toBsonDocument().toJson());
// }


public List<KeplerMetricDTO> getAllKeplerByDateAndTime(LocalDate from, LocalDate to, int minutesAgo, String type) {
 Document timeFilter;
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

        Bson bsonFilter = Filters.and(
                Filters.gte("date", Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())),
                Filters.lt("date", Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        );

        timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());
    } else {
        throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

  if (type != null && !type.isEmpty()) {
      timeFilter.append("type", type);
  }

  PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);
  return query.list();
}

private Document createCustomDateFilter(LocalDate from, LocalDate to) {
  Bson bsonFilter = Filters.and(
          Filters.gte("date", from.atStartOfDay()),
          Filters.lt("date", to.plusDays(1).atStartOfDay())
  );

  return Document.parse(bsonFilter.toBsonDocument().toJson());
}



}
