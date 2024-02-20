package com.zaga.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaga.entity.queryentity.events.EventsDTO;
import com.zaga.handler.EventQueryhandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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

}
