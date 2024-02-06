package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.queryentity.podMetrics.MetricDTO;
import com.zaga.entity.queryentity.podMetrics.PodMetricDTO;
import com.zaga.entity.queryentity.podMetrics.PodMetricsResponseData;
import org.bson.Document;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class PodMetricsHandler {

    @Inject
    MongoClient mongoClient;

    public List<PodMetricsResponseData> getAllPodMetricsByDate(LocalDate from, LocalDate to, int page, int pageSize, int minutesAgo) {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("------------DB call startTimestamp------ " + startTime);

        MongoDatabase database = mongoClient.getDatabase("OtelPodMetrics");
        MongoCollection<Document> collection = database.getCollection("PodMetricDTO");

        List<PodMetricsResponseData> result = executeAggregationPipeline(database, collection,from,to);

        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("------------DB call endTimestamp------ " + endTime);
        System.out.println("-----------DB call ended Timestamp------ " + Duration.between(startTime, endTime));

        return result;
    }
    @SuppressWarnings("unchecked")
    public List<PodMetricsResponseData> executeAggregationPipeline(MongoDatabase database, MongoCollection<Document> collection,LocalDate from, LocalDate to) {
        System.out.println("-------------Aggregation pipeline FROM----------"+from);
        System.out.println("-------------Aggregation pipeline TO------------"+to);
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
                    new Document("metrics.justDate",
                        new Document("$gte", from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                            .append("$lte", to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
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
                    .append("pods", 1)
            )
        );
        

    
        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline, Document.class);
        List<PodMetricsResponseData> resultList = new ArrayList<>();
        for (Document doc : aggregationResult) {
            List<Document> pods = (List<Document>) doc.get("pods");
            if (pods != null && !pods.isEmpty()) { // Check if pods is not null and not empty
                PodMetricsResponseData responseData = new PodMetricsResponseData();
                responseData.setNamespaceName(doc.getString("namespaceName")); 
                // responseData.setPodName(doc.getString("podName"));
                List<PodMetricDTO> podMetricsList = new ArrayList<>();
                for (Document podDoc : pods) {
                    PodMetricDTO podMetricsData = new PodMetricDTO();
                    // responseData.setPodName(doc.getString("podName"));
                    podMetricsData.setPodName(podDoc.getString("podName"));
                    podMetricsData.setNamespaceName(doc.getString("namespaceName"));
                    List<Document> metrics = (List<Document>) podDoc.get("metrics");
                    if (metrics != null && !metrics.isEmpty()) {
                        List<MetricDTO> metricDataList = new ArrayList<>();
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
                resultList.add(responseData);
            }
        }
        return resultList;
        
}
}
    