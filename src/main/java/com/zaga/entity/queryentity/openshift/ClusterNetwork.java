package com.zaga.entity.queryentity.openshift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("id")
@ApplicationScoped
public class ClusterNetwork {

    private String cidr;
    private String hostPrefix;
    private String networkType;
    private String serviceNetwork;
    private String apiServerInternalIP;
    private String ingressIP;
    
}
