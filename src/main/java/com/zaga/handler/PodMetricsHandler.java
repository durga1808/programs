package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.zaga.entity.queryentity.podMetrics.MetricDTO;
import com.zaga.entity.queryentity.podMetrics.PodMetricDTO;
import com.zaga.entity.queryentity.podMetrics.PodMetricsResponseData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class PodMetricsHandler {

  @Inject
  MongoClient mongoClient;

    // public List<PodMetricsResponseData> getAllPodMetricsByDate(LocalDate from, LocalDate to, int page, int pageSize, int minutesAgo) {
    //     LocalDateTime startTime = LocalDateTime.now();
    //     System.out.println("------------DB call startTimestamp------ " + startTime);

    //     MongoDatabase database = mongoClient.getDatabase("OtelPodMetrics");
    //     MongoCollection<Document> collection = database.getCollection("PodMetricDTO");

    //     List<PodMetricsResponseData> result = executeAggregationPipeline(database, collection,from,to);

    //     LocalDateTime endTime = LocalDateTime.now();
    //     System.out.println("------------DB call endTimestamp------ " + endTime);
    //     System.out.println("-----------DB call ended Timestamp------ " + Duration.between(startTime, endTime));

    //     return result;
    // }

    public List<PodMetricsResponseData> getAllPodMetricsByDate(LocalDate from, LocalDate to, int page, int pageSize, int minutesAgo) {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("------------DB call startTimestamp------ " + startTime);
    
        MongoDatabase database = mongoClient.getDatabase("OtelPodMetrics");
        MongoCollection<Document> collection = database.getCollection("PodMetricDTO");
    
        List<PodMetricsResponseData> result;
    
        if (minutesAgo > 0) {
            result = executeAggregationPipelineWithMinutesAgo(database, collection, minutesAgo,page,pageSize);
        } else {
            result = executeAggregationPipeline(database, collection, from, to,page,pageSize);
        }
    
        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("------------DB call endTimestamp------ " + endTime);
        System.out.println("-----------DB call ended Timestamp------ " + Duration.between(startTime, endTime));
    
        return result;
    }
    

    // @SuppressWarnings("unchecked")
    public List<PodMetricsResponseData> executeAggregationPipeline(
        MongoDatabase database,
        MongoCollection<Document> collection,
        LocalDate from,
        LocalDate to,
        int page,
        int pageSize
    ) {
        int skip = (page - 1) * pageSize; // Calculate the number of documents to skip
    
        List<Document> pipeline = Arrays.asList(
            new Document("$unwind", "$metrics"),
            new Document(
                "$addFields",
                new Document(
                    "metrics",
                    new Document(
                        "$mergeObjects",
                        Arrays.asList(
                            "$metrics",
                            new Document(
                                "justDate",
                                new Document(
                                    "$dateToString",
                                    new Document("format", "%m-%d-%Y")
                                        .append("date", "$metrics.date")
                                )
                            )
                        )
                    )
                )
            ),
            new Document(
                "$match",
                new Document(
                    "$and",
                    Arrays.asList(
                        new Document(
                            "metrics.justDate",
                            new Document(
                                "$gte",
                                from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                            )
                                .append(
                                    "$lte",
                                    to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                                )
                        )
                    )
                )
            ),
            new Document(
                "$group",
                new Document(
                    "_id",
                    new Document("namespaceName", "$namespaceName")
                        .append("podName", "$podName")
                )
                    .append("namespaceName", new Document("$first", "$namespaceName"))
                    .append("podName", new Document("$first", "$podName"))
                    .append("metrics", new Document("$push", "$metrics"))
                    .append("totalCount", new Document("$sum", 1)) // Calculate total count for each pod
            ),
            new Document(
                "$project",
                new Document("_id", 0)
                    .append("namespaceName", "$_id.namespaceName")
                    .append("podName", "$_id.podName")
                    .append(
                        "metrics",
                        new Document("$slice", Arrays.asList("$metrics", skip, pageSize))
                    )
                    .append("totalCount", 1) 
            )
        );

        AggregateIterable<Document> aggregationResult = collection.aggregate(
    pipeline,
    Document.class
);

// Result list to hold PodMetricsResponseData objects
List<PodMetricsResponseData> resultList = new ArrayList<>();

// Iterate through aggregation result
for (Document doc : aggregationResult) {
    List<Document> metrics = (List<Document>) doc.get("metrics");
    if (metrics != null && !metrics.isEmpty()) {
        String namespaceName = doc.getString("namespaceName");
        String podName = doc.getString("podName");
        Integer totalCount = doc.getInteger("totalCount");
        PodMetricsResponseData responseData = new PodMetricsResponseData();
        responseData.setNamespaceName(namespaceName);
        responseData.setTotalCount(totalCount); 
        // responseData.setPodName(podName);
        List<PodMetricDTO> podMetricsList = new ArrayList<>();

        // Create a single MetricDTO object for all metrics
        List<MetricDTO> metricDTOs = new ArrayList<>();
        for (Document metricDoc : metrics) {
            MetricDTO metricData = new MetricDTO();
            metricData.setCpuUsage(metricDoc.getDouble("cpuUsage"));
            metricData.setDate(metricDoc.getDate("date"));
            metricData.setMemoryUsage(metricDoc.getLong("memoryUsage"));
            metricDTOs.add(metricData);
        }

        // Set the metrics list for the response data
        PodMetricDTO podMetricsData = new PodMetricDTO();
        podMetricsData.setPodName(podName);
        podMetricsData.setNamespaceName(namespaceName);
        podMetricsData.setMetrics(metricDTOs);
        podMetricsList.add(podMetricsData);

        // Set the pods list for the response data
        responseData.setPods(podMetricsList);

        resultList.add(responseData);
    }
}

return resultList;
}
    
    
public List<PodMetricsResponseData> executeAggregationPipelineWithMinutesAgo(
        MongoDatabase database, MongoCollection<Document> collection,
        int minutesAgo, int page, int pageSize) {
    LocalDateTime currentTime = LocalDateTime.now().minusMinutes(minutesAgo);
    int skip = (page - 1) * pageSize; // Calculate the number of documents to skip

    List<Document> pipeline = Arrays.asList(
        new Document("$addFields",
            new Document("metrics",
                new Document("$map",
                    new Document("input", "$metrics")
                        .append("in",
                            new Document("$mergeObjects",
                                Arrays.asList(
                                    "$$this",
                                    new Document("justDate",
                                        new Document("$dateToString",
                                            new Document("format", "%m-%d-%Y")
                                                .append("date", "$$this.date")
                                        )
                                    )
                                )
                            )
                        )
                )
            )
        ),
        new Document("$match",
            new Document("$and", Arrays.asList(
                new Document("metrics.date",
                    new Document("$gte", Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
                )
            ))
        ),
        new Document("$unwind", "$metrics"),
        new Document("$group",
            new Document("_id",
                new Document("namespaceName", "$namespaceName")
                    .append("podName", "$podName"))
                .append("namespaceName", new Document("$first", "$namespaceName"))
                .append("podName", new Document("$first", "$podName"))
                .append("metrics", new Document("$push", "$metrics"))
        ),
        new Document("$group",
            new Document("_id", "$namespaceName")
                .append("namespaceName", new Document("$first", "$namespaceName"))
                .append("pods", new Document("$push",
                    new Document("podName", "$podName").append("metrics", "$metrics")))
        ),
        new Document("$project",
            new Document("_id", 0)
                .append("namespaceName", "$_id")
                .append("pods", new Document("$slice", Arrays.asList("$pods", skip, pageSize)))
        )
    );

    AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline, Document.class);
    List<PodMetricsResponseData> resultList = new ArrayList<>();
    for (Document doc : aggregationResult) {
        List<Document> pods = (List<Document>) doc.get("pods");
        if (pods != null && !pods.isEmpty()) { // Check if pods is not null and not empty
            PodMetricsResponseData responseData = new PodMetricsResponseData();
            responseData.setNamespaceName(doc.getString("namespaceName"));
            List<PodMetricDTO> podMetricsList = new ArrayList<>();
            int totalCount = 0;
            for (Document podDoc : pods) {
                PodMetricDTO podMetricsData = new PodMetricDTO();
                podMetricsData.setPodName(podDoc.getString("podName"));
                podMetricsData.setNamespaceName(doc.getString("namespaceName"));
                List<Document> metrics = (List<Document>) podDoc.get("metrics");
                if (metrics != null && !metrics.isEmpty()) {
                    List<MetricDTO> metricDataList = new ArrayList<>();
                    totalCount += metrics.size(); // Increment total count by the number of metrics in this pod
                    for (Document metricDoc : metrics) {
                        MetricDTO metricData = new MetricDTO();
                        metricData.setCpuUsage(metricDoc.getDouble("cpuUsage"));
                        metricData.setDate(metricDoc.getDate("date"));
                        metricData.setMemoryUsage(metricDoc.getLong("memoryUsage"));
                        metricDataList.add(metricData);
                    }
                    podMetricsData.setMetrics(metricDataList);
                }
                podMetricsList.add(podMetricsData);
            }
            responseData.setPods(podMetricsList);
            responseData.setTotalCount(totalCount); // Set total count for this namespace
            resultList.add(responseData);
        }
    }
    return resultList;
}
}




    
