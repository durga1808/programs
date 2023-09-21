package com.zaga.repo.query;

import com.zaga.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<TraceDTO> {
    }
