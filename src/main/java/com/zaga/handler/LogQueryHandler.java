package com.zaga.handler;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogMetrics;
import com.zaga.entity.queryentity.log.LogQuery;
import com.zaga.entity.queryentity.trace.TraceMetrics;
import com.zaga.repo.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;
    

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

    public List<LogDTO> searchLogsPaged(LogQuery query, int page, int pageSize, int minutesAgo) {
        
    FindIterable<Document> result = getFilteredResultsForLogs(query, page, pageSize, minutesAgo);

    List<LogDTO> logDTOList = new ArrayList<>();
    try (MongoCursor<Document> cursor = result.iterator()) {
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LogDTO logDTO = new LogDTO();

            logDTO.setTraceId(document.getString("traceId"));
            logDTO.setServiceName(document.getString("serviceName"));
            logDTO.setScopeLogs((List<ScopeLogs>) document.get("scopeLogs"));

            logDTOList.add(logDTO);
        }
    }

    return logDTOList;
}

    private FindIterable<Document> getFilteredResultsForLogs(LogQuery query, int page, int pageSize, int minutesAgo) {
        return null;
    }

    public long countQueryLogs(LogQuery logQuery, int minutesAgo) {
        return 0;
    }

    
  //sort orer decending 
  public List<LogDTO> getAllTracesOrderByCreatedTimeDesc() {
    return logQueryRepo.findAllOrderByCreatedTimeDesc();
  }


//sort order ascending
public List<LogDTO> getAllTracesAsc() {
    return logQueryRepo.findAllOrderByCreatedTimeAsc();
}


// public List<LogMetrics> getLogMetricCount(int timeMinutesAgo){

            

//             // Get the current time in milliseconds since epoch
//             long currentTimeMillis = Instant.now().toEpochMilli();

//             // Calculate the threshold time in milliseconds
//             long thresholdTimeMillis = currentTimeMillis - (timeMinutesAgo * 60 * 1000);

//             // Group log records by serviceName
//             Map<String, LogMetrics> logMetricsMap = new HashMap<>();

//             JsonNode scopeLogs = jsonData.get("scopeLogs");
//             for (JsonNode scopeLog : scopeLogs) {
//                 String serviceName = scopeLog.get("scope").get("name").asText();
//                 JsonNode logRecords = scopeLog.get("logRecords");

//                 LogMetrics logMetrics = logMetricsMap.computeIfAbsent(serviceName, k -> new LogMetrics(serviceName, 0L, 0L, 0L));

//                 for (JsonNode logRecord : logRecords) {
//                     String severityText = logRecord.get("severityText").asText();
//                     long logTimeMillis = Long.parseLong(logRecord.get("timeUnixNano").asText()) / 1_000_000;

//                     // Check if the log record is within the time threshold
//                     if (logTimeMillis >= thresholdTimeMillis) {
//                         switch (severityText) {
//                             case "ERROR":
//                                 logMetrics.setErrorCallCount(logMetrics.getErrorCallCount() + 1);
//                                 break;
//                             case "WARN":
//                                 logMetrics.setWarnCallCount(logMetrics.getWarnCallCount() + 1);
//                                 break;
//                             case "DEBUG":
//                                 logMetrics.setDebugCallCount(logMetrics.getDebugCallCount() + 1);
//                                 break;
//                         }
//                     }
//                 }
//             }

//             // Add the logMetrics to the result list
//             logMetricsList.addAll(logMetricsMap.values());
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         return logMetricsList;
//     }

// }
// }
}