package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricQuery;
import com.zaga.repo.KeplerMetricRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

@ApplicationScoped
public class KeplerMetricHandler {

  @Inject
  KeplerMetricRepo keplerMetricRepo;

  @Inject
  MongoClient mongoClient;

  // public List<KeplerMetric> getAllKeplerData() {
  //   List<KeplerMetric> allKepler = keplerMetricRepo.listAll();
  //   return allKepler;
  // }

  public List<KeplerMetricQuery> getKeplerData(
    // LocalDate from,
    // LocalDate to,
    // int minutesAgo
  ) {
    MongoCollection<Document> collection = mongoClient
      .getDatabase("KeplerMetric")
      .getCollection("Metrics");
    // Bson matchStage;

    List<Bson> pipeline = Arrays.asList(
      new Document(
        "$project",
        new Document("resourceMetrics.scopeMetrics.metrics", 1L)
      ),
      new Document("$unwind", new Document("path", "$resourceMetrics")),
      new Document(
        "$unwind",
        new Document("path", "$resourceMetrics.scopeMetrics")
      ),
      new Document(
        "$unwind",
        new Document("path", "$resourceMetrics.scopeMetrics.metrics")
      ),
      new Document(
        "$match",
        new Document(
          "resourceMetrics.scopeMetrics.metrics.name",
          "kepler_container_cpu_cycles_total"
        )
      ),
      new Document(
        "$unwind",
        new Document(
          "path",
          "$resourceMetrics.scopeMetrics.metrics.sum"
        )
      )
    //   new Document(
    //     "$unwind",
    //     new Document(
    //       "path",
    //       "$resourceMetrics.scopeMetrics.metrics.sum.dataPoints.attributes"
    //     )
    //   )
    );
    AggregateIterable<Document> aggregationResult = collection.aggregate(
      pipeline
    );

    List<KeplerMetricQuery> keplerMetrics = new ArrayList<>();
    for(Document document : aggregationResult){
        System.out.println("Aggregation result:===================================== " + document.get("dataPoints").toString());
    }
    

    // Return the list of KeplerMetricQuery objects
    return keplerMetrics;
  }

 }
