package com.zaga.repo;

import com.zaga.entity.otelevent.OtelEvents;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class EventQueryRepo implements PanacheMongoRepository<OtelEvents>{
    
}
