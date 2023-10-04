package com.zaga.handler;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.otellog.scopeLogs.LogRecord;
import com.zaga.entity.otellog.scopeLogs.Scope;
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
public List<LogDTO> getErrorLogs() {
    MongoDatabase database = mongoClient.getDatabase("OtelLog"); // Replace with your actual database name
    MongoCollection<LogDTO> logDTOCollection = database.getCollection("LogDTO", LogDTO.class);

    Bson matchStage = Aggregates.match(Filters.elemMatch("scopeLogs", Filters.elemMatch("logRecords", Filters.eq("severityText", "ERROR"))));
    Bson unwindStage = Aggregates.unwind("$scopeLogs");
    Bson sortStage = Aggregates.sort(Sorts.descending("createdTime"));

    List<LogDTO> result = logDTOCollection.aggregate(List.of(matchStage, unwindStage, sortStage))
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


public List<LogDTO> findByMatching(int page, int pageSize, String serviceName) {
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

    // Calculate data count
    int dataCount = filteredLogList.size();

    // Paginate the results
    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, dataCount);
    return filteredLogList.subList(startIndex, endIndex);
}



//search functionality 
// public List<LogDTO> searchLogs(String keyword) {
//     List<LogDTO> results = new ArrayList<>();
//     String regexPattern = ".*" + Pattern.quote(keyword) + ".*";
//     BsonRegularExpression regex = new BsonRegularExpression(regexPattern, "i");

//     try {
//         MongoCollection<Document> collection = mongoClient
//                 .getDatabase("OtelLog")
//                 .getCollection("LogDTO");

//         Document query = new Document("$or", List.of(
//             new Document("serviceName", regex),
//             new Document("traceId", regex),
//             new Document("spanId", regex),
//             new Document("severityText", regex)
//         ));

//         MongoCursor<Document> cursor = collection.find(query).iterator();

//         while (cursor.hasNext()) {
//             Document document = cursor.next();
//             LogDTO logDTO = mapDocumentToLogDTO(document);
//             results.add(logDTO);
//         }
//     } catch (Exception e) {
//         // Handle exceptions
//     }

//     return results;
// }

// private LogDTO mapDocumentToLogDTO(Document document) {
//     LogDTO logDTO = new LogDTO();
//     logDTO.setServiceName(document.getString("serviceName"));
//     logDTO.setTraceId(document.getString("traceId"));
//     logDTO.setSpanId(document.getString("spanId"));
//     logDTO.setCreatedTime(document.getDate("createdTime"));
//     logDTO.setSeverityText(document.getString("severityText"));

//     List<Document> scopeLogsDocuments = (List<Document>) document.get("scopeLogs");
//     if (scopeLogsDocuments != null && !scopeLogsDocuments.isEmpty()) {
//         List<ScopeLogs> scopeLogsList = new ArrayList<>();
//         for (Document scopeLogsDocument : scopeLogsDocuments) {
//             ScopeLogs scopeLogs = new ScopeLogs();
//             scopeLogs.setScope(mapScope(scopeLogsDocument.get("scope", Document.class)));
//             scopeLogs.setLogRecords(mapLogRecords((List<Document>) scopeLogsDocument.get("logRecords")));
//             scopeLogsList.add(scopeLogs);
//         }
//         logDTO.setScopeLogs(scopeLogsList);
//     }

//     return logDTO;
// }

// private ScopeLogs mapScope(Document scopeDocument) {
//     ScopeLogs scopeLogs = new ScopeLogs();
//     // Map scopeDocument fields to ScopeLogs object if needed
//     return scopeLogs;
// }

// private List<LogRecord> mapLogRecords(List<Document> logRecordsDocuments) {
//     List<LogRecord> logRecordsList = new ArrayList<>();
//     for (Document logRecordDocument : logRecordsDocuments) {
//         LogRecord logRecord = new LogRecord();
//         // Map logRecordDocument fields to LogRecord object if needed
//         logRecordsList.add(logRecord);
//     }
//     return logRecordsList;
// }

    

}

