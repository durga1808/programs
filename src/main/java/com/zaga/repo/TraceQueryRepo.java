package com.zaga.repo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zaga.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<TraceDTO> {
    
  
   public List<TraceDTO> findRecentDataPaged(String serviceName, int page, int pageSize) {
      PanacheQuery<TraceDTO> query = find("serviceName = ?1 order by createdTime desc", serviceName);
        query.page(page, pageSize); // Apply paging
        return query.list();
    }

    public long countData(String serviceName) {
        return count("serviceName = ?1", serviceName);
    }

     public List<TraceDTO> findAllOrderByCreatedTimeDesc() {
        return listAll(Sort.descending("createdTime"));
    }

    public List<TraceDTO> findAllOrderByCreatedTimeAsc() {
        return listAll(Sort.ascending("createdTime"));
    }

    

    public List<TraceDTO> findByServiceNameAndCreatedTime(String serviceName, Date startDate, Date endDate) {
        // Manually implement your custom query logic here
    
        // Convert the startDate and endDate to Instant
        Instant startInstant = startDate.toInstant();
        Instant endInstant = endDate.toInstant();
    
        // Fetch data from MongoDB filtered by serviceName
        List<TraceDTO> traceList = list("serviceName = ?1", serviceName);
    
        List<TraceDTO> filteredTraceList = new ArrayList<>();
        for (TraceDTO traceDTO : traceList) {
            Date createdTime = traceDTO.getCreatedTime();
            Instant createdInstant = createdTime.toInstant();
            if (createdInstant.isAfter(startInstant) && createdInstant.isBefore(endInstant)) {
                filteredTraceList.add(traceDTO);
            }
        }
        System.out.println("filteredTraceList: " + filteredTraceList);
    
        return filteredTraceList;
    }
    
}