package com.zaga.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.otelevent.ScopeLogs;
import com.zaga.entity.otelevent.scopeLogs.LogRecords;
import com.zaga.entity.queryentity.events.EventsDTO;
import com.zaga.handler.EventQueryhandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventController {

    @Inject
    EventQueryhandler handler;

    @GET
    @Path("/getAllEvents")
    public Response getAllEvents(
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("minutesAgo") int minutesAgo) {
        try {
            List<EventsDTO> allEvents = handler.getAllEvent();

            if (from != null && to != null) {
                Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant().plusSeconds(86399);                                                                   // day

                allEvents = filterEventsByDateRange(allEvents, fromInstant, toInstant);
            } else if (minutesAgo > 0) {
                Instant currentInstant = Instant.now();
                Instant fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

                allEvents = filterEventsByMinutesAgo(allEvents, fromInstant, currentInstant);
            }
            System.out.println("Number of data in the specified time range: " + allEvents.size());

            ObjectMapper objectMapper = new ObjectMapper();
            String responseJson = objectMapper.writeValueAsString(allEvents);
            return Response.ok(responseJson).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred: " + e.getMessage())
                    .build();
        }

    }

    private List<EventsDTO> filterEventsByDateRange(List<EventsDTO> events, Instant from, Instant to) {
        return events.stream()
                .filter(event -> isWithinDateRange(event.getCreatedTime().toInstant(), from, to))
                .collect(Collectors.toList());
    }

    private List<EventsDTO> filterEventsByMinutesAgo(List<EventsDTO> events, Instant fromInstant, Instant toInstant) {
        return events.stream()
                .filter(event -> isWithinDateRange(event.getCreatedTime().toInstant(), fromInstant, toInstant))
                .collect(Collectors.toList());
    }

    private boolean isWithinDateRange(Instant targetInstant, Instant from, Instant to) {
        return !targetInstant.isBefore(from) && !targetInstant.isAfter(to);
    }

//     @GET
//     @Path("/recent")
//     @Produces(MediaType.APPLICATION_JSON)
//     @Consumes(MediaType.APPLICATION_JSON)
//    public List<EventsDTO> filterEvents(@QueryParam("objectKind") String objectKind) {
//         return handler.filterEvents(objectKind);
//     }

@GET
    @Path("/recentevent")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // public List<EventsDTO> getEventsByObjectKind(@QueryParam("objectKind") String objectKind) {
    //     List<EventsDTO> allEvents = handler.getAllEvent();

    //     // Filter events by objectKind
    //     List<EventsDTO> filteredEvents = allEvents.stream()
    //             .filter(event -> objectKind.equals(event.getObjectKind()))
    //             .collect(Collectors.toList());

    //     return filteredEvents;
    // }

    //kind and msg
// public List<String> getFilteredEvents(@QueryParam("kind") String kind) {
//         if (kind == null || kind.isEmpty()) {
//             throw new IllegalArgumentException("Kind parameter is required");
//         }

//         List<EventsDTO> events = handler.getAllEvent();

//         List<String> matchingEvents = new ArrayList<>();

//         for (EventsDTO event : events) {
//             for (ScopeLogs scopeLog : event.getScopeLogs()) {
//                 for (LogRecords logRecord : scopeLog.getLogRecords()) {
//                     if (logRecord.getBody() != null && logRecord.getBody().getStringValue() != null
//                             && event.getObjectKind() != null && event.getObjectKind().equals(kind)) {
//                         matchingEvents.add(logRecord.getBody().getStringValue());
//                     }
//                 }
//             }
//         }

//         return matchingEvents;
//     }


//seveTxt
// public List<String> getFilteredEvents(@QueryParam("kind") String kind, @QueryParam("severityText") String severityText) {
//     if (kind == null || kind.isEmpty()) {
//         throw new IllegalArgumentException("Kind parameter is required");
//     }

//     List<EventsDTO> events = handler.getAllEvent();

//     List<String> matchingEvents = new ArrayList<>();

//     for (EventsDTO event : events) {
//         if (event.getObjectKind() != null && event.getObjectKind().equals(kind)) {
//             for (ScopeLogs scopeLog : event.getScopeLogs()) {
//                 for (LogRecords logRecord : scopeLog.getLogRecords()) {
//                     if (logRecord.getBody() != null && logRecord.getBody().getStringValue() != null
//                             && event.getSeverityText() != null && event.getSeverityText().equals(severityText)) {
//                         matchingEvents.add(logRecord.getBody().getStringValue());
//                     }
//                 }
//             }
//         }
//     }

//     return matchingEvents;
// }

//working code

// public List<String> getFilteredEvents(@QueryParam("objectKind") String objectKind, @QueryParam("minutesAgo") int minutesAgo) {
//     if (objectKind == null || objectKind.isEmpty()) {
//         throw new IllegalArgumentException("ObjectKind parameter is required");
//     }

//     List<EventsDTO> events = handler.getAllEvent();

//     List<String> matchingEvents = new ArrayList<>();

//     for (EventsDTO event : events) {
//         if (event.getObjectKind() != null && event.getObjectKind().equals(objectKind)) {
//             for (ScopeLogs scopeLog : event.getScopeLogs()) {
//                 for (LogRecords logRecord : scopeLog.getLogRecords()) {
//                     if (logRecord.getBody() != null && logRecord.getBody().getStringValue() != null) {
//                         matchingEvents.add("Object Kind: " + event.getObjectKind() + ", Severity Text: " + event.getSeverityText() + ", Body: " + logRecord.getBody().getStringValue());
//                     }
//                 }
//             }
//         }
//     }

//     return matchingEvents;
// }

//minutsago

// public List<String> getFilteredEvents(@QueryParam("objectKind") String objectKind, @QueryParam("minutesAgo") int minutesAgo) {
//     if (objectKind == null || objectKind.isEmpty()) {
//         throw new IllegalArgumentException("ObjectKind parameter is required");
//     }

//     Instant currentInstant = Instant.now();
//     Instant fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//     List<EventsDTO> events = handler.getAllEvent();
//     List<String> matchingEvents = new ArrayList<>();

//     for (EventsDTO event : events) {
//         if (event.getObjectKind() != null && event.getObjectKind().equals(objectKind)) {
//             for (ScopeLogs scopeLog : event.getScopeLogs()) {
//                 for (LogRecords logRecord : scopeLog.getLogRecords()) {
//                     Instant eventInstant = event.getCreatedTime().toInstant();
//                     if (eventInstant.isAfter(fromInstant) && eventInstant.isBefore(currentInstant) && logRecord.getBody() != null && logRecord.getBody().getStringValue() != null) {
//                         matchingEvents.add("Object Kind: " + event.getObjectKind() + ", Severity Text: " + event.getSeverityText() + ", Body: " + logRecord.getBody().getStringValue());
//                     }
//                 }
//             }
//         }
//     }

//     return matchingEvents;
// }



public List<String> getFilteredEvents(@QueryParam("objectKind") String objectKind
// ,@QueryParam("minutesAgo") int minutesAgo
) {
    if (objectKind == null || objectKind.isEmpty()) {
        throw new IllegalArgumentException("ObjectKind parameter is required");
    }

    // Calculate the timestamps for the last 30 minutes
    Instant currentInstant = Instant.now();
    Instant fromInstant = currentInstant.minus(30, ChronoUnit.MINUTES);

    List<EventsDTO> events = handler.getAllEvent();
    List<String> matchingEvents = new ArrayList<>();

    for (EventsDTO event : events) {
        if (event.getObjectKind() != null && event.getObjectKind().equals(objectKind)) {
            for (ScopeLogs scopeLog : event.getScopeLogs()) {
                for (LogRecords logRecord : scopeLog.getLogRecords()) {
                    Instant eventInstant = event.getCreatedTime().toInstant();
                    // Check if the event occurred within the last 30 minutes
                    if (eventInstant.isAfter(fromInstant) && eventInstant.isBefore(currentInstant) && logRecord.getBody() != null && logRecord.getBody().getStringValue() != null) {
                        matchingEvents.add("Object Kind: " + event.getObjectKind() + ", Severity Text: " + event.getSeverityText() + ", Body: " + logRecord.getBody().getStringValue());
                    }
                }
            }
        }
    }

    return matchingEvents;
}

}