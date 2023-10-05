package com.zaga.handler;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.otellog.scopeLogs.LogRecord;
import com.zaga.entity.otellog.scopeLogs.Scope;
import com.zaga.entity.otellog.scopeLogs.logRecord.Body;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogMetrics;
import com.zaga.entity.queryentity.log.LogQuery;
import com.zaga.repo.LogQueryRepo;



import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;

    @Inject
    MongoClient mongoClient;
    
    
    public List<LogDTO> getLogsByServiceName(String serviceName, int page, int pageSize) {
        return logQueryRepo.findByServiceName(serviceName, page, pageSize);
    }

    public long getTotalLogCountByServiceName(String serviceName) {
        return logQueryRepo.countByServiceName(serviceName);
    }
    
    public List<LogDTO> findLogDataPaged(int page, int pageSize) {
        List<LogDTO> logList = logQueryRepo.listAll();
        // Perform any sorting or filtering if needed.
    
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, logList.size());
    
        return logList.subList(startIndex, endIndex);
    }
    
    public long countLogRecords() {
        System.out.println(
          "LogQueryHandler.countLogRecords()" + logQueryRepo.count()
        );
        return logQueryRepo.count();
    }



    
 // public List<LogDTO> searchLogsPaged(LogQuery query, int page, int pageSize,
    // int minutesAgo) {
    // FindIterable<Document> result = getFilteredLogResults(query, page, pageSize,
    // minutesAgo);
    // List<LogDTO> logDTOList = new ArrayList<>();

    // try (MongoCursor<Document> cursor = result.iterator()) {
    // while (cursor.hasNext()) {
    // Document document = cursor.next();
    // LogDTO logDTO = new LogDTO();

    // logDTO.setServiceName(document.getString("serviceName"));
    // logDTO.setTraceId(document.getString("traceId"));

    // // Assuming you have a method to fetch scope logs based on traceId
    // List<ScopeLogs> scopeLogs = fetchScopeLogsByTraceId(logDTO.getTraceId());
    // logDTO.setScopeLogs(scopeLogs);

    // logDTOList.add(logDTO);
    // }
    // }

    // return logDTOList;
    // }

    // private List<ScopeLogs> fetchScopeLogsByTraceId(String traceId) {
    // // Implement logic to fetch scope logs by traceId
    // // Return the actual scope logs or an empty list if not found
    // return new ArrayList<>();
    // }

    // private FindIterable<Document> getFilteredLogResults(LogQuery query, int
    // page, int pageSize, int minutesAgo) {
    // // Construct a query based on LogQuery's serviceName and severityText fields
    // Document filter = new Document();

    // if (query.getServiceName() != null) {
    // filter.append("serviceName", query.getServiceName());
    // }

    // if (query.getSeverityText() != null) {
    // filter.append("severityText", query.getSeverityText());
    // }

    // // Apply additional filters based on page, pageSize, and minutesAgo

    // // Use your MongoDB driver to apply the filter and return the results
    // // Example: collection.find(filter).skip((page - 1) *
    // pageSize).limit(pageSize).sort(Sorts.descending("timestamp"))
    // return LogDTO .find(filter).skip((page - 1) * pageSize).limit(pageSize);
    // }

    // public long countQueryLogs(LogQuery query, int minutesAgo) {
    // FindIterable<Document> result = getFilteredLogResults(query, 0,
    // Integer.MAX_VALUE, minutesAgo);
    // long totalCount = result.into(new ArrayList<>()).size();
    // return totalCount;
    // }

    // public List<LogDTO> searchLogsPaged(LogQuery query, int page, int pageSize,
    // int minutesAgo) {
    // try {
    // FindIterable<Document> result = getFilteredLogResults(query, page, pageSize,
    // minutesAgo);
    // List<LogDTO> logDTOList = new ArrayList<>();

    // try (MongoCursor<Document> cursor = result.iterator()) {
    // while (cursor.hasNext()) {
    // Document document = cursor.next();
    // LogDTO logDTO = new LogDTO();

    // logDTO.setServiceName(document.getString("serviceName"));
    // logDTO.setSeverityText(document.getString("severityText"));

    // // Assuming you have a method to fetch scope logs based on traceId
    // List<ScopeLogs> scopeLogs = fetchScopeLogsByTraceId(logDTO.getTraceId());
    // logDTO.setScopeLogs(scopeLogs);

    // logDTOList.add(logDTO);
    // }
    // }

    // return logDTOList;
    // } catch (Exception e) {
    // // Log the exception and handle it appropriately
    // e.printStackTrace(); // Replace this with your actual logging mechanism
    // throw new InternalServerErrorException("An error occurred: " +
    // e.getMessage());
    // }
    // }

    // private List<ScopeLogs> fetchScopeLogsByTraceId(String traceId) {
    // return null;
    // }

    // public long countQueryLogs(LogQuery logQuery, int minutesAgo) {
    // return 0;
    // }

    // getLogs by multiple queries like serviceName and severityText from LogDTO
    // entity



