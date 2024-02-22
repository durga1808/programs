package com.zaga.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.BsonNull;
import org.bson.Document;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.queryentity.cluster_utilization.ClusterUtilizationDTO;
import com.zaga.entity.queryentity.cluster_utilization.response.ClusterResponse;
import com.zaga.repo.ClusterUtilizationDTORepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ClusterUtilizationHandler {

    @Inject
    ClusterUtilizationDTORepo clusterUtilizationDTORepo;

  @Inject
  MongoClient mongoClient;
    
    public List<ClusterUtilizationDTO> getAllClusterData() {
       return clusterUtilizationDTORepo.listAll();
    }

  public List<ClusterResponse> getAllClusterByDateAndTime(
    LocalDate from,
    LocalDate to,
    int minutesAgo
  ) {
    LocalDateTime startTime = LocalDateTime.now();
    // System.out.println("------------DB call startTimestamp------ " + startTime);

    MongoDatabase database = mongoClient.getDatabase("OtelClusterUtilization");
    MongoCollection<Document> collection = database.getCollection(
      "ClusterDTO"
    );

    List<ClusterResponse> result;

    if (from != null && to != null) {
      result =
        executeAggregationPipeline(
          collection,
          from,
          to
        );
    }
     else if (from != null && minutesAgo > 0) {
      result =
        executeAnotherLogic(collection, from, minutesAgo);
    } 
    else {
      System.out.println(
        "Invalid parameters. Provide either 'from' or 'minutesAgo'."
      );
      result = Collections.emptyList();
    }

    LocalDateTime endTime = LocalDateTime.now();
    // System.out.println("------------DB call endTimestamp------ " + endTime);
    // System.out.println(
    //   "-----------DB call ended Timestamp------ " +
    //   Duration.between(startTime, endTime)
    // );

    return result;
  }


   public List<ClusterResponse> executeAggregationPipeline(
   MongoCollection<Document> collection,
   LocalDate from,
   LocalDate to) {
        List<Document> pipeline = Arrays.asList(
            new Document("$addFields", 
                new Document("justDate", 
                    new Document("$dateToString", 
                        new Document("format", "%m-%d-%Y")
                            .append("date", "$date")
                    )
                )
            ), 
            new Document("$match", 
    new Document("$and", 
        Arrays.asList(
            new Document("justDate", 
                new Document("$gte",from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                    .append("$lte",   to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
            )
        )
    )
),
  new Document("$sort", 
                new Document("date", 1L)
            ), 
            new Document("$group", 
    new Document("_id", "$nodeName")
            .append("avgCpuUsage", 
    new Document("$avg", "$cpuUsage"))
            .append("avgMemoryUsage", 
    new Document("$avg", "$memoryUsage"))
            .append("avgMemoryAvailable", 
    new Document("$avg", "$memoryAvailable"))
            .append("avgFileSystemCapacity", 
    new Document("$avg", "$fileSystemCapacity"))
            .append("avgFileSystemUsage", 
    new Document("$avg", "$fileSystemUsage"))
            .append("avgFileSystemAvailable", 
    new Document("$avg", "$fileSystemAvailable"))), 
    new Document("$group", 
    new Document("_id",  new Document("$const", null))
            .append("cpuUsage", 
    new Document("$sum", "$avgCpuUsage"))
            .append("memoryUsage", 
    new Document("$sum", "$avgMemoryUsage"))
            .append("memoryAvailable", 
    new Document("$sum", "$avgMemoryAvailable"))
            .append("fileSystemCapacity", 
    new Document("$sum", "$avgFileSystemCapacity"))
            .append("fileSystemUsage", 
    new Document("$sum", "$avgFileSystemUsage"))
            .append("fileSystemAvailable", 
    new Document("$sum", "$avgFileSystemAvailable"))));

        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

        List<ClusterResponse> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            ClusterResponse clusterResponse = new ClusterResponse();
            clusterResponse.setCpuUsage(document.getDouble("cpuUsage"));
            clusterResponse.setMemoryUsage(document.getDouble("memoryUsage"));
            clusterResponse.setMemoryAvailable(document.getDouble("memoryAvailable"));
            clusterResponse.setFileSystemCapacity(document.getDouble("fileSystemCapacity"));
            clusterResponse.setFileSystemUsage(document.getDouble("fileSystemUsage"));
            clusterResponse.setFileSystemAvailable(document.getDouble("fileSystemAvailable"));
            result.add(clusterResponse);
        }
        return result;
    }
    


    private List<ClusterResponse> executeAnotherLogic(
    MongoCollection<Document> collection,
    LocalDate from,
    Integer minutesAgo)
    {
        List<Document> pipeline = Arrays.asList(
            new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
                new Document("$gte", Arrays.asList("$date",
                new Document("$subtract", Arrays.asList(new java.util.Date(), minutesAgo * 60L * 1000L)))), 
                new Document("$lte", Arrays.asList("$date", 
                        new java.util.Date()))
            )))),
            new Document("$sort", 
                new Document("date", 1L)
            ), 
            new Document("$group", 
    new Document("_id", "$nodeName")
            .append("avgCpuUsage", 
    new Document("$avg", "$cpuUsage"))
            .append("avgMemoryUsage", 
    new Document("$avg", "$memoryUsage"))
            .append("avgMemoryAvailable", 
    new Document("$avg", "$memoryAvailable"))
            .append("avgFileSystemCapacity", 
    new Document("$avg", "$fileSystemCapacity"))
            .append("avgFileSystemUsage", 
    new Document("$avg", "$fileSystemUsage"))
            .append("avgFileSystemAvailable", 
    new Document("$avg", "$fileSystemAvailable"))), 
    new Document("$group", 
    new Document("_id",  new Document("$const", null))
            .append("cpuUsage", 
    new Document("$sum", "$avgCpuUsage"))
            .append("memoryUsage", 
    new Document("$sum", "$avgMemoryUsage"))
            .append("memoryAvailable", 
    new Document("$sum", "$avgMemoryAvailable"))
            .append("fileSystemCapacity", 
    new Document("$sum", "$avgFileSystemCapacity"))
            .append("fileSystemUsage", 
    new Document("$sum", "$avgFileSystemUsage"))
            .append("fileSystemAvailable", 
    new Document("$sum", "$avgFileSystemAvailable"))));
        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

        List<ClusterResponse> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            ClusterResponse clusterResponse = new ClusterResponse();
            clusterResponse.setCpuUsage(document.getDouble("cpuUsage"));
            clusterResponse.setMemoryUsage(document.getDouble("memoryUsage"));
            clusterResponse.setMemoryAvailable(document.getDouble("memoryAvailable"));
            clusterResponse.setFileSystemCapacity(document.getDouble("fileSystemCapacity"));
            clusterResponse.setFileSystemUsage(document.getDouble("fileSystemUsage"));
            clusterResponse.setFileSystemAvailable(document.getDouble("fileSystemAvailable"));
            result.add(clusterResponse);
        }
        return result;
    }
}
