package com.zaga.handler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.zaga.entity.otelevent.ScopeLogs;
import com.zaga.entity.otelevent.scopeLogs.LogRecords;
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




