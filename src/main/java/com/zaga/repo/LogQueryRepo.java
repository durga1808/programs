package com.zaga.repo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.entity.queryentity.log.LogDTO;
import com.zaga.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<LogDTO> {

    
       public List<LogDTO> findByServiceName(String serviceName, int page, int pageSize) {
        return find("serviceName", serviceName)
                .page(page, pageSize)
                .list();
    }

    public long countByServiceName(String serviceName) {
        return count("serviceName", serviceName);
    }


    public List<LogDTO> findAllOrderByCreatedTimeDesc() {
        return listAll(Sort.descending("createdTime"));
    }

    public List<LogDTO> findAllOrderByCreatedTimeAsc() {
        return listAll(Sort.ascending("createdTime"));
    }


    public List<LogDTO> findByServiceNameAndCreatedTime(String serviceName, Date startDate, Date endDate) {
        // Manually implement your custom query logic here
    
        // Convert the startDate and endDate to Instant
        Instant startInstant = startDate.toInstant();
        Instant endInstant = endDate.toInstant();
    
        // Fetch data from MongoDB filtered by serviceName
        List<LogDTO> logList = list("serviceName = ?1", serviceName);
    
        List<LogDTO> filteredLogList = new ArrayList<>();
        for (LogDTO logDTO : logList) {
            Date createdTime = logDTO.getCreatedTime();
            Instant createdInstant = createdTime.toInstant();
            if (createdInstant.isAfter(startInstant) && createdInstant.isBefore(endInstant)) {
                filteredLogList.add(logDTO);
            }
        }
        System.out.println("filteredTraceList: " + filteredLogList);
    
        return filteredLogList;
    }
    
    

}