// search log in a filter query
    public List<LogDTO> searchlogPaged(LogQuery logQuery,int page, int pageSize,int minutesAgo) {


        List<String> serviceNames = logQuery.getServiceName();
        List<String> severityTexts = logQuery.getSeverityText();
    
        List<LogDTO> logList = logQueryRepo.listAll(); // Replace with your data source retrieval logic
    
        List<LogDTO> filteredLogList = new ArrayList<>();
    
        for (LogDTO logDTO : logList) {
            if ((serviceNames == null || serviceNames.contains(logDTO.getServiceName())) &&
                (severityTexts == null || severityTexts.contains(logDTO.getSeverityText()))) {
                filteredLogList.add(logDTO);
            }
        }
    
        return filteredLogList;
    }

    
    
  //sort orer decending 
  public List<LogDTO> getAllLogssOrderByCreatedTimeDesc() {
    return logQueryRepo.findAllOrderByCreatedTimeDesc();
  }


//sort order ascending
public List<LogDTO> getAllLogssAsc() {
    return logQueryRepo.findAllOrderByCreatedTimeAsc();
}

//sort order error data decending
// public List<LogDTO> getAllErrorLogsOrderBySeverityAndCreatedTimeDesc() {
//     MongoDatabase database = mongoClient.getDatabase("OtelLog"); 
//     MongoCollection<LogDTO> logDTOCollection = database.getCollection("LogDTO", LogDTO.class);

//     Bson matchStage = Aggregates.match(Filters.elemMatch("scopeLogs.logRecords", Filters.eq("severityText", "ERROR")));
//     Bson sortStage = Aggregates.sort(Sorts.orderBy(Sorts.descending("severityText"), Sorts.descending("createdTime")));

//     List<LogDTO> result = logDTOCollection.aggregate(List.of(matchStage, sortStage))
//             .into(new ArrayList<>());

//     return result;
// }

public List<LogDTO> getAllErrorLogsOrderBySeverityAndCreatedTimeDesc() {
    MongoDatabase database = mongoClient.getDatabase("OtelLog");
    MongoCollection<LogDTO> logDTOCollection = database.getCollection("LogDTO", LogDTO.class);

    Bson addSortFieldStage = Aggregates.addFields(new Field<>("customSortField", new Document("$cond",
            Arrays.asList(
                    new Document("$eq", Arrays.asList("$severityText", "ERROR")),
                    0,
                    1
            )
    )));

    Bson sortStage = Aggregates.sort(Sorts.orderBy(
            Sorts.ascending("customSortField"),
            Sorts.descending("createdTime")
    ));

    Bson projectStage = Aggregates.project(Projections.exclude("customSortField"));

    List<LogDTO> result = logDTOCollection.aggregate(Arrays.asList(addSortFieldStage, sortStage, projectStage))
            .into(new ArrayList<>());

    return result;
}






