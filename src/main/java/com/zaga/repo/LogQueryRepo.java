package com.zaga.repo;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.entity.queryentity.log.LogDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
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
    

    public List<LogDTO> findByKeyword(String keyword) {
        // Using MongoDB's $regex operator for case-insensitive search
        String regexPattern = "(?i).*" + keyword + ".*";
        return list("{'fieldInYourDocument': {$regex =?1}}", regexPattern);
    }

}
