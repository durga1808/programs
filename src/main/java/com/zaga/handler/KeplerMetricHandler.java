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

@ApplicationScoped
public class KeplerMetricHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  MongoClient mongoClient;

  public List<KeplerMetricDTO> getAllKeplerByDateAndTime(
    LocalDate from,
    LocalDate to,
    int minutesAgo,
    String type,
    List<String> keplerTypeList
  ) {
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

      LocalDateTime DBCallOneStart = LocalDateTime.now();
      
      System.out.println(
        "------------DB call One startTimestamp------ " + DBCallOneStart
      );

      Bson bsonFilter = Filters.and(
        Filters.gte(
          "date",
          Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())
        ),
        Filters.lt(
          "date",
          Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant())
        )
      );
      
      LocalDateTime DBCallOneEnd = LocalDateTime.now();

      System.out.println(
        "------------DB call One endTimestamp------ " + DBCallOneEnd
      );

      System.out.println(
        "-----------DB call ended Timestamp------ " +
        Duration.between(DBCallOneStart, DBCallOneEnd)
      );

 
      timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());

    } else {
      throw new IllegalArgumentException(
        "Either date range or minutesAgo must be provided"
      );
    }

    if (type != null && !type.isEmpty()) {
      timeFilter.append("type", type);
    }

    if (keplerTypeList != null && !keplerTypeList.isEmpty()) {
      timeFilter.append("keplerType", new Document("$in", keplerTypeList));
    }

    LocalDateTime DBCallTwoStart = LocalDateTime.now();

    System.out.println(
      "------------DB call Two startTimestamp------ " + DBCallTwoStart
    );

    PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);

    LocalDateTime DBCallTwoEnd = LocalDateTime.now();

    System.out.println(
      "------------DB call Two endTimestamp------ " + DBCallTwoEnd
    );

    System.out.println(
      "-----------DB call ended Timestamp------ " +
      Duration.between(DBCallTwoStart, DBCallTwoEnd)
    );

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
