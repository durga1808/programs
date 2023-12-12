package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.queryentity.kepler.Response.ContainerPowerMetrics;
import com.zaga.entity.queryentity.kepler.Response.KeplerResponseData;
import com.zaga.repo.KeplerMetricRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.Document;

@ApplicationScoped
public class KeplerMetricHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  MongoClient mongoClient;

  public List<KeplerResponseData> getAllKeplerByDateAndTime(
    LocalDate from,
    LocalDate to,
    int minutesAgo,
    String type,
    List<String> keplerTypeList,
    int page,
    int pageSize
  ) {
    LocalDateTime startTime = LocalDateTime.now();
    System.out.println("------------DB call startTimestamp------ " + startTime);

    MongoDatabase database = mongoClient.getDatabase("KeplerMetric");
    MongoCollection<Document> collection = database.getCollection(
      "KeplerMetricDTO"
    );

    List<KeplerResponseData> result;

    if (from != null && to != null) {
      result =
        executeAggregationPipeline(
          collection,
          from,
          to,
          type,
          keplerTypeList,
          page,
          pageSize
        );
    } else if (from != null && minutesAgo > 0) {
      result =
        executeAnotherLogic(collection, from, minutesAgo, type, keplerTypeList, page, pageSize);
    } else {
      System.out.println(
        "Invalid parameters. Provide either 'from' or 'minutesAgo'."
      );
      result = Collections.emptyList();
    }

    LocalDateTime endTime = LocalDateTime.now();
    System.out.println("------------DB call endTimestamp------ " + endTime);
    System.out.println(
      "-----------DB call ended Timestamp------ " +
      Duration.between(startTime, endTime)
    );

    return result;
  }

  public List<KeplerResponseData> executeAggregationPipeline(
    MongoCollection<Document> collection,
    LocalDate from,
    LocalDate to,
    String type,
    List<String> keplerTypeList,
    int page,
    int pageSize
  ) {
    int skip = (page - 1) * pageSize;

    List<Document> pipeline = Arrays.asList(
      new Document(
        "$addFields",
        new Document(
          "justDate",
          new Document(
            "$dateToString",
            new Document("format", "%m-%d-%Y").append("date", "$date")
          )
        )
      ),
      new Document(
        "$match",
        new Document(
          "$and",
          Arrays.asList(
            new Document(
              "justDate",
              new Document(
                "$gte",
                from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
              )
                .append(
                  "$lte",
                  to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                )
            ),
            new Document("$or", Arrays.asList(new Document("type", type))),
            new Document("powerConsumption", new Document("$gt", 0L)),
            new Document("keplerType", new Document("$in", keplerTypeList))
          )
        )
      ),
      new Document(
        "$group",
        new Document("_id", "$serviceName")
          .append(
            "matchedDocuments",
            new Document(
              "$push",
              new Document("powerConsumption", "$powerConsumption")
                .append("date", "$date")
            )
          )
          .append("totalCount", new Document("$sum", 1L))
      ),
      new Document(
        "$facet",
        new Document(
          "paginatedResults",
          Arrays.asList(
            new Document("$unwind", "$matchedDocuments"),
            new Document("$sort", new Document("_id", 1L)),
            new Document(
              "$group",
              new Document("_id", "$_id")
                .append("serviceName", new Document("$first", "$_id"))
                .append(
                  "matchedDocuments",
                  new Document("$push", "$matchedDocuments")
                )
                .append("totalCount", new Document("$first", "$totalCount"))
            ),
            new Document(
              "$project",
              new Document("serviceName", 1L)
                .append(
                  "matchedDocuments",
                  new Document(
                    "$slice",
                    Arrays.asList("$matchedDocuments", skip, pageSize)
                  )
                )
                .append("totalCount", 1L)
            )
          )
        )
      )
    );

    AggregateIterable<Document> aggregationResult = collection.aggregate(
      pipeline
    );

    List<KeplerResponseData> result = new ArrayList<>();
    for (Document document : aggregationResult) {
      List<Document> paginatedResults = document.getList(
        "paginatedResults",
        Document.class
      );
      for (Document paginatedDocument : paginatedResults) {
        result.add(fromDocument(paginatedDocument));
      }
    }
    return result;
  }

  private List<KeplerResponseData> executeAnotherLogic(
    MongoCollection<Document> collection,
    LocalDate from,
    Integer minutesAgo,
    String type,
    List<String> keplerTypeList,
    Integer page,
    Integer pageSize
  ) {
        int skip = (page-1)* pageSize;
    List<Document> pipeline = Arrays.asList(new Document("$match", 
    new Document("$and", Arrays.asList(new Document("$or", Arrays.asList(new Document("type", type))), 
                new Document("keplerType", 
                new Document("$in", keplerTypeList)), 
                new Document("powerConsumption", 
                new Document("$gt", 0L)), 
                new Document("$expr", 
                new Document("$and", Arrays.asList(new Document("$gte", Arrays.asList("$date", 
                                    new Document("$subtract", Arrays.asList(new java.util.Date(), minutesAgo * 60L * 1000L)))), 
                            new Document("$lte", Arrays.asList("$date", 
                                    new java.util.Date())))))))), 
    new Document("$group", 
    new Document("_id", "$serviceName")
            .append("matchedDocuments", 
    new Document("$push", 
    new Document("powerConsumption", "$powerConsumption")
                    .append("date", "$date")))
            .append("totalCount", 
    new Document("$sum", 1L))), 
    new Document("$facet", 
    new Document("paginatedResults", Arrays.asList(new Document("$unwind", "$matchedDocuments"), 
                new Document("$sort", 
                new Document("_id", 1L)), 
                new Document("$group", 
                new Document("_id", "$_id")
                        .append("serviceName", 
                new Document("$first", "$_id"))
                        .append("matchedDocuments", 
                new Document("$push", "$matchedDocuments"))
                        .append("totalCount", 
                new Document("$first", "$totalCount"))), 
                new Document("$project", 
                new Document("serviceName", 1L)
                        .append("matchedDocuments", 
                new Document("$slice", Arrays.asList("$matchedDocuments", skip,pageSize)))
                        .append("totalCount", 1L))))));
                        
    AggregateIterable<Document> aggregationResult = collection.aggregate(
      pipeline
    );

    List<KeplerResponseData> result = new ArrayList<>();
    for (Document document : aggregationResult) {
      List<Document> paginatedResults = document.getList(
        "paginatedResults",
        Document.class
      );
      for (Document paginatedDocument : paginatedResults) {
        result.add(fromDocument(paginatedDocument));
      }
    }    System.out.println("Final Size of DTO List: " + result.size());

    return result;
  }

  public static KeplerResponseData fromDocument(Document document) {
        KeplerResponseData keplerResponse = new KeplerResponseData();
        
        // Assuming "serviceName" corresponds to "displayName"
        keplerResponse.setDisplayName(document.getString("serviceName"));
    
        // Extracting the totalCount value
        Long totalCountLong = document.getLong("totalCount");
        int totalCount = totalCountLong.intValue(); // or simply int totalCount = totalCountLong;
        keplerResponse.setTotalCount(totalCount); 
    
        List<Document> matchedDocuments = document.getList("matchedDocuments", Document.class);
        List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();
    
        for (Document matchedDocument : matchedDocuments) {
            ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics();
            containerPowerMetrics.setConsumptionValue(matchedDocument.getDouble("powerConsumption"));
            containerPowerMetrics.setCreatedTime(matchedDocument.getDate("date"));
            containerPowerMetricsList.add(containerPowerMetrics);
        }
    
        keplerResponse.setContainerPowerMetrics(containerPowerMetricsList);
        return keplerResponse;
    }
    }
