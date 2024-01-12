package com.zaga.repo;

import com.zaga.entity.queryentity.openshift.ServiceList;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ServiceListRepo implements PanacheMongoRepository<ServiceList>{
    
}
