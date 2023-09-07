package com.zaga.service;

import com.zaga.entity.oteltrace.OtelTrace;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public interface TraceService {
    void createProduct(OtelTrace trace);
}
