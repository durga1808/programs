package com.zaga.handler;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.zaga.entity.otellog.ScopeLogs;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.log.LogQuery;
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

    
  
}

