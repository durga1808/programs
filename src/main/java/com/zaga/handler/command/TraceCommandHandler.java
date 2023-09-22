package com.zaga.handler.command;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.oteltrace.ResourceSpans;
import com.zaga.entity.oteltrace.ScopeSpans;
import com.zaga.entity.oteltrace.scopeSpans.Spans;
import com.zaga.entity.oteltrace.scopeSpans.spans.Attributes;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.repo.command.TraceCommandRepo;
import com.zaga.repo.query.TraceQueryRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TraceCommandHandler {

  @Inject
  TraceCommandRepo traceCommandRepo;

  @Inject
  TraceQueryRepo traceQueryRepo;

  public void createTraceProduct(OtelTrace trace) {
    System.out.println("Tracesss" + trace);
    traceCommandRepo.persist(trace);

    List<TraceDTO> traceDTOs = extractAndMapData(trace);
    System.out.println(traceDTOs);
  }

  // logic for getting serviceName
  private String getServiceName(ResourceSpans resourceSpans) {
    return resourceSpans
      .getResource()
      .getAttributes()
      .stream()
      .filter(attribute -> "service.name".equals(attribute.getKey()))
      .findFirst()
      .map(attribute -> attribute.getValue().getStringValue())
      .orElse(null);
  }


    // logic for calculating the createdtime
  private String calculateCreatedTime(Spans span) {
    String startTimeUnixNano = span.getStartTimeUnixNano();
    long startUnixNanoTime = Long.parseLong(startTimeUnixNano);
    Instant startInstant = Instant.ofEpochSecond(
      startUnixNanoTime / 1_000_000_000L,
      startUnixNanoTime % 1_000_000_000L
    );

    Instant currentInstant = Instant.now();
    Duration createdDuration = Duration.between(startInstant, currentInstant);
    String createdTime = formatDuration(createdDuration);

    return createdTime;
  }

  // logic for calculating the duration for trace 
  private static String formatDuration(Duration duration) {
    if (duration.toMinutes() < 1) {
      long seconds = duration.getSeconds();
      if (seconds == 0) {
        return "a few seconds ago";
      } else if (seconds == 1) {
        return "a second ago";
      } else {
        return seconds + " seconds ago";
      }
    } else if (duration.toHours() < 1) {
      long minutes = duration.toMinutes();
      if (minutes == 1) {
        return "a minute ago";
      } else {
        return minutes + " minutes ago";
      }
    } else if (duration.toDays() < 1) {
      long hours = duration.toHours();
      if (hours == 1) {
        return "an hour ago";
      } else {
        return hours + " hours ago";
      }
    } else {
      long days = duration.toDays();
      if (days == 1) {
        return "a day ago";
      } else {
        return days + " days ago";
      }
    }
  }

  private int calculateDuration(Spans span) {
    String startTimeUnixNano = span.getStartTimeUnixNano();
    String endTimeUnixNano = span.getEndTimeUnixNano();

    long startUnixNanoTime = Long.parseLong(startTimeUnixNano);
    long endUnixNanoTime = Long.parseLong(endTimeUnixNano);

    Instant startInstant = Instant.ofEpochSecond(
      startUnixNanoTime / 1_000_000_000L,
      startUnixNanoTime % 1_000_000_000L
    );
    Instant endInstant = Instant.ofEpochSecond(
      endUnixNanoTime / 1_000_000_000L,
      endUnixNanoTime % 1_000_000_000L
    );

    Duration duration = Duration.between(startInstant, endInstant);

    return (int) duration.toMillis();
  }

  // extraction and marshelling of data and persistance for trace
  private List<TraceDTO> extractAndMapData(OtelTrace trace) {
    List<TraceDTO> traceDTOs = new ArrayList<>();

    try {
        for (ResourceSpans resourceSpans : trace.getResourceSpans()) {
            String serviceName = getServiceName(resourceSpans);
            
            List<String> traceIdList = new ArrayList<>(); 
            
            for (ScopeSpans scopeSpans : resourceSpans.getScopeSpans()) {
                List<Spans> spans = scopeSpans.getSpans();
                
                for (Spans span : spans) {
                    String traceId = span.getTraceId();

                    if (!traceIdList.contains(traceId)) {
                        traceIdList.add(traceId);
                    }
                }
            }
            
            for (String traceIdLoop : traceIdList) {
                TraceDTO traceDTO = new TraceDTO(); 
                List<Spans> objectList = new ArrayList<Spans>();
                
                for (ScopeSpans scopeSpans : resourceSpans.getScopeSpans()) {
                    List<Spans> spans = scopeSpans.getSpans();
                    
                    for (Spans span : spans) {
                        String traceId = span.getTraceId();
                        
                        if (traceId.contains(traceIdLoop)) {
                            traceDTO.setServiceName(serviceName);
                            traceDTO.setTraceId(traceId);
                            
                            if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
                                traceDTO.setOperationName(span.getName());
                            } else {
                            }
                            
                            List<Attributes> attributes = span.getAttributes();
                            
                            for (Attributes attribute : attributes) {
                                if ("http.method".equals(attribute.getKey())) {
                                    traceDTO.setMethodName(attribute.getValue().getStringValue());
                                } else if ("http.status_code".equals(attribute.getKey())) {
                                    String statusCodeString = attribute.getValue().getStringValue();
                                    
                                    try {
                                        Integer statusCode = Integer.parseInt(statusCodeString);
                                        traceDTO.setStatusCode(statusCode);
                                    } catch (NumberFormatException e) {
                                        // Handle the parsing error
                                    }
                                } else {
                                  
                                }
                            }
                            
                            traceDTO.setDuration(calculateDuration(span));
                            traceDTO.setCreatedTime(calculateCreatedTime(span));
                            objectList.add(span);
                            traceDTO.setSpanCount(String.valueOf(objectList.size()));
                            traceDTO.setSpans(objectList);
                        }
                    }
                }
                traceQueryRepo.persist(traceDTO);
                traceDTOs.add(traceDTO);
            }
        }
        return traceDTOs;
    } catch (Exception e) {
        e.printStackTrace();
        return traceDTOs;
    }
}



  // get all trace data
  public List<OtelTrace> getTraceProduct(OtelTrace trace) {
    return traceCommandRepo.listAll();
  }
}
