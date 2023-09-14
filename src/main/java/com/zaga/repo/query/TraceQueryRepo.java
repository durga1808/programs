package com.zaga.repo.query;

import java.util.List;
import java.util.stream.Collectors;

import com.zaga.entity.otellog.resource.Attribute;
import com.zaga.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<OtelTrace> {


}
