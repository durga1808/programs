package com.zaga.repo;

import java.util.List;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.entity.queryentity.log.LogDTO;

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
    
}
