package com.zaga.handler;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;
import com.zaga.repo.KeplerMetricRepo;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;

@ApplicationScoped
public class KeplerMetricHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  MongoClient mongoClient;

  // public List<KeplerMetricDTO> getAllKeplerByDateAndTime(
  //   LocalDate from,
  //   LocalDate to,
  //   int minutesAgo,
  //   String type,
  //   List<String> keplerTypeList
  // ) {
  //   Document timeFilter;
  //   if (from != null && to != null && to.isBefore(from)) {
  //     LocalDate temp = from;
  //     from = to;
  //     to = temp;
  //   }

  //   if (from != null && to != null) {
  //     timeFilter = createCustomDateFilter(from, to);
  //   } else if (minutesAgo > 0) {
  //     LocalDateTime currentDateTime = LocalDateTime.now();
  //     LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

  //     LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
  //     if (fromDateTime.isBefore(startOfToday)) {
  //       fromDateTime = startOfToday;
  //     }
  //     LocalDateTime toDateTime = currentDateTime;

  //     LocalDateTime DBCallOneStart = LocalDateTime.now();
      
  //     System.out.println(
  //       "------------DB call One startTimestamp------ " + DBCallOneStart
  //     );

  //     Bson bsonFilter = Filters.and(
  //       Filters.gte(
  //         "date",
  //         Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())
  //       ),
  //       Filters.lt(
  //         "date",
  //         Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant())
  //       )
  //     );
      
  //     LocalDateTime DBCallOneEnd = LocalDateTime.now();

  //     System.out.println(
  //       "------------DB call One endTimestamp------ " + DBCallOneEnd
  //     );

  //     System.out.println(
  //       "-----------DB call ended Timestamp------ " +
  //       Duration.between(DBCallOneStart, DBCallOneEnd)
  //     );

 
  //     timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());

  //   } else {
  //     throw new IllegalArgumentException(
  //       "Either date range or minutesAgo must be provided"
  //     );
  //   }

  //   if (type != null && !type.isEmpty()) {
  //     timeFilter.append("type", type);
  //   }

  //   if (keplerTypeList != null && !keplerTypeList.isEmpty()) {
  //     timeFilter.append("keplerType", new Document("$in", keplerTypeList));
  //   }

  //   LocalDateTime DBCallTwoStart = LocalDateTime.now();

  //   System.out.println(
  //     "------------DB call Two startTimestamp------ " + DBCallTwoStart
  //   );

  //   PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);

  //   LocalDateTime DBCallTwoEnd = LocalDateTime.now();

  //   System.out.println(
  //     "------------DB call Two endTimestamp------ " + DBCallTwoEnd
  //   );

  //   System.out.println(
  //     "-----------DB call ended Timestamp------ " +
  //     Duration.between(DBCallTwoStart, DBCallTwoEnd)
  //   );

  //   return query.list();
  // }


  


public List<KeplerMetricDTO> getAllKeplerByDateAndTime(
    LocalDate from,
    LocalDate to,
    int minutesAgo,
    String type,
    List<String> keplerTypeList
) {
    LocalDateTime startTime = LocalDateTime.now();

    Bson bsonFilter;

    if (from != null && to != null && to.isBefore(from)) {
        LocalDate temp = from;
        from = to;
        to = temp;
    }

    if (from != null && to != null) {
        bsonFilter = Filters.and(
            Filters.gte("date", Date.from(from.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())),
            Filters.lt("date", Date.from(to.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
        );
    } else if (minutesAgo > 0) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);

        if (fromDateTime.isBefore(startOfToday)) {
            fromDateTime = startOfToday;
        }

        bsonFilter = Filters.and(
            Filters.gte("date", Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())),
            Filters.lt("date", Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        );
    } else {
        throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

    if (type != null && !type.isEmpty()) {
        bsonFilter = Filters.and(bsonFilter, Filters.eq("type", type));
    }

    if (keplerTypeList != null && !keplerTypeList.isEmpty()) {
        bsonFilter = Filters.and(bsonFilter, Filters.in("keplerType", keplerTypeList));
    }

    LocalDateTime dbCallStart = LocalDateTime.now();
    System.out.println("------------DB call startTimestamp------ " + dbCallStart);

    Document documentFilter = Document.parse(bsonFilter.toBsonDocument().toJson());

    PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(documentFilter);

    LocalDateTime dbCallEnd = LocalDateTime.now();
    System.out.println("------------DB call endTimestamp------ " + dbCallEnd);
    System.out.println("-----------DB call ended Timestamp------ " + Duration.between(dbCallStart, dbCallEnd));

    return query.list();
}

  // private Document createCustomDateFilter(LocalDate from, LocalDate to) {
  //   Bson bsonFilter = Filters.and(
  //     Filters.gte("date", from.atStartOfDay()),
  //     Filters.lt("date", to.plusDays(1).atStartOfDay())
  //   );

  //   return Document.parse(bsonFilter.toBsonDocument().toJson());
  // }
}



