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
// public List<String> filterEvents(String objectKind) {
//     List<EventsDTO> allEvents = getAllEvents();
//     List<String> matchingEvents = new ArrayList<>();

//     for (EventsDTO event : allEvents) {
//         if (event.getObjectKind().equals(objectKind)) {
//             for (ScopeLogs scopeLog : event.getScopeLogs()) {
//                 for (LogRecords logRecord : scopeLog.getLogRecords()) {
//                     if (logRecord.getBody() != null && logRecord.getBody().getStringValue() != null) {
//                         matchingEvents.add("Object Kind: " + objectKind + ", Body: " + logRecord.getBody().getStringValue());
//                     }
//                 }
//             }
//         }
//     }

//     return matchingEvents;
// }

// private List<EventsDTO> getAllEvents() {
//     // Implement this method to retrieve events from your data source
//     // For demonstration purposes, I'll return an empty list here
//     return List.of();
// }


}




