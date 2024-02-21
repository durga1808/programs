package com.zaga.handler;

import java.util.List;

import com.zaga.entity.queryentity.cluster_utilization.ClusterUtilizationDTO;
import com.zaga.repo.ClusterUtilizationDTORepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ClusterUtilizationHandler {

    @Inject
    ClusterUtilizationDTORepo clusterUtilizationDTORepo;

    public List<ClusterUtilizationDTO> getAllClusterData() {
       return clusterUtilizationDTORepo.listAll();
    }
    
}
