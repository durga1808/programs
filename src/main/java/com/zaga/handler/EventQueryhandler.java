package com.zaga.handler;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bson.BsonNull;
import org.bson.Document;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.otelevent.ScopeLogs;
import com.zaga.entity.otelevent.scopeLogs.LogRecords;
import com.zaga.entity.otelevent.scopeLogs.logRecord.Body;
import com.zaga.entity.queryentity.cluster_utilization.response.ClusterResponse;
import com.zaga.entity.queryentity.events.EventsDTO;
import com.zaga.repo.EventDTORepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventQueryhandler {
    
@Inject
EventDTORepo eventDTORepo;

 @Inject
  MongoClient mongoClient;


public List<EventsDTO> getAllEvent() {

    return eventDTORepo.listAll();
}


 public List<EventsDTO> getAllEventsByDateAndTime(
    LocalDate from,
    LocalDate to,
    int minutesAgo
  ) {
    LocalDateTime startTime = LocalDateTime.now();
    MongoDatabase database = mongoClient.getDatabase("OtelEvent");
    MongoCollection<Document> collection = database.getCollection(
      "EventsDTO"
    );

    List<EventsDTO> result;

    if (from != null && to != null) {
      result =
        executeAggregationPipeline(
          collection,
          from,
          to
        );
    }
     else if (from != null && minutesAgo > 0) {
      result =
        executeAnotherLogic(collection, from, minutesAgo);
    } 
    else {
      System.out.println(
        "Invalid parameters. Provide either 'from' or 'minutesAgo'."
      );
      result = Collections.emptyList();
    }

    LocalDateTime endTime = LocalDateTime.now();
    return result;
  }

public List<EventsDTO> executeAggregationPipeline(
   MongoCollection<Document> collection,
   LocalDate from,
   LocalDate to) {
        List<Document> pipeline = Arrays.asList(
            new Document("$addFields", 
                new Document("justDate", 
                    new Document("$dateToString", 
                        new Document("format", "%m-%d-%Y")
                            .append("date", "$createdTime")
                    )
                )
            ), 
            new Document("$match", 
    new Document("$and", 
        Arrays.asList(
            new Document("justDate", 
                new Document("$gte",from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                    .append("$lte",   to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
            )
        )
    )
),
new Document("$sort", new Document("createdTime", -1)),
new Document("$project", new Document("_id", 0)
            .append("nodeName", 1)
            .append("objectKind", 1)
            .append("objectName", 1)
            .append("scopeLogs", 1)
            .append("severityText", 1)
            .append("createdTime", 1)
        )
    );

    AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

    List<EventsDTO> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            EventsDTO eventsDTO = new EventsDTO();
            eventsDTO.setCreatedTime(document.getDate("createdTime"));
            eventsDTO.setNodeName(document.getString("nodeName"));
            eventsDTO.setObjectKind(document.getString("objectKind"));
            eventsDTO.setObjectName(document.getString("objectName"));
            eventsDTO.setSeverityText(document.getString("severityText"));

            // Handle scopeLogs field
            List<Document> scopeLogsDocs = document.get("scopeLogs", List.class);
            if (scopeLogsDocs != null) {
                List<ScopeLogs> scopeLogsList = new ArrayList<>();
                for (Document scopeLogDoc : scopeLogsDocs) {
                    ScopeLogs scopeLogs = new ScopeLogs();
                    scopeLogs.setScope(scopeLogDoc.get("scope", Map.class));

                    List<Document> logRecordsDocs = scopeLogDoc.get("logRecords", List.class);
                    if (logRecordsDocs != null) {
                        List<LogRecords> logRecordsList = new ArrayList<>();
                        for (Document logRecordDoc : logRecordsDocs) {
                            LogRecords logRecord = new LogRecords();
                            logRecord.setTimeUnixNano(logRecordDoc.getString("timeUnixNano"));
                            logRecord.setSeverityNumber(logRecordDoc.getInteger("severityNumber"));
                            logRecord.setSeverityText(logRecordDoc.getString("severityText"));
                            logRecord.setSpanId(logRecordDoc.getString("spanId"));
                            logRecord.setTraceId(logRecordDoc.getString("traceId"));

                            // Here's where you're getting the error, you should cast to Document
                           // Inside the mapping logic for LogRecords
                            Body body = new Body();
                            Document bodyDoc = logRecordDoc.get("body", Document.class);
                            if (bodyDoc != null) {
    // Assuming that "stringValue" is a field directly in the document
                            body.setStringValue(bodyDoc.getString("stringValue"));
                            }
                            logRecord.setBody(body);

                            logRecord.setAttributes(logRecordDoc.get("attributes", List.class));
                            logRecordsList.add(logRecord);
                        }
                        scopeLogs.setLogRecords(logRecordsList);
                    }
                    scopeLogsList.add(scopeLogs);
                }
                eventsDTO.setScopeLogs(scopeLogsList);
            }

            result.add(eventsDTO);
        }
        System.out.println("----------events data------- " + result.size());

    return result;
   }    


   private List<EventsDTO> executeAnotherLogic(
            MongoCollection<Document> collection,
            LocalDate from,
            Integer minutesAgo) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
                        new Document("$gte", Arrays.asList("$createdTime",
                                new Document("$subtract", Arrays.asList(new java.util.Date(), minutesAgo * 60L * 1000L)))),
                        new Document("$lte", Arrays.asList("$createdTime",
                                new java.util.Date()))
                )))),
                new Document("$sort",
                        new Document("date", 1L)
                ),
                new Document("$sort", new Document("createdTime", -1)),
               new Document("$project", new Document("_id", 0)
                        .append("nodeName", 1)
                        .append("objectKind", 1)
                        .append("objectName", 1)
                        .append("scopeLogs", 1)
                        .append("severityText", 1)
                        .append("createdTime", 1)
                )
        );

        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

        List<EventsDTO> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            EventsDTO eventsDTO = new EventsDTO();
            eventsDTO.setCreatedTime(document.getDate("createdTime"));
            eventsDTO.setNodeName(document.getString("nodeName"));
            eventsDTO.setObjectKind(document.getString("objectKind"));
            eventsDTO.setObjectName(document.getString("objectName"));
            eventsDTO.setSeverityText(document.getString("severityText"));

            // Handle scopeLogs field
            List<Document> scopeLogsDocs = document.get("scopeLogs", List.class);
            if (scopeLogsDocs != null) {
                List<ScopeLogs> scopeLogsList = new ArrayList<>();
                for (Document scopeLogDoc : scopeLogsDocs) {
                    ScopeLogs scopeLogs = new ScopeLogs();
                    scopeLogs.setScope(scopeLogDoc.get("scope", Map.class));

                    List<Document> logRecordsDocs = scopeLogDoc.get("logRecords", List.class);
                    if (logRecordsDocs != null) {
                        List<LogRecords> logRecordsList = new ArrayList<>();
                        for (Document logRecordDoc : logRecordsDocs) {
                            LogRecords logRecord = new LogRecords();
                            logRecord.setTimeUnixNano(logRecordDoc.getString("timeUnixNano"));
                            logRecord.setSeverityNumber(logRecordDoc.getInteger("severityNumber"));
                            logRecord.setSeverityText(logRecordDoc.getString("severityText"));
                            logRecord.setSpanId(logRecordDoc.getString("spanId"));
                            logRecord.setTraceId(logRecordDoc.getString("traceId"));

                            // Handle Body field
                            Body body = new Body();
                            Document bodyDoc = logRecordDoc.get("body", Document.class);
                            if (bodyDoc != null) {
                                body.setStringValue(bodyDoc.getString("stringValue"));
                            }
                            logRecord.setBody(body);

                            logRecord.setAttributes(logRecordDoc.get("attributes", List.class));
                            logRecordsList.add(logRecord);
                        }
                        scopeLogs.setLogRecords(logRecordsList);
                    }
                    scopeLogsList.add(scopeLogs);
                }
                eventsDTO.setScopeLogs(scopeLogsList);
            }

            result.add(eventsDTO);
        }
        System.out.println("----------events data------- " + result.size());
        return result;
       
    }   
}






