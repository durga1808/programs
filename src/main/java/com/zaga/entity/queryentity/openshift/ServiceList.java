package com.zaga.entity.queryentity.openshift;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
@MongoEntity(collection="OpenshiftServices",database="ObservabilityCredentials")
public class ServiceList {
    private String namespaceName;
    private String serviceName;
    private String instrumented;
    private String deploymentName;
    private String createdTime;
    }
