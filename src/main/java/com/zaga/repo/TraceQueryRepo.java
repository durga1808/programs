package com.zaga.repo;

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
    }
