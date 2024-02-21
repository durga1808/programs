package com.zaga.repo;

import com.zaga.entity.queryentity.events.EventsDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class EventDTORepo implements PanacheMongoRepository<EventsDTO>{
    
}
