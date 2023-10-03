package com.zaga.handler;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Filters;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogQuery;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.entity.queryentity.trace.TraceQuery;
import com.zaga.repo.LogQueryRepo;
import com.mongodb.client.model.Filters;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;

@ApplicationScoped
public class LogQueryHandler {

    @Inject
    LogQueryRepo logQueryRepo;
    
    

    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://devteam:Zagateam2023*@applicationcluster.tvbngn1.mongodb.net/test";

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

//     public List<LogDTO> searchLogsPaged(LogQuery query, int page, int pageSize, int minutesAgo) {
//     FindIterable<Document> result = getFilteredLogResults(query, page, pageSize, minutesAgo);
//     List<LogDTO> logDTOList = new ArrayList<>();
    
//     try (MongoCursor<Document> cursor = result.iterator()) {
//         while (cursor.hasNext()) {
//             Document document = cursor.next();
//             LogDTO logDTO = new LogDTO();
            
//             logDTO.setServiceName(document.getString("serviceName"));
//             logDTO.setTraceId(document.getString("traceId"));
            
//             // Assuming you have a method to fetch scope logs based on traceId
//             List<ScopeLogs> scopeLogs = fetchScopeLogsByTraceId(logDTO.getTraceId());
//             logDTO.setScopeLogs(scopeLogs);
            
//             logDTOList.add(logDTO);
//         }
//     }
    
//     return logDTOList;
// }

// private List<ScopeLogs> fetchScopeLogsByTraceId(String traceId) {
//     // Implement logic to fetch scope logs by traceId
//     // Return the actual scope logs or an empty list if not found
//     return new ArrayList<>();
// }

// private FindIterable<Document> getFilteredLogResults(LogQuery query, int page, int pageSize, int minutesAgo) {
//     // Construct a query based on LogQuery's serviceName and severityText fields
//     Document filter = new Document();
    
//     if (query.getServiceName() != null) {
//         filter.append("serviceName", query.getServiceName());
//     }
    
//     if (query.getSeverityText() != null) {
//         filter.append("severityText", query.getSeverityText());
//     }
    
//     // Apply additional filters based on page, pageSize, and minutesAgo
    
//     // Use your MongoDB driver to apply the filter and return the results
//     // Example: collection.find(filter).skip((page - 1) * pageSize).limit(pageSize).sort(Sorts.descending("timestamp"))
//     return LogDTO .find(filter).skip((page - 1) * pageSize).limit(pageSize);
// }

// public long countQueryLogs(LogQuery query, int minutesAgo) {
//     FindIterable<Document> result = getFilteredLogResults(query, 0, Integer.MAX_VALUE, minutesAgo);
//     long totalCount = result.into(new ArrayList<>()).size();
//     return totalCount;
// }


// public List<LogDTO> searchLogsPaged(LogQuery query, int page, int pageSize, int minutesAgo) {
//     try {
//         FindIterable<Document> result = getFilteredLogResults(query, page, pageSize, minutesAgo);
//         List<LogDTO> logDTOList = new ArrayList<>();
        
//         try (MongoCursor<Document> cursor = result.iterator()) {
//             while (cursor.hasNext()) {
//                 Document document = cursor.next();
//                 LogDTO logDTO = new LogDTO();
                
//                 logDTO.setServiceName(document.getString("serviceName"));
//                 logDTO.setSeverityText(document.getString("severityText"));
                
//                 // Assuming you have a method to fetch scope logs based on traceId
//                 List<ScopeLogs> scopeLogs = fetchScopeLogsByTraceId(logDTO.getTraceId());
//                 logDTO.setScopeLogs(scopeLogs);
                
//                 logDTOList.add(logDTO);
//             }
//         }
        
//         return logDTOList;
//     } catch (Exception e) {
//         // Log the exception and handle it appropriately
//         e.printStackTrace(); // Replace this with your actual logging mechanism
//         throw new InternalServerErrorException("An error occurred: " + e.getMessage());
//     }
// }


private List<ScopeLogs> fetchScopeLogsByTraceId(String traceId) {
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

//sort error first

    // public List<LogDTO> getAllLogsSortedByErrorFirst() {
    //     try (MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING)) {
    //         // Specify your database and collection
    //         MongoCollection<Document> logCollection = mongoClient.getDatabase("OtelLog").getCollection("LogDTO");

    //         // Aggregation pipeline stages
    //         List<Document> pipeline = List.of(
    //                 Aggregates.unwind("$scopeLogs"),
    //                 Aggregates.unwind("$scopeLogs.logRecords"),
    //                 Aggregates.match(Filters.eq("scopeLogs.logRecords.severityText", "ERROR")),
    //                 Aggregates.sort(Sorts.descending("scopeLogs.logRecords.severityNumber"))
    //                 // Add more stages as needed
    //         );

    //         List<LogDTO> sortedLogs = logQueryRepo.fromDocuments(logCollection.aggregate(pipeline).into(new ArrayList<>()));

    //         return sortedLogs;
    //     }

}


