package com.zaga.repo;

import com.zaga.entity.queryentity.node.NodeMetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NodeDTORepo implements PanacheMongoRepository<NodeMetricDTO>{
    
}
