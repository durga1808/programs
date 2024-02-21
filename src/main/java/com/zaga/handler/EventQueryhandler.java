package com.zaga.handler;

import java.util.List;

import com.zaga.entity.queryentity.events.EventsDTO;
import com.zaga.repo.EventDTORepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventQueryhandler {
    
@Inject
EventDTORepo eventDTORepo;



public List<EventsDTO> getAllEvent() {

    return eventDTORepo.listAll();
}


}
