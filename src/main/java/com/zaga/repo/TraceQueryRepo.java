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
        query.page(page, pageSize); 
        return query.list();
    }

    public long countData(String serviceName) {
        return count("serviceName = ?1", serviceName);
    }

     public List<TraceDTO> findAllOrderByCreatedTimeDesc(List<String> serviceNameList) {
      return find("serviceName in ?1",Sort.descending("createdTime"),serviceNameList).list();
    }

    public List<TraceDTO> findAllOrderByCreatedTimeAsc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.ascending("createdTime"),serviceNameList).list();
    }

    

    public List<TraceDTO> findByServiceNameAndCreatedTime(String serviceName, Date startDate, Date endDate) {
        
        Instant startInstant = startDate.toInstant();
        Instant endInstant = endDate.toInstant();
    
        List<TraceDTO> traceList = list("serviceName = ?1", serviceName);
    
        List<TraceDTO> filteredTraceList = new ArrayList<>();
        for (TraceDTO traceDTO : traceList) {
            Date createdTime = traceDTO.getCreatedTime();
            Instant createdInstant = createdTime.toInstant();
            if (createdInstant.isAfter(startInstant) && createdInstant.isBefore(endInstant)) {
                filteredTraceList.add(traceDTO);
            }
        }
        // System.out.println("filteredTraceList: " + filteredTraceList);
    
        return filteredTraceList;
    }
    
}