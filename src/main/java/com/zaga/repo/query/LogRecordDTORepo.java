package com.zaga.repo.query;

import com.zaga.entity.queryentity.log.LogRecordDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogRecordDTORepo implements PanacheMongoRepository<LogRecordDTO> {
   
}