public List<LogMetrics> getLogMetricCount(int timeAgoMinutes) {
    List<LogDTO> logList = logQueryRepo.listAll(); // Replace with your data source retrieval logic
    Map<String, LogMetrics> metricsMap = new HashMap<>();

    Instant cutoffTime = Instant.now().minus(timeAgoMinutes, ChronoUnit.MINUTES);

    for (LogDTO logDTO : logList) {
        Date logCreateTime = logDTO.getCreatedTime();
        if (logCreateTime != null) {
            Instant logInstant = logCreateTime.toInstant();

            if (!logInstant.isBefore(cutoffTime)) {
                String serviceName = logDTO.getServiceName();

                LogMetrics metrics = metricsMap.get(serviceName);
                if (metrics == null) {
                    metrics = new LogMetrics();
                    metrics.setServiceName(serviceName);
                    metrics.setErrorCallCount(0L); // Initialize errorCallCount to 0
                    metrics.setWarnCallCount(0L);
                    metrics.setDebugCallCount(0L);
                }

                // Calculate the call counts based on the severityText
                calculateCallCounts(logDTO, metrics);

                metricsMap.put(serviceName, metrics);
            }
        }
    }

    return new ArrayList<>(metricsMap.values());
}

private void calculateCallCounts(LogDTO logDTO, LogMetrics metrics) {
    for (ScopeLogs scopeLogs : logDTO.getScopeLogs()) {
        for (LogRecord logRecord : scopeLogs.getLogRecords()) {
            String severityText = logDTO.getSeverityText(); // Get severityText from LogDTO
            if ("ERROR".equals(severityText)) {
                metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
            } else if ("WARN".equals(severityText)) {
                metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
            } else if ("DEBUG".equals(severityText)) {
                metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
            }
        }
    }
}


public List<LogDTO> findByMatching(String serviceName) {
    LocalDateTime currentTime = LocalDateTime.now();
    LocalDateTime startTime = currentTime.minusHours(55182);

    Instant currentInstant = currentTime.atZone(ZoneId.systemDefault()).toInstant();
    Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();

    Date currentDate = Date.from(currentInstant);
    Date startDate = Date.from(startInstant);

    List<LogDTO> logList = logQueryRepo.findByServiceNameAndCreatedTime(serviceName, startDate, currentDate);

    List<LogDTO> filteredLogList = new ArrayList<>();
    for (LogDTO logDTO : logList) {
        List<ScopeLogs> scopeLogsList = logDTO.getScopeLogs();
        boolean hasError = false;

        for (ScopeLogs scopeLogs : scopeLogsList) {
            for (LogRecord logRecord : scopeLogs.getLogRecords()) {
                if ("ERROR".equals(logRecord.getSeverityText())) {
                    hasError = true;
                    break;
                }
            }

            if (hasError) {
                filteredLogList.add(logDTO);
                break;
            }
        }
    }

    return filteredLogList;
}




  // search functionality
  public List<LogDTO> searchLogs(String keyword) {
    List<LogDTO> results = new ArrayList<>();
    String regexPattern = ".*" + Pattern.quote(keyword) + ".*";
    BsonRegularExpression regex = new BsonRegularExpression(regexPattern, "i");

    try {
        MongoCollection<Document> collection = mongoClient
                .getDatabase("OtelLog")
                .getCollection("LogDTO");

        Document query = new Document("$or", List.of(
                // new Document("serviceName", regex),
                // new Document("traceId", regex),
                // new Document("spanId", regex),
                // new Document("severityText", regex),
                // new Document("scopeLogs", regex)
                new Document("scopeLogs.logRecords.body.stringValue", regex)
                ));

        MongoCursor<Document> cursor = collection.find(query).iterator();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            LogDTO logResult = mapDocumentToLogDTO(document);
            results.add((LogDTO) logResult);
        }
    } catch (Exception e) {
    }

    return results;
}

