package com.zaga.repo;

import com.zaga.entity.queryentity.log.LogRecordDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogRecordDTORepo implements PanacheMongoRepository<LogRecordDTO> {
   
}