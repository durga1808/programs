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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class PodMetricsHandler {

  @Inject
  MongoClient mongoClient;

  public List<PodMetricsResponseData> getAllPodMetricsByDate(
    LocalDate from,
    LocalDate to,
    int page,
    int pageSize,
    int minutesAgo
  ) {
    LocalDateTime startTime = LocalDateTime.now();
    System.out.println("------------DB call startTimestamp------ " + startTime);

    MongoDatabase database = mongoClient.getDatabase("OtelPodMetrics");
    MongoCollection<Document> collection = database.getCollection(
      "PodMetricDTO"
    );

    List<PodMetricsResponseData> result = executeAggregationPipeline(
      database,
      collection,
      from,
      to,
      page,
      pageSize
    );

    LocalDateTime endTime = LocalDateTime.now();
    System.out.println("------------DB call endTimestamp------ " + endTime);
    System.out.println(
      "-----------DB call ended Timestamp------ " +
      Duration.between(startTime, endTime)
    );

    return result;
  }

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
      )
    );

    // Execute aggregation pipeline
    AggregateIterable<Document> aggregationResult = collection.aggregate(
      pipeline,
      Document.class
    );

    // Map to store total count for each pod
    Map<String, Integer> podMetricCounts = new HashMap<>();

    // Result list to hold PodMetricsResponseData objects
    List<PodMetricsResponseData> resultList = new ArrayList<>();

    // Iterate through aggregation result
    for (Document doc : aggregationResult) {
      @SuppressWarnings("unchecked")
    List<Document> metrics = (List<Document>) doc.get("metrics");
      if (metrics != null && !metrics.isEmpty()) {
        String namespaceName = doc.getString("namespaceName");
        String podName = doc.getString("podName");
        PodMetricsResponseData responseData = new PodMetricsResponseData();
        responseData.setNamespaceName(namespaceName);
        // responseData.setPodName(podName);
        List<PodMetricDTO> podMetricsList = new ArrayList<>();

        // Calculate total count for this pod
        // int totalCount = metrics.size();
        // podMetricCounts.put(podName, totalCount);

        // Iterate through metrics for this pod
        for (Document metricDoc : metrics) {
          PodMetricDTO podMetricsData = new PodMetricDTO();
          podMetricsData.setPodName(podName);
          podMetricsData.setNamespaceName(namespaceName);
          MetricDTO metricData = new MetricDTO();
          metricData.setCpuUsage(metricDoc.getDouble("cpuUsage"));
          metricData.setDate(metricDoc.getDate("date"));
          metricData.setMemoryUsage(metricDoc.getLong("memoryUsage"));
          podMetricsData.setMetrics(Collections.singletonList(metricData));
          podMetricsList.add(podMetricsData);
        }
        responseData.setPods(podMetricsList);
        resultList.add(responseData);
      }
    }

    // // Update resultList to include total count for each pod
    // for (PodMetricsResponseData responseData : resultList) {
    //   List<PodMetricDTO> pods = responseData.getPods();
    //   int totalCount = 0;
    //   if (pods != null) {
    //     for (PodMetricDTO pod : pods) {
    //       String podName = pod.getPodName();
    //       totalCount += podMetricCounts.getOrDefault(podName, 0);
    //     }
    //   }
    //   responseData.setTotalCount(totalCount);
    // }

    return resultList;
  }
}