private LogDTO mapDocumentToLogDTO(Document document) {
    // LogDTO logDto = DocumentMapper.mapDocumentToDto(document,LogDTO.class);
    // System.out.println("------fasfSF----" + logDto.getServiceName());

    LogDTO logDTO = new LogDTO();
    Gson gson = new Gson();
    String data = gson.toJson(document);
    // System.out.println("---data----  " + data);

    JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
    JsonArray jsonArray = jsonObject.getAsJsonArray("scopeLogs");
    // System.out.println("----scope---- " + jsonArray);

    logDTO.setServiceName(jsonObject.get("serviceName").getAsString());
    logDTO.setTraceId(jsonObject.get("traceId").getAsString());
    logDTO.setSpanId(jsonObject.get("spanId").getAsString());
    logDTO.setCreatedTime(document.getDate("createdTime"));
    logDTO.setSeverityText(jsonObject.get("severityText").getAsString());

    Scope scope = new Scope();
    List<LogRecord> logRecords = new ArrayList<LogRecord>();

    //scope logs
    for(int i = 0 ; i < jsonArray.size() ; i++){
        JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
        //scope name 
        scope.setName(jsonObject2.getAsJsonObject("scope").get("name").getAsString());

        //log records 
        JsonArray jsonArray2 = jsonObject2.getAsJsonArray("logRecords");
        
        for(int j = 0 ; j < jsonArray2.size() ; j++){
            LogRecord logRecord = new LogRecord();
            Body body = new Body();
            JsonObject jsonObject3 = jsonArray2.get(j).getAsJsonObject();
            System.out.println("-------" + jsonObject3.get("timeUnixNano").getAsString());
            logRecord.setTimeUnixNano(jsonObject3.get("timeUnixNano").getAsString());
            logRecord.setObservedTimeUnixNano(jsonObject3.get("observedTimeUnixNano").getAsString());
            logRecord.setSeverityNumber(jsonObject3.get("severityNumber").getAsInt());
            logRecord.setSeverityText(jsonObject3.get("severityText").getAsString());
            logRecord.setFlags(jsonObject3.get("flags").getAsInt());
            logRecord.setTraceId(jsonObject3.get("traceId").getAsString());
            logRecord.setSpanId(jsonObject3.get("spanId").getAsString());
            body.setStringValue(jsonObject3.getAsJsonObject("body").get("stringValue").getAsString());
            logRecord.setBody(body);               
            logRecords.add(logRecord);
        }

        System.out.println("----scope name ---- " + jsonObject2.getAsJsonObject("scope").get("name").getAsString());
    }
    
    ScopeLogs scopeLogs = new ScopeLogs();
    scopeLogs.setScope(scope);
    scopeLogs.setLogRecords(logRecords);
    List<ScopeLogs> scopeLogsArray = new ArrayList<ScopeLogs>();
    scopeLogsArray.add(scopeLogs);
    logDTO.setScopeLogs(scopeLogsArray);
    // logDTO.setServiceName(document.getString("serviceName"));
    // logDTO.setTraceId(document.getString("traceId"));
    // logDTO.setSpanId(document.getString("spanId"));
    // logDTO.setCreatedTime(document.getDate("createdTime"));
    // logDTO.setSeverityText(document.getString("severityText"));

    // List<Document> scopeLogsDocuments = (List<Document>) document.get("scopeLogs");

    // System.out.println("-----scoped log data---- " + scopeLogsDocuments.size());
    // if (scopeLogsDocuments.size() > 0) {
    // List<Map<String, Object>> scopeLogsList = new ArrayList<>();
    // for (Document scopeLogsDocument : scopeLogsDocuments) {
    // Map<String, Object> scopeLogsMap = new HashMap<>();
    // scopeLogsMap.put("name", scopeLogsDocument.getString("flags"));
    // System.out.println(scopeLogsDocument);
    // }

    // // logDTO.setScopeLogs(scopeLogsDocuments);
    // }

    return logDTO;
}



}

