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
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TraceCommandHandler {

  @Inject
  TraceCommandRepo traceCommandRepo;

  @Inject
  TraceQueryRepo traceQueryRepo;

  @Transactional
  public void createTraceProduct(OtelTrace trace) {
    System.out.println("Tracesss" + trace);
    traceCommandRepo.persist(trace);

    List<TraceDTO> traceDTOs = extractAndMapData(trace);
    System.out.println(traceDTOs);
  }

  public List<TraceDTO> extractAndMapData(OtelTrace trace) {
    List<TraceDTO> traceDTOs = new ArrayList<>();

    try {
      List<String> traceIdList = new ArrayList<>();
      // Map<String, Object> attributeMap = new HashMap<>();

      List<CompletableFuture<Void>> futures = new ArrayList<>();

      for (ResourceSpans resourceSpans : trace.getResourceSpans()) {
        String serviceName = getServiceName(resourceSpans);
        for (ScopeSpans scopeSpans : resourceSpans.getScopeSpans()) {
          for (Spans span : scopeSpans.getSpans()) {
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
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    traceDTO.setServiceName(serviceName);
                    traceDTO.setTraceId(traceId);
                    if (
                      span.getParentSpanId() == null ||
                      span.getParentSpanId().isEmpty()
                    ) {
                      traceDTO.setMethodName(extractMethodName(span));
                      traceDTO.setStatusCode(extractStatusCode(span));
                    }
                    traceDTO.setDuration(calculateDuration(span));
                    traceDTO.setSpanCount(
                      String.valueOf(scopeSpans.getSpans().size())
                    );
                    traceDTO.setCreatedTime(span.getStartTimeUnixNano());

                    objectList.add(span);
                    traceDTO.setSpans(objectList);
                    // traceDTOs.add(traceDTO);

                    // Persist traceDTO asynchronously
                    traceQueryRepo.persist(traceDTO);
                  }
                );
                futures.add(future);
              }
            }
          }
        }
      }

      return traceDTOs;
    } catch (Exception e) {
      e.printStackTrace();
      return traceDTOs;
    }
  }

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

  private String calculateDuration(Spans span) {
    return "CalculatedDuration";
  }

  private String extractMethodName(Spans span) {
    List<Attributes> attributes = span.getAttributes();
    for (Attributes attribute : attributes) {
      if ("http.method".equals(attribute.getKey())) {
        return attribute.getValue().getStringValue();
      }
    }
    return "Unknown";
  }

  private String extractStatusCode(Spans span) {
    List<Attributes> attributes = span.getAttributes();
    for (Attributes attribute : attributes) {
      if ("http.status_code".equals(attribute.getKey())) {
        return String.valueOf(attribute.getValue().getIntValue());
      }
    }
    return "Unknown";
  }

  public List<OtelTrace> getTraceProduct(OtelTrace trace) {
    return traceCommandRepo.listAll();
  }
}
